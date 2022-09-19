package euclidea

import EuclideaContext
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class RoughSolveTest {
    @Test
    fun alphaTutorial1() {
        // Equilateral Triangle
        // TODO line segment rather than infinite line
        val point1 = Point.Origin
        val point2 = Point(1.0, 0.0)
        val line = Element.Line(point1, point2)
        val solutions = run {
            val circle1 = circleTool(point1, point2)!!
            val circle2 = circleTool(point2, point1)!!
            intersect(circle1, circle2).points().map { pointX ->
                listOf(point1, point2).map { pointA -> lineTool(pointA, pointX)!! }
            }
        }
        val solutionContext = solve(EuclideaContext(listOf(point1, point2), listOf(line)), 4) { context ->
            solutions.any { context.containsAll(it) }
        }
        println(solutionContext)
    }

    private data class SearchNode(val context: EuclideaContext, val depth: Int)

    private fun solve(
        initialContext: EuclideaContext,
        maxDepth: Int,
        check: (EuclideaContext) -> Boolean
    ): EuclideaContext? {
        val queue: ArrayDeque<SearchNode> = ArrayDeque(listOf(SearchNode(initialContext, 0)))
        while (true) {
            val node = queue.removeFirstOrNull()
            if (node === null) {
                return null
            }
            with(node) {
                if (check(context)) {
                    return context
                } else if (depth < maxDepth) {
                    queue.addAll(context.nexts().map {
                        val nextDepth = depth + 1
                        SearchNode(it, nextDepth)
                    })
                }
            }
        }
    }
}