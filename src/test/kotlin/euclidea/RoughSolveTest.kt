package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

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
        dumpSolution(solutionContext)
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
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_point() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a point on the vertical line, rather than the vertical line itself, saving one ply
        // >> useful partial solution at depth 7 (~23 sec)
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        // Extra parallel line, not necessarily part of optimal solution but let's see...
        val bonusLine = Element.Line(Point(0.0, 2.0), Point(1.0, 2.0))
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(50.0)),
            points = listOf(center),
            elements = listOf(circle, line, bonusLine)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 7) { context ->
            context.points.any { point -> coincides(point.x, 0.0) && !coincides(point.y, 2.0) }
        }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_check_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Reproduce and check my best solution so far
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        // 'probe' line to cut across the circle and line.
        val probeLineIntercept = Point(-1.043215, 0.0)
        val probeLine = Element.Line(probeLineIntercept, Point(-0.828934, 3.0))
        // Solution works regardless of point 'order' here
        val (xPoint1, xPoint2) = intersectTwoPoints(circle, probeLine)
        val xLine1 = Element.Line(center, xPoint1)
        val xLine2 = Element.Line(center, xPoint2)
        val xPoint3 = intersectTwoPointsOther(circle, xLine1, xPoint1)
        val xPoint4 = intersectTwoPointsOther(circle, xLine2, xPoint2)
        val probeLineOpp = Element.Line(xPoint3, xPoint4)
        val pivotLine = Element.Line(center, probeLineIntercept)
        val pivotOppPoint = intersectOnePoint(pivotLine, probeLineOpp)
        val apexPoint = intersectOnePoint(xLine1, line)
        val adjacentLine = Element.Line(pivotOppPoint, apexPoint)
        val farPoint = intersectOnePoint(adjacentLine, probeLine)
        val probeLineOppIntercept = intersectOnePoint(probeLineOpp, line)
        val crossLine = Element.Line(farPoint, probeLineOppIntercept)
        val middlePoint = intersectOnePoint(crossLine, pivotLine)
        val otherLine = Element.Line(middlePoint, apexPoint)
        val nextPoint = intersectOnePoint(otherLine, probeLineOpp)
        val apexPointOpp = intersectOnePoint(xLine2, line)
        val line1 = Element.Line(apexPointOpp, nextPoint)
        val parallelPoint = intersectOnePoint(line1, pivotLine)
        val parallelLine = Element.Line(parallelPoint, xPoint2)
        val symmetricalPoint = intersectTwoPointsOther(circle, parallelLine, xPoint2)
        val symmetricalLine = Element.Line(symmetricalPoint, middlePoint)
        val topPoint = intersectOnePoint(symmetricalLine, probeLine)
        val solutionLine = Element.Line(topPoint, center)

        val solutionContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(50.0)),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElements(
            listOf(
                probeLine,
                xLine1,
                xLine2,
                probeLineOpp,
                pivotLine,
                adjacentLine,
                crossLine,
                otherLine,
                line1,
                parallelLine,
                symmetricalLine
            )
        )

        dumpSolution(solutionContext)
        assertTrue(solutionContext.nexts().any { it.hasElement(solutionLine) })
    }

    private fun intersectOnePoint(element1: Element, element2: Element, name: String? = null): Point =
        when (val i = intersect(element1, element2)) {
            is Intersection.OnePoint -> i.point.copy(name = name)
            else -> error("One intersection point expected: $i")
        }

    private fun intersectTwoPoints(
        element1: Element,
        element2: Element,
        name1: String? = null,
        name2: String? = null
    ): Pair<Point, Point> =
        when (val i = intersect(element1, element2)) {
            is Intersection.TwoPoints -> Pair(i.point1.copy(name = name1), i.point2.copy(name = name2))
            else -> error("Two intersection points expected: $i")
        }

    private fun intersectTwoPointsOther(
        element1: Element,
        element2: Element,
        point1: Point,
        name: String? = null
    ): Point {
        val intersection = intersect(element1, element2)
        val points = intersection.points().filter { point2 -> !coincides(point1, point2) }
        return when (points.size) {
            1 -> points.first().copy(name = name)
            else -> error("Expected one point other than $point1: $intersection")
        }
    }

    @Test
    fun puzzle15_7_stages() {
        // Drop a Perpendicular**
        // (lines only)
        val solution = Element.Line(Point(0.0, 0.0), Point(0.0, 1.0))
        val solutionContext = solve(puzzle15_7_par_stage(), 5) { context ->
            context.hasElement(solution)
        }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_par() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a parallel line
        val solutionContext = puzzle15_7_par_stage()
        dumpSolution(solutionContext)
    }

    private fun puzzle15_7_par_stage(): EuclideaContext {
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 6) { context ->
            context.elements.any {
                it !== circle && it !== line && it !== probeLine && intersect(
                    line,
                    it
                ) == Intersection.Disjoint
            }
        }
        return solutionContext!!
    }

    @Test
    fun puzzle15_8() {
        // Line-Circle Intersection*
        // (circles only)
        val center = Point(0.0, 0.0)
        val circle = Element.Circle(center, 1.0)
        val point = Point(-2.542897, 0.0)
        val initialContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false),
            points = listOf(center, point),
            elements = listOf(circle)
        )
        // Check for either solution point, or a useful waypoint
        val solutionKnots = listOf(-1.0, 1.0, -2.0, 2.0)
        fun checkSolution(a: Double, b: Double): Boolean {
            return coincides(a, 0.0) && solutionKnots.any { coincides(it, b) }
        }
        // 6 - nothing
        // 7 - coincidence? (~3 hours)
        val solutionContext = solve(initialContext, 6) { context ->
            context.points.any { checkSolution(it.y, it.x) }
        }
        dumpSolution(solutionContext)
    }

    private fun dumpSolution(solutionContext: EuclideaContext?) {
        println(solutionContext)
        solutionContext?.let { printSteps(it) }
    }

    private class Labeler<T : HasName>(val prefix: String) {
        private val tags = mutableMapOf<T, Int>()
        private var nextTag = 1

        fun label(item: T, newAction: ((String) -> Unit)? = null): String {
            return when (val tag = tags[item]) {
                null -> {
                    val newTag = nextTag++
                    tags[item] = newTag
                    val label = labelFor(newTag, item.name)
                    newAction?.let { it(label) }
                    label
                }
                else -> labelFor(tag, item.name)
            }
        }

        private fun labelFor(tag: Int, name: String?): String {
            return "${prefix}${tag}${name?.let { "_${it}" } ?: ""}"
        }
    }

    private fun printSteps(context: EuclideaContext) {
        with(context) {
            object {
                val pointLabeler = Labeler<Point>("point")
                val circleLabeler = Labeler<Element.Circle>("circle")
                val lineLabeler = Labeler<Element.Line>("line")

                fun elementLabel(element: Element): String {
                    return when (element) {
                        is Element.Circle -> explainCircle(element)
                        is Element.Line -> explainLine(element)
                    }
                }

                fun explainPoint(point: Point): String {
                    return pointLabeler.label(point) { pointLabel ->
                        val coordinates = "(${point.x}, ${point.y})"
                        when (val pair = pointSource[point]) {
                            null -> println("$pointLabel at $coordinates")
                            else -> {
                                val elementLabel1 = elementLabel(pair.first)
                                val elementLabel2 = elementLabel(pair.second)
                                println("$pointLabel at intersection of $elementLabel1 and $elementLabel2 $coordinates")
                            }
                        }
                    }
                }

                fun explainCircle(circle: Element.Circle): String {
                    val centerLabel = explainPoint(circle.center)
                    return circleLabeler.label(circle) { circleLabel ->
                        when (val sample = circle.sample) {
                            null -> println("$circleLabel with center $centerLabel and radius ${circle.radius}")
                            else -> {
                                val sampleLabel = explainPoint(sample)
                                println("$circleLabel with center $centerLabel extending to $sampleLabel")
                            }
                        }
                    }
                }

                fun explainLine(line: Element.Line): String {
                    val point1Label = explainPoint(line.point1)
                    val point2Label = explainPoint(line.point2)
                    return lineLabeler.label(line) { lineLabel ->
                        println("$lineLabel from $point1Label to $point2Label")
                    }
                }

                fun explainSteps() {
                    for (element in elements) {
                        when (element) {
                            is Element.Circle -> explainCircle(element)
                            is Element.Line -> explainLine(element)
                        }
                    }
                }
            }.explainSteps()
        }
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
                    sub(next, nextDepth)?.let { return@sub it }
            return null
        }
        if (check(initialContext))
            return initialContext
        return sub(initialContext, 0)
    }
}
