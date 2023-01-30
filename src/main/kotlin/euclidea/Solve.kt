package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.random.Random
import kotlin.system.measureTimeMillis


data class SolveContext(
    val context: EuclideaContext,
    val depth: Int
)

private data class SolveState(
    val solveContext: SolveContext,
    val oldPoints: Set<Point>,
    val nonNewElementCount: Int,
    val consecutiveNonNewElementCount: Int,
    val lastAddedElements: List<Element>,
    val remainingToolSequence: List<EuclideaTool>?,
    val extraElementCount: Int,
    val unfamiliarElementCount: Int
)

private data class PendingNode(
    val element: Element,
    val visitPriority: Int,
    val isNew: Boolean
) : Comparable<PendingNode> {

    override fun compareTo(other: PendingNode): Int {
        // Note reverse order
        return compareValuesBy(other, this, { it.visitPriority }, { it.isNew })
    }
}

private data class SolveScratch(
    val pendingElements: ElementsByTool = ElementsByTool(),
    val passedElements: ElementSet = ElementSet(),
    val random: Random = Random(0)
) {
    fun dupe(): SolveScratch {
        return SolveScratch(ElementsByTool(pendingElements), ElementSet(passedElements), Random(random.nextInt()))
    }
}

private val forkThreadCount = 6  // Runtime.getRuntime().availableProcessors()
private val forkDepth = 2
private val forkQueueSize = 50

// Don't combine points closer than this distance (somewhat unlikely to be used in a real solution, also prone to numeric instability)
private val minSeparation = 0.01

fun separated(point1: Point, point2: Point): Boolean {
    return distance(point1, point2) >= minSeparation
}

fun separated(point1: Point, point2: Point, point3: Point): Boolean {
    return separated(point1, point2) && separated(point2, point3) && separated(point3, point1)
}

