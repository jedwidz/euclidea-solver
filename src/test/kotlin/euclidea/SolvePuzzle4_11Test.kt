package euclidea

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle4_11Test {
    // Square by Two Vertices
    // (circles only)

    @Test
    fun puzzle4_11_check_solution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun puzzle4_11_improve_solution() {
        Solver().improveSolution()
    }

    class Solver {

        data class Params(
            val base1: Point,
            val base2: Point
        )

        fun checkReferenceSolution() {
            // Reproduce and check my best solution so far
            val namer = Namer()
            val params = Params(
                base1 = namer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.0, 0.0))
            )
            val solutionContext =
                referenceSolution(params, namer)

            dumpSolution(solutionContext, namer)
            assertTrue { isSolution(params).invoke(solutionContext) }
        }

        fun improveSolution() {
            val namer = Namer()
            val params = Params(
                base1 = namer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.0, 0.0))
            )
            val startingContext =
                initialContext(params, namer)

            val sampleSolutionContext =
                referenceSolution(params, namer)

            val isSolution = isSolution(params)

            val replayNamer = Namer()
            val replayParams = Params(
                base1 = replayNamer.set("base1", Point(0.0, 0.0)),
                base2 = namer.set("base2", Point(1.01983, 0.0011))
            )
            val replayInitialContext = initialContext(
                replayParams,
                replayNamer
            )

            val isReplaySolution = isSolution(replayParams)

            fun checkSolution(context: EuclideaContext): Boolean {
                return try {
                    val replaySolutionContext =
                        replaySteps(context, replayInitialContext)
                    isReplaySolution(replaySolutionContext)
                } catch (e: IllegalStateException) {
                    // Failed replay
                    false
                }
            }

            assertTrue(isSolution(sampleSolutionContext))

            val maxExtraElements = 2
            val initialSameElements = 0
            val solutionContext = solve(startingContext, 7, prune = { next ->
                val extraElements = next.elements.count { !sampleSolutionContext.hasElement(it) }
                extraElements > maxExtraElements || next.elements.size <= initialSameElements && extraElements > 0
            }) { context ->
                isSolution(context) && checkSolution(context)
            }
            dumpSolution(solutionContext, namer)
            println("Count: ${solutionContext?.elements?.size}")
        }

        private fun isSolution(
            params: Params
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

        private fun referenceSolution(
            params: Params,
            namer: Namer
        ): EuclideaContext {
            val initialContext = initialContext(
                params, namer
            )
            with(params) {
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

                val solutionContext = initialContext.withElements(
                    listOf(
                        start1, start2, triangle, big, big1, focusC, finalC
                    )
                )
                return solutionContext
            }
        }

        private fun initialContext(
            params: Params,
            namer: Namer
        ): EuclideaContext {
            with(params) {
                val baseContext = EuclideaContext(
                    config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(5.0)),
                    points = listOf(base1, base2),
                    elements = listOf()
                )
                return baseContext
            }
        }
    }
}
