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
                @Suppress("unused") val context = object {
                    val start1 = circleTool(base1, base2)
                    val start2 = circleTool(base2, base1)
                    val upDown = intersectTwoPoints(start1, start2)
                    val up = upDown.first
                    val down = upDown.second
                    val top = circleTool(up, down)
                    val two = intersectTwoPointsOther(start2, top, down)
                    val big = circleTool(two, base1)
                    val x = intersectTwoPoints(big, start1)
                    val x1 = x.first
                    val x2 = x.second
                    val target1 = circleTool(x1, base1)
                    val target2 = circleTool(x2, base1)
                    val solution = intersectTwoPointsOther(target1, target2, base1)
                }
                namer.nameReflected(context)
                return setup to initialContext.withElements(elementsReflected(context))
            }
        }
    }
}
