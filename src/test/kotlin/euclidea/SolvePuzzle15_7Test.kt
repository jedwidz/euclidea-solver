package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle15_7Test {
    // Drop a Perpendicular**
    // (lines only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Starting point includes initial probe line
        // Solution is detected as any novel point on the solution line (one step less than the solution line itself)
        Solver().improveSolution(1, 10 - 1 - 1)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val center: Point,
        val radius: Double,
        val probePoint1: Point,
        val probePoint2: Point
    )

    data class Setup(
        val circle: Element.Circle,
        val line: Element.Line,
        val probeLine: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(namer: Namer): Params {
            return Params(
                base1 = namer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.0, 0.0)),
                center = namer.set("center", Point(0.0, 2.0)),
                radius = 1.0,
                probePoint1 = namer.set("probe1", Point(-0.943215, 0.1)),
                probePoint2 = namer.set("probe2", Point(-0.828934, 3.0))
            )
        }

        override fun makeReplayParams(namer: Namer): Params {
            return Params(
                base1 = namer.set("base1", Point(0.01, 0.0)),
                base2 = namer.set("base2", Point(1.0, 0.1)),
                center = namer.set("center", Point(0.02, 2.0)),
                radius = 1.0124,
                probePoint1 = namer.set("probe1", Point(-0.943215, 0.0)),
                probePoint2 = namer.set("probe2", Point(-0.828934, 3.0))
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val circle = namer.set("circle", Element.Circle(center, radius))
                val line = namer.set("line", Element.Line(base1, base2))
                val baseContext = EuclideaContext(
                    config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(100.0)),
                    points = listOf(center),
                    elements = listOf(circle, line)
                )
                // 'probe' line to cut across the circle and line.
                val probeLine = namer.set("probe", Element.Line(probePoint1, probePoint2))
                val probeLineContext = baseContext.withElement(probeLine)
                return Pair(Setup(circle, line, probeLine), probeLineContext)
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                val base = lineTool(base1, base2)!!
                val checkSolutionLine = perpendicularTool(base, center)!!
                return { context ->
                    context.points.any { point ->
                        pointAndLineCoincide(point, checkSolutionLine) && !coincides(point, center) && !coincides(
                            point,
                            base1
                        ) && !coincides(point, base2)
                    }
                }
            }
        }

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Optimal 10E solution
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

                    val bracketPoint1 =
                        namer.set("bracket1", intersectTwoPointsOther(circle, pincerLine1, pivotCirclePoint1))
                    val crossLine1 = namer.set("cross1", Element.Line(bracketPoint1, pivotCirclePoint2))

                    val bracketPoint2 =
                        namer.set("bracket2", intersectTwoPointsOther(circle, pincerLine2, pivotCirclePoint2))
                    val crossLine2 = namer.set("cross2", Element.Line(bracketPoint2, pivotCirclePoint1))

                    val topPoint = namer.set("top", intersectOnePoint(crossLine1, crossLine2))
                    val pegPoint2 = namer.set("peg2", intersectOnePoint(line, xLine1))
                    val pegLine2 = namer.set("peg2", Element.Line(topPoint, pegPoint2))
                    val solutionPoint = namer.set("solution", intersectOnePoint(probeLine, pegLine2))
                    val solutionLine = namer.set("solution", Element.Line(solutionPoint, center))

                    val solutionContext = initialContext.withElements(
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
                    return Pair(setup, solutionContext)
                }
            }
        }
    }
}
