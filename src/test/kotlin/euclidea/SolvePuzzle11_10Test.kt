package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle11_10Test {
    // Ratio 1 to 5

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Line segment in puzzle, but assume first step is to fill in the line
        Solver().improveSolution(4, 4)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val probe1: Point,
        val probeScale: Double
    ) {
        val probe2 = baseO + (baseA - baseO) * probeScale
    }

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                probe1 = Point(0.6, 0.3),
                probeScale = 0.5623
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0011, 0.0133),
                probe1 = Point(0.6001, 0.3022),
                probeScale = 0.5623111
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    // Line segment in puzzle, but assume first step is to fill in the line
                    // val base = Element.Line(baseO, baseA, limit1 = true, limit2 = true)
                    val base = Element.Line(baseO, baseA)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(baseO, baseA/*, probe1, probe2*/),
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
                val checkSolutionPoint = baseO + (baseA - baseO) * (1.0 / 6.0)
                return { context ->
                    context.hasPoint(checkSolutionPoint)
                }
            }
        }
    }
}
