package euclidea

import euclidea.EuclideaTools.circleTool
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
        Solver().improveSolution(0, 7, 4)
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
                base1 = Point(0.0, 0.0),
                base2 = Point(1.01983, 0.0011)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext.of(
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
                @Suppress("unused") val context = object {
                    val start1 = circleTool(base1, base2)
                    val start2 = circleTool(base2, base1)
                    val adj = intersectTwoPoints(start1, start2)
                    val adj1 = adj.first
                    val adj2 = adj.second
                    val triangle = circleTool(adj1, base1)
                    val big = circleTool(adj2, adj1)
                    val big1P = intersectTwoPointsOther(triangle, start2, base1)
                    val big1 = circleTool(big1P, base1)
                    val focus = intersectAnyPoint(big, big1)
                    val focusC = circleTool(base2, focus)
                    val finalP = intersectAnyPoint(start1, focusC)
                    val finalC = circleTool(adj2, finalP)
                    // val solution = intersectTwoPoints(finalC, circle)
                }
                namer.nameReflected(context)
                return setup to initialContext.withElements(elementsReflected(context))
            }
        }
    }
}