data class ExtraElementConstraint(
    val knownElements: ElementSet,
    val maxExtraElements: Int,
    val maxUnfamiliarElements: Int?
)

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    maxNonNewElements: Int? = null,
    maxConsecutiveNonNewElements: Int? = null,
    prune: ((SolveContext) -> Boolean)? = null,
    visitPriority: ((SolveContext, Element) -> Int)? = null,
    pass: ((SolveContext, Element) -> Boolean)? = null,
    remainingStepsLowerBound: ((EuclideaContext) -> Int)? = null,
    extraElementConstraint: ExtraElementConstraint? = null,
    excludeElements: ElementSet? = null,
    toolSequence: List<EuclideaTool>? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {

    val knownElements = extraElementConstraint?.knownElements
    val maxExtraElements = extraElementConstraint?.maxExtraElements
    val maxUnfamiliarElements = extraElementConstraint?.maxUnfamiliarElements

    val familiarLineHeadings = DoubleSet()
    val familiarCircleRadii = DoubleSet()
    val familiarLineHeadingIncrements = 4
    val familiarLineHeadingIncrement = PI * 2.0 / familiarLineHeadingIncrements
    knownElements?.items()?.forEach { element ->
        fun addFamiliarLineHeading(heading: Double) {
            val normalHeading = normalizeLineHeading(heading)
            familiarLineHeadings += normalHeading
            // Handle wrap edge cases
            if (coincides(normalHeading, 0.0))
                familiarLineHeadings += normalHeading + PI * 2.0
            if (coincides(normalHeading, PI * 2.0))
                familiarLineHeadings += normalHeading - PI * 2.0
        }
        when (element) {
            is Element.Line -> {
                val lineHeading = element.heading
                for (i in 0.until(familiarLineHeadingIncrements))
                    addFamiliarLineHeading(lineHeading + i * familiarLineHeadingIncrement)
            }
            is Element.Circle -> {
                val radius = element.radius
                familiarCircleRadii += radius
                familiarCircleRadii += 2.0 * radius
                familiarCircleRadii += 0.5 * radius
            }
        }
    }

    fun isFamiliarElement(element: Element): Boolean {
        return when (element) {
            is Element.Line -> element.heading in familiarLineHeadings
            is Element.Circle -> element.radius in familiarCircleRadii
        }
    }

    if (check(initialContext))
        return initialContext

    val parallelSolver = object {
        val results = ConcurrentLinkedQueue<EuclideaContext>()

        // Plus one is for the 'root' call, which can block while forking
        val executor = Executors.newFixedThreadPool(forkThreadCount + 1)
        val outstandingCount = AtomicInteger()
        var forkQueueCount = Semaphore(forkQueueSize + forkThreadCount)

        fun yieldResult(context: EuclideaContext) {
            results.add(context)
            executor.shutdownNow()
        }

        fun awaitResult(): EuclideaContext? {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
            return results.poll()
        }

        fun fork(solveState: SolveState, solveScratch: SolveScratch) {
            outstandingCount.incrementAndGet()
            forkQueueCount.acquire()
            executor.submit {
                val timeMillis = measureTimeMillis {
                    try {
                        sub(solveState, solveScratch)
                    } catch (e: Throwable) {
                        println("Batch threw up... $e")
                        e.printStackTrace()
                    } finally {
                        forkQueueCount.release()
                        val count = outstandingCount.decrementAndGet()
                        if (count == 0)
                            executor.shutdown()
                    }
                }
                println("Batch completed in ${timeMillis}ms; queued tasks ${outstandingCount.get()}")
            }
        }

        fun sub(solveState: SolveState, solveScratch: SolveScratch) {
            val pendingElements = solveScratch.pendingElements
            val passedElements = solveScratch.passedElements
            val random = solveScratch.random

            val (solveContext, oldPoints) = solveState
            val (context, depth) = solveContext
            val nextDepth = depth + 1
            with(context) {
                val newPoints = points.filter { it !in oldPoints }
                val nextOldPoints = oldPoints + newPoints

                val nextTool = solveState.remainingToolSequence?.first()

                val newElements = mutableSetOf<Element>()
                fun tryAdd(e: Element?) {
                    if (e !== null && e !in pendingElements && e !in passedElements && !hasElement(e) && excludeElements?.let { e in it } != true) {
                        pendingElements += e
                        if (nextTool === null || nextTool === e.sourceTool)
                            newElements += e
                    }
                }

                val remainingConfig =
                    solveState.remainingToolSequence?.let { config.restrictConfig(it.toSet()) } ?: config

                if (remainingConfig.anyTwoPointToolEnabled) {
                    fun visit(point1: Point, point2: Point) {
                        if (separated(point1, point2)) {
                            if (remainingConfig.lineToolEnabled)
                                tryAdd(lineTool(point1, point2))
                            if (remainingConfig.circleToolEnabled) {
                                tryAdd(circleTool(point1, point2))
                                tryAdd(circleTool(point2, point1))
                            }
                            if (remainingConfig.perpendicularBisectorToolEnabled)
                                tryAdd(perpendicularBisectorTool(point1, point2))
                        }
                    }
                    newPoints.forEachIndexed { i, newPoint ->
                        oldPoints.forEach { visit(newPoint, it) }
                        for (j in i + 1 until newPoints.size)
                            visit(newPoint, newPoints[j])
                    }
                }

                if (remainingConfig.anyLinePointToolEnabled) {
                    fun visit(line: Element.Line, point: Point) {
                        if (remainingConfig.perpendicularToolEnabled)
                            tryAdd(perpendicularTool(line, point))
                        if (remainingConfig.parallelToolEnabled)
                            tryAdd(parallelTool(line, point))
                    }

                    val newLines = solveState.lastAddedElements.filterIsInstance<Element.Line>().toSet()
                    val oldLines = elements.filterIsInstance<Element.Line>() - newLines
                    oldLines.forEach { oldLine ->
                        newPoints.forEach { newPoint ->
                            visit(oldLine, newPoint)
                        }
                    }
                    newLines.forEach { newLine ->
                        points.forEach { point ->
                            visit(newLine, point)
                        }
                    }
                }

                if (remainingConfig.anyThreePointToolEnabled) {
                    fun visit(point1: Point, point2: Point, point3: Point) {
                        if (separated(point1, point2, point3)) {
                            if (remainingConfig.angleBisectorToolEnabled) {
                                tryAdd(angleBisectorTool(point1, point2, point3))
                                tryAdd(angleBisectorTool(point2, point3, point1))
                                tryAdd(angleBisectorTool(point3, point1, point2))
                            }
                            if (remainingConfig.nonCollapsingCompassToolEnabled) {
                                tryAdd(nonCollapsingCompassTool(point1, point2, point3))
                                tryAdd(nonCollapsingCompassTool(point2, point3, point1))
                                tryAdd(nonCollapsingCompassTool(point3, point1, point2))
                            }
                        }
                    }

                    val oldPointsList = oldPoints.toList()

                    // 3 new
                    newPoints.forEachTriple { n1, n2, n3 -> visit(n1, n2, n3) }

                    // 2 new + 1 old
                    newPoints.forEachPair { n1, n2 ->
                        oldPointsList.forEach { o1 -> visit(n1, n2, o1) }
                    }

                    // 1 new + 2 old
                    newPoints.forEach { n1 ->
                        oldPointsList.forEachPair { o1, o2 -> visit(n1, o1, o2) }
                    }
                }

                fun maybePass(items: List<Element>): List<Element> {
                    return when (pass) {
                        null -> items
                        else -> {
                            val keep = mutableListOf<Element>()
                            val skipNonNewElements =
                                maxConsecutiveNonNewElements != null && solveState.consecutiveNonNewElementCount >= maxConsecutiveNonNewElements
                            items.forEach { element ->
                                val skipAsNonNewElement = skipNonNewElements && element !in newElements
                                if (!skipAsNonNewElement && !pass(solveContext, element))
                                    keep += element
                            }
                            keep
                        }
                    }
                }

                fun maybePrioritize(items: List<Element>): List<Element> {
                    // Prioritize known elements by default
                    val defaultPriority: ((SolveContext, Element) -> Int)? = when {
                        knownElements === null -> null
                        depth > forkDepth + 1 -> null
                        else -> {
                            val randomRange = 0..100
                            { _, element ->
                                val knownComponent = if (element in knownElements) 0 else 1
                                val randomComponent = randomRange.random(random)
                                knownComponent * randomRange.last + randomComponent
                            }
                        }
                    }
                    return when (val effectivePriority = visitPriority ?: defaultPriority) {
                        null -> items
                        else -> {
                            // looks like sortedBy evaluates its selector more than once, so likely more efficient to 'precalc' it
                            val prioritized = items.map { element ->
                                PendingNode(
                                    element = element,
                                    visitPriority = effectivePriority(solveContext, element),
                                    isNew = element in newElements
                                )
                            }.sorted()
                            prioritized.map { it.element }
                        }
                    }
                }

                val keep = maybePass(pendingElements.itemsForTool(nextTool))
                val pendingList = maybePrioritize(keep)
                // println("$depth - ${pendingList.size}")

                // val distances = pendingList.map { it.distance() }.sorted().take(10)
                // println("Distances: $distances")

                for (newElement in pendingList) {
                    if (Thread.currentThread().isInterrupted)
                        return
                    val removed = pendingElements.remove(newElement)
                    if (!removed) {
                        println("Not removed")
                    }
                    assert(removed)
                    passedElements.add(newElement)
                    val isNonNewElement = newElement !in newElements
                    val nextNonNewElementCount = solveState.nonNewElementCount + (if (isNonNewElement) 1 else 0)
                    val isExtraElement = knownElements?.let { newElement !in it } ?: false
                    val nextExtraElementCount = solveState.extraElementCount + if (isExtraElement) 1 else 0
                    val nextUnfamiliarElementCount =
                        solveState.unfamiliarElementCount + if (maxUnfamiliarElements !== null && !isFamiliarElement(
                                newElement
                            )
                        ) 1 else 0
                    val nextConsecutiveNonNewElementCount =
                        if (isNonNewElement) solveState.consecutiveNonNewElementCount + 1 else 0
                    if ((maxNonNewElements == null || nextNonNewElementCount <= maxNonNewElements) &&
                        (maxExtraElements == null || nextExtraElementCount <= maxExtraElements) &&
                        (maxUnfamiliarElements == null || nextUnfamiliarElementCount <= maxUnfamiliarElements)
                    ) {
                        val nextContext = withElement(newElement)
                        val nextSolveContext = SolveContext(nextContext, nextDepth)
                        val next =
                            SolveState(
                                nextSolveContext,
                                nextOldPoints,
                                nextNonNewElementCount,
                                nextConsecutiveNonNewElementCount,
                                listOf(newElement),
                                solveState.remainingToolSequence?.drop(1),
                                nextExtraElementCount,
                                nextUnfamiliarElementCount
                            )
                        val lowerBound = remainingStepsLowerBound?.let { it(nextContext) }
                        if ((lowerBound == null || lowerBound <= 0) && check(nextContext))
                            yieldResult(nextContext)
                        else if (nextDepth < maxDepth &&
                            (lowerBound == null || lowerBound + nextDepth <= maxDepth) &&
                            (prune == null || !prune(nextSolveContext))
                        ) {
                            val newSolveScratch = solveScratch.dupe()
                            if (depth == forkDepth)
                                fork(next, newSolveScratch)
                            else
                                sub(next, newSolveScratch)
                        }
                    }
                }
            }
        }
    }

    val effectiveToolSequence = if (toolSequence === null) null else {
        val toolSteps = toolSequence.size
        when {
            toolSteps == maxDepth -> toolSequence
            toolSteps > maxDepth -> {
                println("Warning - truncating tool sequence of length $toolSteps to agree with max depth of $maxDepth")
                toolSequence.take(maxDepth)
            }
            else -> error("Tool sequence of length $toolSteps less than max depth of $maxDepth")
        }
    }
    parallelSolver.fork(
        SolveState(
            solveContext = SolveContext(initialContext, 0),
            oldPoints = setOf(),
            nonNewElementCount = 0,
            consecutiveNonNewElementCount = 0,
            lastAddedElements = initialContext.elements,
            remainingToolSequence = effectiveToolSequence,
            extraElementCount = 0,
            unfamiliarElementCount = 0
        ),
        SolveScratch()
    )
    return parallelSolver.awaitResult()
}
