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
    fun puzzle15_7_par() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a parallel line
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
        dumpSolution(solutionContext)
    }

    private fun dumpSolution(solutionContext: EuclideaContext?) {
        println(solutionContext)
        solutionContext?.let { printSteps(it) }
    }

    private class Labeler<T>(val prefix: String) {
        private val tags = mutableMapOf<T, Int>()
        private var nextTag = 1

        fun label(item: T, newAction: ((String) -> Unit)? = null): String {
            return when (val tag = tags[item]) {
                null -> {
                    val newTag = nextTag++
                    tags[item] = newTag
                    val label = labelFor(newTag)
                    newAction?.let { it(label) }
                    label
                }
                else -> labelFor(tag)
            }
        }

        private fun labelFor(tag: Int): String {
            return prefix + tag
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