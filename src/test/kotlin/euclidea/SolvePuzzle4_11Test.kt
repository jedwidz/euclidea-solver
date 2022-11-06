package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle4_11Test {
    // Square by Two Vertices
    // (circles only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(2, 7)
    }

    data class Params(
        val base1: Point,
        val base2: Point
    )

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(namer: Namer): Params {
            return Params(
                base1 = namer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.0, 0.0))
            )
        }

        override fun makeReplayParams(namer: Namer): Params {
            return Params(
                base1 = namer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.01983, 0.0011))
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext(
                    config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(5.0)),
                    points = listOf(base1, base2),
                    elements = listOf()
                )
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                val diff = base2 - base1
                // Arbitrary choice of one of two axis-aligned squares (the 'diagonal square' seems unlikely to be optimal)
                val solution1 = Point(base1.x + diff.y, base1.y - diff.x)
                val solution2 = solution1.plus(diff)
                return { context ->
                    context.hasPoint(solution1) && context.hasPoint(solution2)
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
                // Optimal 7E solution
                val start1 = namer.set("start1", EuclideaTools.circleTool(base1, base2)!!)
                val start2 = namer.set("start2", EuclideaTools.circleTool(base2, base1)!!)
                val (adj1, adj2) = namer.setAll("adj1", "adj2", intersectTwoPoints(start1, start2))
                val triangle = namer.set("triangle", EuclideaTools.circleTool(adj1, base1)!!)
                val big = namer.set("big", EuclideaTools.circleTool(adj2, adj1)!!)
                val big1P = namer.set("big1P", intersectTwoPointsOther(triangle, start2, base1))
                val big1 = namer.set("big1", EuclideaTools.circleTool(big1P, base1)!!)
                val focus = namer.set("focus", intersectAnyPoint(big, big1))
                val focusC = namer.set("focusC", EuclideaTools.circleTool(base2, focus)!!)
                val finalP = namer.set("finalP", intersectAnyPoint(start1, focusC))
                val finalC = namer.set("finalC", EuclideaTools.circleTool(adj2, finalP)!!)

                // namer.setAll("solution1", "solution2", intersectTwoPoints(finalC, circle))

                return setup to initialContext.withElements(
                    listOf(
                        start1, start2, triangle, big, big1, focusC, finalC
                    )
                )
            }
        }
    }
}
