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
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 7) { context ->
            context.hasElement(solution)
        }
        println(solutionContext)
    }

    @Test
    fun puzzle15_7_par() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a parallel line
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        val solution = Element.Line(Point(0.0, 0.0), Point(0.0, 1.0))
        // 'probe' line to cut across the circle and line.
        val probeLine1 = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val probeLine2 = Element.Line(Point(1.134342, 0.0), Point(0.312323, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElement(probeLine1).withElement(probeLine2)
        val solutionContext = solve(initialContext, 3) { context ->
            context.elements.any {
                it !== circle && it !== line && it !== probeLine1 && it !== probeLine2 && intersect(
                    line,
                    it
                ) == Intersection.Disjoint
            }
        }
        println(solutionContext)
    }

    private fun solve(
        initialContext: EuclideaContext,
        maxDepth: Int,
        check: (EuclideaContext) -> Boolean
    ): EuclideaContext? {
        fun sub(context: EuclideaContext, depth: Int): EuclideaContext? {
            val nextDepth = depth + 1
            for (next in context.nexts())
                if (check(next))
                    return next
                else if (nextDepth < maxDepth)
                    sub(next, nextDepth)
            return null
        }
        if (check(initialContext))
            return initialContext
        return sub(initialContext, 0)
    }
}