package euclidea

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
        val solutionContext =
            solve(EuclideaContext(points = listOf(point1, point2), elements = listOf(line)), 4) { context ->
                solutions.any { context.hasElements(it) }
            }
        println(solutionContext)
    }

    @Test
    fun puzzle15_7() {
        // Drop a Perpendicular**
        // (lines only)
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        val solution = Element.Line(Point(0.0, 0.0), Point(0.0, 1.0))
        val point1 = Point(-1.043215, 0.0)
        val point2 = Point(-0.828934, 2.0)
        val givenPoints = listOf(center, point1, point2)
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = givenPoints,
            elements = listOf(circle, line, Element.Line(point1, point2))
        )
        val solutionContext = solve(initialContext, 5) { context ->
            context.hasElement(solution)
        }
        println(solutionContext)
    }

    private data class SearchNode(val context: EuclideaContext, val depth: Int)

    private fun solve(
        initialContext: EuclideaContext,
        maxDepth: Int,
        check: (EuclideaContext) -> Boolean
    ): EuclideaContext? {
        val seen = mutableSetOf<EuclideaSignature>()
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
                    val nextDepth = depth + 1
                    for (next in context.nexts()) {
                        if (seen.add(next.signature))
                            queue.add(SearchNode(next, nextDepth))
                    }
                }
            }
        }
    }
}