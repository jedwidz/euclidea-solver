package euclidea

import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle10_2Test {
    // Outer Tangent

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // No solution ~17 min
        Solver().improveSolution(6, 6)
    }

    data class Params(
        val centerA: Point,
        val radiusA: Double,
        val centerB: Point,
        val radiusB: Double,
        val probe1: Point,
        val probe2: Point
    )

    data class Setup(
        val circleA: Element.Circle,
        val circleB: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                radiusA = 0.2,
                centerB = Point(1.0, 0.0),
                radiusB = 0.35,
                probe1 = Point(0.0, 0.12),
                probe2 = Point(1.0, 0.16)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                radiusA = 0.201234,
                centerB = Point(1.0011, 0.0133),
                radiusB = 0.3534,
                probe1 = Point(0.0, 0.12001),
                probe2 = Point(1.0011, 0.1601)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circleA = Element.Circle(centerA, radiusA)
                    val circleB = Element.Circle(centerB, radiusB)
                }
                namer.nameReflected(context)
                with(context) {
                    val probeLine = lineTool(probe1, probe2)
                    val probeA = intersectTwoPoints(probeLine, circleA).first
                    val probeB = intersectTwoPoints(probeLine, circleB).first
                    return Setup(circleA, circleB) to EuclideaContext(
                        config = EuclideaConfig(
                            // limited by 6L hint
                            perpendicularBisectorToolEnabled = true,
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(centerA, centerB, probeA, probeB/*probe1, probe2*/),
                        elements = listOf(circleA, circleB)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(setup) {
                return { context ->
                    context.elements.any {
                        it is Element.Line && meetAtOnePoint(it, circleA) && meetAtOnePoint(
                            it,
                            circleB
                        )
                    }
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isLineFromPerpendicularBisector
                    2 -> !element.isLineFromPerpendicularBisector
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isLineFromLine
                    else -> false
                }
            }
        }
    }
}
