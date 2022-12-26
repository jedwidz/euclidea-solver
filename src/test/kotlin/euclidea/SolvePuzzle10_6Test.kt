package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle10_6Test {
    // Segment Trisection

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found ~10 sec
        Solver().improveSolution(5, 5)
    }

    data class Params(
        val pointA: Point,
        val pointB: Point,
        val probe1: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.0, 0.0),
                probe1 = Point(0.9, 0.9)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.0043, 0.0141),
                probe1 = Point(0.9111, 0.922)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = Element.Line(pointA, pointB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0),
                            perpendicularBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
                            parallelToolEnabled = true,
                            angleBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true
                        ),
                        points = listOf(pointA, pointB/*, probe*/),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutionPoints = with(params) {
                val step = (pointB - pointA) * (1.0 / 3.0)
                listOf(1, 2).map { i -> pointA + step * i.toDouble() }
            }
            return { context -> context.hasPoints(solutionPoints) }
        }
    }
}
