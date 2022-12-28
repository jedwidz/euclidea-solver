package euclidea

import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle11_4Test {
    // Angle of 54 degrees

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // 5L solution found ~8 sec
        Solver().improveSolution(4, 5)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val probe: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0011, 0.0133),
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = Element.Line(baseO, baseA, limit1 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0),
                            perpendicularBisectorToolEnabled = true,
                            angleBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true
                        ),
                        points = listOf(baseO, baseA /*, probe*/),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                // arbitrary choice of two possible solutions
                val solP = rotatePoint(baseO, baseA, 54.0)
                val solution = lineTool(baseO, solP)
                return { context ->
                    context.hasElement(solution)
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5L moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromPerpendicularBisector
                    1 -> !element.isCircleFromNonCollapsingCompass
                    2 -> !element.isCircleFromNonCollapsingCompass
                    3 -> !element.isCircleFromNonCollapsingCompass
                    4 -> !element.isLineFromAngleBisector
                    else -> false
                }
            }
        }
    }
}
