package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle10_4Test {
    // Rotation 90deg

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(5, 6)
    }

    data class Params(
        val center: Point,
        val point1A: Point,
        val point1B: Point
    )

    data class Setup(
        // actually the solution...
        val point2A: Point,
        val point2B: Point,
        val line1: Element.Line,
        val line2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                point1A = Point(-0.4, -0.22),
                point1B = Point(-0.2, 0.12)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                point1A = Point(-0.4142, -0.2234),
                point1B = Point(-0.2343, 0.12124)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                fun rotate(point: Point) = rotatePoint(center, point, 90.0)
                val context = object {
                    val line1 = Element.Line(point1A, point1B, limit1 = true, limit2 = true)
                    val point2A = rotate(point1A)
                    val point2B = rotate(point1B)
                    val line2 = Element.Line(point2A, point2B, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(point2A, point2B, line1, line2) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(5.0),
                            perpendicularBisectorToolEnabled = true
                        ),
                        points = listOf(center, point1A, point1B),
                        elements = listOf(line1)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            return { context ->
                with(setup) {
                    context.hasElement(line2) && context.hasPoint(point2A) && context.hasPoint(point2B)
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
//                    0 -> !(element.isLineFromLine && coincides(element, lineTool(params.centerA, params.centerB)))
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isLineFromLine
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isLineFromPerpendicularBisector
                    else -> false
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
                    // Optimal 5L solution
                    @Suppress("unused") val context = object {
                        val circleB = circleTool(center, point1B)
                        val lineB = lineTool(center, point1B)
                        val perpB = perpendicularTool(lineB, center)
                        val solutionPointB = intersectTwoPoints(perpB, circleB).first
                        val solution = perpendicularTool(line1, solutionPointB)
                        val circleA = circleTool(center, point1A)
                        val solutionPointA = intersectTwoPoints(solution, circleA).second
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
