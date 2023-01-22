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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
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
    val lastAddedElements: List<Element>
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
    val pendingElements: ElementSet = ElementSet(),
    val passedElements: ElementSet = ElementSet()
) {
    fun dupe(): SolveScratch {
        return SolveScratch(pendingElements.dupe(), passedElements.dupe())
    }
}

private fun ElementSet.dupe(): ElementSet {
    val res = ElementSet()
    res += this
    return res
}

private val threadCount = 6  // Runtime.getRuntime().availableProcessors()
private val forkDepth = 2

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    nonNewElementLimit: Int? = null,
    consecutiveNonNewElementLimit: Int? = null,
    prune: ((SolveContext) -> Boolean)? = null,
    visitPriority: ((SolveContext, Element) -> Int)? = null,
    pass: ((SolveContext, Element) -> Boolean)? = null,
    remainingStepsLowerBound: ((EuclideaContext) -> Int)? = null,
    excludeElements: ElementSet? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {

    if (check(initialContext))
        return initialContext

    val parallelSolver = object {
        val results = ConcurrentLinkedQueue<EuclideaContext>()
        val executor = Executors.newFixedThreadPool(threadCount)
        val outstandingCount = AtomicInteger()

        fun yieldResult(context: EuclideaContext) {
            results.add(context)
            executor.shutdownNow()
        }

        fun awaitResult(): EuclideaContext? {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
            return results.poll()
        }

        fun fork(solveState: SolveState, solveScratch: SolveScratch) {
            val newSolveScratch = solveScratch.dupe()
            outstandingCount.incrementAndGet()
            executor.submit {
                val timeMillis = measureTimeMillis {
                    try {
                        sub(solveState, newSolveScratch)
                    } catch (e: Throwable) {
                        println("Batch threw up... $e")
                        e.printStackTrace()
                    } finally {
                        val count = outstandingCount.decrementAndGet()
                        if (count == 0)
                            executor.shutdown()
                    }
                }
                println("Batch completed in ${timeMillis}ms")
            }
        }

        fun sub(solveState: SolveState, solveScratch: SolveScratch) {
            val pendingElements: ElementSet = solveScratch.pendingElements
            val passedElements: ElementSet = solveScratch.passedElements

            val (solveContext, oldPoints) = solveState
            val (context, depth) = solveContext
            val nextDepth = depth + 1
            with(context) {
                val newPoints = points.filter { it !in oldPoints }
                val nextOldPoints = oldPoints + newPoints

                val newElements = mutableSetOf<Element>()
                fun tryAdd(e: Element?) {
                    if (e !== null && e !in pendingElements && e !in passedElements && !hasElement(e) && excludeElements?.let { e in it } != true) {
                        pendingElements += e
                        newElements.add(e)
                    }
                }

                if (config.anyTwoPointToolEnabled) {
                    fun visit(point1: Point, point2: Point) {
                        if (config.lineToolEnabled)
                            tryAdd(lineTool(point1, point2))
                        if (config.circleToolEnabled) {
                            tryAdd(circleTool(point1, point2))
                            tryAdd(circleTool(point2, point1))
                        }
                        if (config.perpendicularBisectorToolEnabled)
                            tryAdd(perpendicularBisectorTool(point1, point2))
                    }
                    newPoints.forEachIndexed { i, newPoint ->
                        oldPoints.forEach { visit(newPoint, it) }
                        for (j in i + 1 until newPoints.size)
                            visit(newPoint, newPoints[j])
                    }
                }

                if (config.anyLinePointToolEnabled) {
                    fun visit(line: Element.Line, point: Point) {
                        if (config.perpendicularToolEnabled)
                            tryAdd(perpendicularTool(line, point))
                        if (config.parallelToolEnabled)
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

                if (config.anyThreePointToolEnabled) {
                    fun visit(point1: Point, point2: Point, point3: Point) {
                        if (config.angleBisectorToolEnabled) {
                            tryAdd(angleBisectorTool(point1, point2, point3))
                            tryAdd(angleBisectorTool(point2, point3, point1))
                            tryAdd(angleBisectorTool(point3, point1, point2))
                        }
                        if (config.nonCollapsingCompassToolEnabled) {
                            tryAdd(nonCollapsingCompassTool(point1, point2, point3))
                            tryAdd(nonCollapsingCompassTool(point2, point3, point1))
                            tryAdd(nonCollapsingCompassTool(point3, point1, point2))
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

                fun maybePass(items: List<Element>): Pair<List<Element>, List<Element>> {
                    return when (pass) {
                        null -> items to listOf()
                        else -> {
                            val keep = mutableListOf<Element>()
                            val skippedNewElements = mutableListOf<Element>()
                            val skipNonNewElements =
                                consecutiveNonNewElementLimit != null && solveState.consecutiveNonNewElementCount >= consecutiveNonNewElementLimit
                            items.forEach { element ->
                                val skipAsNonNewElement = skipNonNewElements && element !in newElements
                                if (skipAsNonNewElement || pass(solveContext, element)) {
                                    if (element in newElements)
                                        skippedNewElements += element
                                } else keep += element
                            }
                            keep to skippedNewElements
                        }
                    }
                }

                fun maybePrioritize(items: List<Element>): List<Element> {
                    return when (visitPriority) {
                        null -> items
                        else -> {
                            // looks like sortedBy evaluates its selector more than once, so likely more efficient to 'precalc' it
                            val prioritized = items.map { element ->
                                PendingNode(
                                    element = element,
                                    visitPriority = visitPriority(solveContext, element),
                                    isNew = element in newElements
                                )
                            }.sorted()
                            prioritized.map { it.element }
                        }
                    }
                }

                val (keep, skippedNewElements) = maybePass(pendingElements.items())
                val pendingList = maybePrioritize(keep)
                // println("$depth - ${pendingList.size}")

                val removedElements = mutableSetOf<Element>()
                val newPassedElements = mutableSetOf<Element>()
                for (newElement in pendingList) {
                    if (Thread.currentThread().isInterrupted)
                        return
                    val removed = pendingElements.remove(newElement)
                    if (!removed) {
                        println("Not removed)")
                    }
                    assert(removed)
                    passedElements.add(newElement)
                    newPassedElements.add(newElement)
                    val isNonNewElement = newElement !in newElements
                    if (isNonNewElement)
                        removedElements.add(newElement)
                    val nextNonNewElementCount = solveState.nonNewElementCount + (if (isNonNewElement) 1 else 0)
                    val nextConsecutiveNonNewElementCount =
                        if (isNonNewElement) solveState.consecutiveNonNewElementCount + 1 else 0
                    if (nonNewElementLimit == null || nextNonNewElementCount < nonNewElementLimit) {
                        val nextContext = withElement(newElement)
                        val nextSolveContext = SolveContext(nextContext, nextDepth)
                        val next =
                            SolveState(
                                nextSolveContext,
                                nextOldPoints,
                                nextNonNewElementCount,
                                nextConsecutiveNonNewElementCount,
                                listOf(newElement)
                            )
                        val lowerBound = remainingStepsLowerBound?.let { it(nextContext) }
                        if ((lowerBound == null || lowerBound <= 0) && check(nextContext))
                            yieldResult(nextContext)
                        else if (nextDepth < maxDepth &&
                            (lowerBound == null || lowerBound + nextDepth <= maxDepth) &&
                            (prune == null || !prune(nextSolveContext))
                        ) {
                            if (depth == forkDepth)
                                fork(next, solveScratch)
                            else
                                sub(next, solveScratch)
                        }
                    }
                }
                pendingElements -= skippedNewElements
                pendingElements += removedElements
                passedElements -= newPassedElements
            }
        }
    }
    parallelSolver.fork(
        SolveState(SolveContext(initialContext, 0), setOf(), 0, 0, initialContext.elements),
        SolveScratch()
    )
    return parallelSolver.awaitResult()
}
