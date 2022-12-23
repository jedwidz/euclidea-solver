package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool

data class SolveContext(
    val context: EuclideaContext,
    val depth: Int
)

private data class SolveState(
    val solveContext: SolveContext,
    val oldPoints: Set<Point>,
    val nonNewElementCount: Int,
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

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    nonNewElementLimit: Int? = null,
    prune: ((SolveContext) -> Boolean)? = null,
    visitPriority: ((SolveContext, Element) -> Int)? = null,
    pass: ((SolveContext, Element) -> Boolean)? = null,
    remainingStepsLowerBound: ((EuclideaContext) -> Int)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    val pendingElements = ElementSet()
    val passedElements = ElementSet()

    fun sub(solveState: SolveState): EuclideaContext? {
        val (solveContext, oldPoints) = solveState
        val (context, depth) = solveContext
        val nextDepth = depth + 1
        with(context) {
            val newPoints = points.filter { it !in oldPoints }
            val nextOldPoints = oldPoints + newPoints

            val newElements = mutableSetOf<Element>()
            fun tryAdd(e: Element?) {
                if (e !== null && e !in pendingElements && e !in passedElements && !hasElement(e)) {
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
                    if (config.perpendicularBisectorToolEnabled) {
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
                        items.forEach { element ->
                            if (pass(solveContext, element)) {
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

            val pendingList = maybePrioritize(maybePass(pendingElements.items()))
            // println("$depth - ${pendingList.size}")

            val removedElements = mutableSetOf<Element>()
            val newPassedElements = mutableSetOf<Element>()
            for (newElement in pendingList) {
                val removed = pendingElements.remove(newElement)
                assert(removed)
                passedElements.add(newElement)
                newPassedElements.add(newElement)
                val isNonNewElement = newElement !in newElements
                if (isNonNewElement)
                    removedElements.add(newElement)
                val nextNonNewElementCount = solveState.nonNewElementCount + (if (isNonNewElement) 1 else 0)
                if (nonNewElementLimit == null || nextNonNewElementCount < nonNewElementLimit) {
                    val nextContext = withElement(newElement)
                    val nextSolveContext = SolveContext(nextContext, nextDepth)
                    val next = SolveState(nextSolveContext, nextOldPoints, nextNonNewElementCount, listOf(newElement))
                    val lowerBound = remainingStepsLowerBound?.let { it(nextContext) }
                    if ((lowerBound == null || lowerBound <= 0) && check(nextContext))
                        return nextContext
                    else if (nextDepth < maxDepth &&
                        (lowerBound == null || lowerBound + nextDepth <= maxDepth) &&
                        (prune == null || !prune(nextSolveContext))
                    ) {
                        sub(next)?.let { return@sub it }
                    }
                }
            }
            pendingElements -= skippedNewElements
            pendingElements += removedElements
            passedElements -= newPassedElements
        }
        return null
    }
    if (check(initialContext))
        return initialContext
    return sub(SolveState(SolveContext(initialContext, 0), setOf(), 0, initialContext.elements))
}

