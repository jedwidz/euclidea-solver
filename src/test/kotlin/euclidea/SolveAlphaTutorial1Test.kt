package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolveAlphaTutorial1Test {
    @Test
    fun alphaTutorial1() {
        // Equilateral Triangle
        // TODO line segment rather than infinite line
        val point1 = Point.Origin
        val point2 = Point(1.0, 0.0)
        val line = Element.Line(point1, point2)
        val solutions = run {
            val circle1 = circleTool(point1, point2)
            val circle2 = circleTool(point2, point1)
            intersect(circle1, circle2).points().map { pointX ->
                listOf(point1, point2).map { pointA -> lineTool(pointA, pointX) }
            }
        }
        val solutionContext =
            solve(EuclideaContext.of(points = listOf(point1, point2), elements = listOf(line)), 4) { context ->
                solutions.any { context.hasElements(it) }
            }
        dumpSolution(solutionContext)
    }
}
