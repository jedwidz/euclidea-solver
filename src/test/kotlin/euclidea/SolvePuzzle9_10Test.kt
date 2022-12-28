package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test

class SolvePuzzle9_10Test {
    // Triangle Mid-Segment

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(4, 5)
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val baseC: Point,
        val probe1Scale: Double,
        val probe2Scale: Double
    ) {
        val probe1 = baseC + (baseB - baseC) * probe1Scale
        val probe2 = baseB + (baseC - baseB) * probe2Scale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val line3: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.4, 0.8),
                probe1Scale = 0.25,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0143, 0.0134),
                baseC = Point(0.4043, 0.8123),
                probe1Scale = 0.25013,
                probe2Scale = 0.13513
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val line2 = Element.Line(baseA, baseC, limit1 = true, limit2 = true)
                    val line3 = Element.Line(baseB, baseC, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, line3) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
                            maxSqDistance = sq(50.0)
                        ),
                        points = listOf(baseA, baseB, baseC, probe1/*, probe2*/),
                        elements = listOf(line1, line2, line3)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            // cheekily use reference solution
            val solution = constructSolution(params)
            return { context ->
                coincides(context.elements.last(), solution)
            }
        }

        private fun constructSolution(params: Params): Element.Line {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Optimal 2L solution
                    val midL = perpendicularBisectorTool(baseB, baseC)
                    val midP = intersectOnePoint(midL, line3)
                    val solution = parallelTool(line1, midP)
                    return solution
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromLine
                    4 -> !element.isLineFromLine
                    else -> false
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
                with(setup) {
                    // Optimal 5E solution - looked up here 8-(:
                    // https://www.youtube.com/watch?v=cGHHMRz9eRc&ab_channel=DidaTaufiq
                    @Suppress("unused") val context = object {
                        val half = circleTool(probe1, baseC)
                        val fullP = intersectTwoPointsOther(half, line3, baseC)
                        val full = circleTool(fullP, baseC)
                        val target = intersectTwoPoints(full, line1)
                        val target1 = target.first
                        val target2 = target.second
                        val aim1 = lineTool(baseC, target1)
                        val aim2 = lineTool(baseC, target2)
                        val solution1 = intersectTwoPoints(aim1, half).second
                        val solution2 = intersectTwoPoints(aim2, half).second
                        val solution = lineTool(solution1, solution2)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
