package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

private data class SolveState(
    val context: EuclideaContext,
    val oldPoints: Set<Point>
)

private data class PendingNode(
    val element: Element,
    val visitPriority: Int
) : Comparable<PendingNode> {

    override fun compareTo(other: PendingNode): Int {
        return other.visitPriority.compareTo(visitPriority)
    }
}

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    prune: ((EuclideaContext) -> Boolean)? = null,
    visitPriority: ((Element) -> Int)? = null,
    remainingStepsLowerBound: ((EuclideaContext) -> Int)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    val pendingElements = ElementSet()
    val passedElements = ElementSet()

    fun maybePrioritize(items: List<Element>): List<Element> {
        return when (visitPriority) {
            null -> items
            else -> {
                // looks like sortedBy evaluates its selector more than once, so likely more efficient to 'precalc' it
                items.map { element ->
                    PendingNode(
                        element = element,
                        visitPriority = visitPriority(element)
                    )
                }.sorted().map { it.element }
            }
        }
    }

    fun sub(
        solveState: SolveState, depth: Int,
    ): EuclideaContext? {
        val nextDepth = depth + 1
        val (context, oldPoints) = solveState
        with(context) {
            val newPoints = points.filter { it !in oldPoints }
            val nextOldPoints = oldPoints + newPoints

            val newElements = mutableListOf<Element>()
            fun tryAdd(e: Element?) {
                if (e !== null && e !in pendingElements && e !in passedElements && !hasElement(e)) {
                    pendingElements += e
                    newElements.add(e)
                }
            }

            fun visit(point1: Point, point2: Point) {
                if (config.lineToolEnabled)
                    tryAdd(lineTool(point1, point2))
                if (config.circleToolEnabled) {
                    tryAdd(circleTool(point1, point2))
                    tryAdd(circleTool(point2, point1))
                }
            }
            newPoints.forEachIndexed { i, newPoint ->
                oldPoints.forEach { visit(newPoint, it) }
                for (j in i + 1 until newPoints.size)
                    visit(newPoint, newPoints[j])
            }

            val pendingList = maybePrioritize(pendingElements.items())
            // println("$depth - ${pendingList.size}")

            val removedElements = mutableSetOf<Element>()
            val newPassedElements = mutableSetOf<Element>()
            for (newElement in pendingList) {
                val removed = pendingElements.remove(newElement)
                assert(removed)
                passedElements.add(newElement)
                newPassedElements.add(newElement)
                if (newElement !in newElements)
                    removedElements.add(newElement)
                val nextContext = withElement(newElement)
                val next = SolveState(nextContext, nextOldPoints)
                if (check(nextContext))
                    return nextContext
                else if (nextDepth < maxDepth &&
                    (remainingStepsLowerBound == null || remainingStepsLowerBound(nextContext) + nextDepth <= maxDepth) &&
                    (prune == null || !prune(nextContext))
                )
                    sub(next, nextDepth)?.let { return@sub it }
            }
            pendingElements += removedElements
            passedElements -= newPassedElements
        }
        return null
    }
    if (check(initialContext))
        return initialContext
    return sub(SolveState(initialContext, setOf()), 0)
}

