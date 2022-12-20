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

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(1.0, 0.0),
                center = Point(0.0, 2.0),
                radius = 1.0,
                probePoint1 = Point(-0.943215, 0.1),
                probePoint2 = Point(-0.828934, 3.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.01, 0.0),
                base2 = Point(1.0, 0.1),
                center = Point(0.02, 2.0),
                radius = 1.0124,
                probePoint1 = Point(-0.943215, 0.0),
                probePoint2 = Point(-0.828934, 3.0)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = Element.Circle(center, radius)
                    val line = Element.Line(base1, base2)

                    // 'probe' line to cut across the circle and line.
                    val probeLine = Element.Line(probePoint1, probePoint2)
                }
                namer.nameReflected(context)
                with(context) {
                    val baseContext = EuclideaContext(
                        config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(100.0)),
                        points = listOf(center),
                        elements = listOf(circle, line)
                    )
                    val probeLineContext = baseContext.withElement(probeLine)
                    return Pair(Setup(circle, line, probeLine), probeLineContext)
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                val base = lineTool(base1, base2)
                val checkSolutionLine = perpendicularTool(base, center)
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
                    @Suppress("unused") val context = object {
                        // Solution works regardless of point 'order' here
                        val xPoint = intersectTwoPoints(circle, probeLine)
                        val xPoint1 = xPoint.first
                        val xPoint2 = xPoint.second
                        val xLine1 = Element.Line(center, xPoint1)
                        val xPoint3 = intersectTwoPointsOther(circle, xLine1, xPoint1)
                        val probeLineIntercept = intersect(line, probeLine).points().first()
                        val pivotLine = Element.Line(center, probeLineIntercept)
                        val pegLine1 = Element.Line(xPoint3, xPoint2)
                        val pegPoint1 = intersectOnePoint(line, pegLine1)
                        val pivotCirclePoint = intersectTwoPoints(circle, pivotLine)
                        val pivotCirclePoint1 = pivotCirclePoint.first
                        val pivotCirclePoint2 = pivotCirclePoint.second
                        val pincerLine1 = Element.Line(pivotCirclePoint1, pegPoint1)
                        val pincerLine2 = Element.Line(pivotCirclePoint2, pegPoint1)

                        val bracketPoint1 =
                            intersectTwoPointsOther(circle, pincerLine1, pivotCirclePoint1)
                        val crossLine1 = Element.Line(bracketPoint1, pivotCirclePoint2)

                        val bracketPoint2 =
                            intersectTwoPointsOther(circle, pincerLine2, pivotCirclePoint2)
                        val crossLine2 = Element.Line(bracketPoint2, pivotCirclePoint1)

                        val topPoint = intersectOnePoint(crossLine1, crossLine2)
                        val pegPoint2 = intersectOnePoint(line, xLine1)
                        val pegLine2 = Element.Line(topPoint, pegPoint2)
                        val solutionPoint = intersectOnePoint(probeLine, pegLine2)
                        val solutionLine = Element.Line(solutionPoint, center)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
