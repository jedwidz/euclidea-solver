package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle7_3Test {
    // Angle of 75 degrees

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 5)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val probe: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(2.0011, 0.01),
                probe = Point(0.1233, 0.88)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.043, 0.012),
                base2 = Point(2.011, 0.0001),
                probe = Point(0.1235, 0.86)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = lineTool(base1, base2)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(8.0)),
                        points = listOf(base1, probe),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutionElements = constructSolution(params)
            return { context ->
                context.hasElements(solutionElements)
            }
        }

        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
            val referenceElements = ElementSet()
            referenceElements += referenceSolution(params, Namer()).second.elements

            val solutionElements = ElementSet()
            solutionElements += constructSolution(params)

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                solutionScore * 10 + referenceScore * 4
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> element !is Element.Circle
                    1 -> element !is Element.Circle
                    2 -> element !is Element.Circle
                    3 -> element !is Element.Line
                    4 -> element !is Element.Line
                    else -> false
                }
            }
        }

        private fun constructSolution(params: Params): List<Element> {
            with(params) {
                // arbitrary choice of possible solutions
                val solP = rotatePoint(base1, base2, 75.0)
                val solution = lineTool(base1, solP)
                return listOf(solution)
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
                    // Optimal 5E solution
                    @Suppress("unused") val context = object {
                        val start = circleTool(probe, base1)
                        val cut = intersectTwoPointsOther(start, base, base1)
                        val lens = circleTool(cut, probe)
                        val adj1 = intersectTwoPoints(lens, start).second
                        val support = circleTool(base1, adj1)
                        val aimP1 = intersectTwoPoints(support, base).second
                        val aimP2 = intersectTwoPointsOther(support, start, adj1)
                        val aim = lineTool(aimP1, aimP2)
                        val solutionP = intersectTwoPointsOther(aim, start, aimP2)
                        val solution = lineTool(solutionP, base1)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
