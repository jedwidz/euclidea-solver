package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

private data class SolveState(
    val context: EuclideaContext,
    val oldPoints: Set<Point>,
    val pendingElements: Set<Element>
)

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    prune: ((EuclideaContext) -> Boolean)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    fun sub(
        solveState: SolveState, depth: Int,
    ): EuclideaContext? {
        val nextDepth = depth + 1
        val (context, oldPoints, pendingElements) = solveState
        with(context) {
            val newPoints = points.filter { it !in oldPoints }
            val nextOldPoints = oldPoints + newPoints

            val newElements = mutableListOf<Element>()
            fun tryAdd(e: Element?) {
                if (e !== null && !hasElement(e))
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

            val newPendingElements = pendingElements + newElements

            var nextPendingElements = newPendingElements
            newPendingElements.forEach { newElement ->
                nextPendingElements = nextPendingElements.minus(newElement)
                val next = SolveState(withElement(newElement), nextOldPoints, nextPendingElements)
                val nextContext = next.context
                if (check(nextContext))
                    return nextContext
                else if (nextDepth < maxDepth && (prune == null || !prune(nextContext)))
                    sub(next, nextDepth)?.let { return@sub it }
            }
        }
        return null
    }
    if (check(initialContext))
        return initialContext
    return sub(SolveState(initialContext, setOf(), setOf()), 0)
}

