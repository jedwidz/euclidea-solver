package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

private data class SolveState(
    val context: EuclideaContext,
    val oldPoints: Set<Point>
)

private data class PendingNode(
    val element: Element,
    val remainingStepsLowerBound: Int
) : Comparable<PendingNode> {

    override fun compareTo(other: PendingNode): Int {
        return remainingStepsLowerBound.compareTo(other.remainingStepsLowerBound)
    }
}

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    prune: ((EuclideaContext) -> Boolean)? = null,
    remainingStepsLowerBound: ((EuclideaContext, Element) -> Int)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    val pendingElements = ElementSet()
    val passedElements = ElementSet()
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
                if (e !== null && e !in pendingElements && e !in passedElements && !hasElement(e))
                    newElements.add(e)
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

            pendingElements += newElements

            val pendingList = pendingElements.items().mapNotNull { element ->
                val lowerBound = remainingStepsLowerBound?.let { it(context, element) } ?: 0
                if (nextDepth + lowerBound <= maxDepth)
                    PendingNode(
                        element = element,
                        remainingStepsLowerBound = lowerBound
                    )
                else null
            }.sorted()

            val removedElements = mutableSetOf<Element>()
            val newPassedElements = mutableSetOf<Element>()
            for (pendingNode in pendingList) {
                val newElement = pendingNode.element
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
                else if (nextDepth < maxDepth && (prune == null || !prune(nextContext)))
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

