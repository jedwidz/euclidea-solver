package euclidea

import euclidea.EuclideaTools.circleTool
import org.junit.jupiter.api.Test

class SolvePuzzle15_1Test {
    // Midpoint**
    // (circles only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 6)
    }

    data class Params(
        val base1: Point,
        val base2: Point
    )

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(1.0, 0.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.01, 0.0),
                base2 = Point(0.02, 0.0)
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext(
                    config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(100.0)),
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
                val checkSolutionPoint = midpoint(base1, base2)
                return { context ->
                    context.hasPoint(checkSolutionPoint)
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
                // Optimal 6E solution
                val start1 = namer.set("start1", circleTool(base1, base2))
                val start2 = namer.set("start2", circleTool(base2, base1))
                val (up, down) = namer.setAll("up", "down", intersectTwoPoints(start1, start2))
                val top = namer.set("top", circleTool(up, down))
                val two = namer.set("two", intersectTwoPointsOther(start2, top, down))
                val big = namer.set("big", circleTool(two, base1))
                val (x1, x2) = namer.setAll("x1", "x2", intersectTwoPoints(big, start1))
                val target1 = namer.set("target1", circleTool(x1, base1))
                val target2 = namer.set("target2", circleTool(x2, base1))
                namer.set("solution", intersectTwoPointsOther(target1, target2, base1))

                return setup to initialContext.withElements(
                    listOf(
                        start1, start2, top, big, target1, target2
                    )
                )
            }
        }
    }
}
