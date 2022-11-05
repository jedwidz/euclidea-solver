package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle15_7Test {

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
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.0, 2.0))
        val radius = 1.0
        val probePoint1 = namer.set("probe1", Point(-0.943215, 0.0))
        val probePoint2 = namer.set("probe2", Point(-0.828934, 3.0))
        val solutionContext =
            puzzle15_7_solution10E(center, radius, basePoint, basePoint2, probePoint1, probePoint2, namer)

        dumpSolution(solutionContext, namer)
        val checkSolutionLine = Element.Line(center, basePoint)
        assertTrue(solutionContext.hasElement(checkSolutionLine))
    }

    @Test
    fun puzzle15_7_replay_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Test replaying a solution, with different starting points
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0134, 0.0134))
        val basePoint2 = namer.set("base2", Point(1.01111, 0.01134))
        val center = namer.set("center", Point(0.0343, 2.011))
        val radius = 1.01
        val probePoint1 = namer.set("probe1", Point(-0.9432151, 0.05))
        val probePoint2 = namer.set("probe2", Point(-0.8289344, 3.0555))
        val solutionContext =
            puzzle15_7_solution10E(center, radius, basePoint, basePoint2, probePoint1, probePoint2, namer)

        // Double-check that solution works
        val base = lineTool(basePoint, basePoint2)!!
        val checkSolutionLine = perpendicularTool(base, center)!!
        assertTrue(solutionContext.hasElement(checkSolutionLine))

        val replayNamer = Namer()
        val replayBasePoint = replayNamer.set("base", Point(0.0, 0.0))
        val replayBasePoint2 = replayNamer.set("base2", Point(1.0, 0.0))
        val replayCenter = replayNamer.set("center", Point(0.0, 2.0))
        val replayRadius = 1.0
        val replayProbePoint1 = replayNamer.set("probe1", Point(-0.943215, 0.0))
        val replayProbePoint = replayNamer.set("probe2", Point(-0.828934, 3.0))
        val (_, replayInitialContext) = puzzle15_7_probeLineContext(
            replayCenter,
            replayRadius,
            replayBasePoint,
            replayBasePoint2,
            replayProbePoint1,
            replayProbePoint,
            replayNamer
        )

        val replaySolutionContext =
            replaySteps(solutionContext, replayInitialContext)

        dumpSolution(replaySolutionContext, replayNamer)
        val replayBase = lineTool(replayBasePoint, replayBasePoint2)!!
        val replayCheckSolutionLine = perpendicularTool(replayBase, replayCenter)!!
        assertTrue(replaySolutionContext.hasElement(replayCheckSolutionLine))
    }

    @Test
    fun puzzle15_7_improve_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Try to improve on my best solution so far
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.0, 2.0))
        val radius = 1.0
        val (_, _, initialContext) = puzzle15_7_initialContext(center, radius, basePoint, basePoint2, namer)

        val probePoint1 = namer.set("probe1", Point(-0.943215, 0.1))
        val probePoint2 = namer.set("probe2", Point(-0.828934, 3.0))
        val sampleSolutionContext =
            puzzle15_7_solution10E(center, radius, basePoint, basePoint2, probePoint1, probePoint2, namer)

        val probeLine = namer.set("probe", Element.Line(probePoint1, probePoint2))
        val startingContext = initialContext.withElement(probeLine)

        val isSolution = puzzle15_7_isSolution(basePoint, basePoint2, center)

        val replayNamer = Namer()
        val replayBasePoint = replayNamer.set("base", Point(0.01, 0.0))
        val replayBasePoint2 = replayNamer.set("base2", Point(1.0, 0.1))
        val replayCenter = replayNamer.set("center", Point(0.02, 2.000))
        val replayRadius = 1.0124
        val replayProbePoint1 = replayNamer.set("probe1", Point(-0.943215, 0.0))
        val replayProbePoint = replayNamer.set("probe2", Point(-0.828934, 3.0))
        val (_, replayInitialContext) = puzzle15_7_probeLineContext(
            replayCenter,
            replayRadius,
            replayBasePoint,
            replayBasePoint2,
            replayProbePoint1,
            replayProbePoint,
            replayNamer
        )

        val isReplaySolution = puzzle15_7_isSolution(replayBasePoint, replayBasePoint2, replayCenter)

        fun checkSolution(context: EuclideaContext): Boolean {
            return try {
                val replaySolutionContext =
                    replaySteps(context, replayInitialContext)
                isReplaySolution(replaySolutionContext)
            } catch (e: IllegalStateException) {
                // Failed replay
                false
            }
        }

        assertTrue(isSolution(sampleSolutionContext))

        // Not expected to get a better solution than 10E
        val maxExtraElements = 3
        val solutionContext = solve(startingContext, 10 - 1 - 1, prune = { next ->
            next.elements.count { !sampleSolutionContext.hasElement(it) } > maxExtraElements
        }) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    fun puzzle15_7_isSolution(basePoint: Point, basePoint2: Point, center: Point): (EuclideaContext) -> Boolean {
        val base = lineTool(basePoint, basePoint2)!!
        val checkSolutionLine = perpendicularTool(base, center)!!
        return { context ->
            context.points.any { point ->
                pointAndLineCoincide(point, checkSolutionLine) && !coincides(point, center) && !coincides(
                    point,
                    basePoint
                ) && !coincides(point, basePoint2)
            }
        }
    }

    private fun puzzle15_7_solution10E(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        probePoint1: Point,
        probePoint2: Point,
        namer: Namer
    ): EuclideaContext {
        val (temp, probeLineContext) = puzzle15_7_probeLineContext(
            center,
            radius,
            basePoint,
            basePoint2,
            probePoint1,
            probePoint2,
            namer
        )
        val (circle, line, probeLine) = temp
        // Solution works regardless of point 'order' here
        val (xPoint1, xPoint2) = namer.setAll("x1", "x2", intersectTwoPoints(circle, probeLine))
        val xLine1 = namer.set("x1", Element.Line(center, xPoint1))
        val xPoint3 = namer.set("x3", intersectTwoPointsOther(circle, xLine1, xPoint1))
        val probeLineIntercept = namer.set("probeIntercept", intersect(line, probeLine).points().first())
        val pivotLine = namer.set("pivot", Element.Line(center, probeLineIntercept))
        val pegLine1 = namer.set("peg1", Element.Line(xPoint3, xPoint2))
        val pegPoint1 = namer.set("peg1", intersectOnePoint(line, pegLine1))
        val (pivotCirclePoint1, pivotCirclePoint2) = namer.setAll(
            "pivotCircle1",
            "pivotCircle2",
            intersectTwoPoints(circle, pivotLine)
        )
        val pincerLine1 = namer.set("pincer1", Element.Line(pivotCirclePoint1, pegPoint1))
        val pincerLine2 = namer.set("pincer2", Element.Line(pivotCirclePoint2, pegPoint1))

        val bracketPoint1 = namer.set("bracket1", intersectTwoPointsOther(circle, pincerLine1, pivotCirclePoint1))
        val crossLine1 = namer.set("cross1", Element.Line(bracketPoint1, pivotCirclePoint2))

        val bracketPoint2 = namer.set("bracket2", intersectTwoPointsOther(circle, pincerLine2, pivotCirclePoint2))
        val crossLine2 = namer.set("cross2", Element.Line(bracketPoint2, pivotCirclePoint1))

        val topPoint = namer.set("top", intersectOnePoint(crossLine1, crossLine2))
        val pegPoint2 = namer.set("peg2", intersectOnePoint(line, xLine1))
        val pegLine2 = namer.set("peg2", Element.Line(topPoint, pegPoint2))
        val solutionPoint = namer.set("solution", intersectOnePoint(probeLine, pegLine2))
        val solutionLine = namer.set("solution", Element.Line(solutionPoint, center))

        val solutionContext = probeLineContext.withElements(
            listOf(
                xLine1,
                pivotLine,
                pegLine1,
                pincerLine1,
                pincerLine2,
                crossLine1,
                crossLine2,
                pegLine2,
                solutionLine
            )
        )
        return solutionContext
    }

    private fun puzzle15_7_initialContext(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        namer: Namer
    ): Triple<Element.Circle, Element.Line, EuclideaContext> {
        val circle = namer.set("circle", Element.Circle(center, radius))
        val line = namer.set("base", Element.Line(basePoint, basePoint2))
        val baseContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(100.0)),
            points = listOf(center),
            elements = listOf(circle, line)
        )
        return Triple(circle, line, baseContext)
    }

    private fun puzzle15_7_probeLineContext(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        probePoint1: Point,
        probePoint2: Point,
        namer: Namer
    ): Pair<Triple<Element.Circle, Element.Line, Element.Line>, EuclideaContext> {
        val (circle, line, initialContext) = puzzle15_7_initialContext(center, radius, basePoint, basePoint2, namer)
        // 'probe' line to cut across the circle and line.
        val probeLine = namer.set("probe", Element.Line(probePoint1, probePoint2))
        val probeLineContext = initialContext.withElement(probeLine)
        return Pair(Triple(circle, line, probeLine), probeLineContext)
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
}
