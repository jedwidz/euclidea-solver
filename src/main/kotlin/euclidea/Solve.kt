package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

private data class SolveState(
    val context: EuclideaContext,
    val oldPoints: Set<Point>
)

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    prune: ((EuclideaContext) -> Boolean)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    val pendingElements = mutableSetOf<Element>()
    val passedElements = mutableSetOf<Element>()
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

            pendingElements.addAll(newElements)

            val removedElements = mutableSetOf<Element>()
            val newPassedElements = mutableSetOf<Element>()
            while (true) {
                val newElement = pendingElements.firstOrNull() ?: break
                pendingElements.remove(newElement)
                passedElements.add(newElement)
                newPassedElements.add(newElement)
                if (newElement !in newElements)
                    removedElements.add(newElement)
                val next = SolveState(withElement(newElement), nextOldPoints)
                val nextContext = next.context
                if (check(nextContext))
                    return nextContext
                else if (nextDepth < maxDepth && (prune == null || !prune(nextContext)))
                    sub(next, nextDepth)?.let { return@sub it }
            }
            pendingElements.addAll(removedElements)
            passedElements.removeAll(newPassedElements)
        }
        return null
    }
    if (check(initialContext))
        return initialContext
    return sub(SolveState(initialContext, setOf()), 0)
}

