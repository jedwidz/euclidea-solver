package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle9_7Test {
    // Minimum Perimeter - 2

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution not found ~4 min 35 sec
        Solver().improveSolution(4, 8)
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
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.01, 0.0),
                baseC = Point(0.401, 0.8005),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val line2 = Element.Line(baseB, baseC, limit1 = true, limit2 = true)
                    val line3 = Element.Line(baseC, baseA, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, line3) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(15.0)
                        ),
                        points = listOf(baseA, baseB, baseC/*, probe1, probe2*/),
                        elements = listOf(line1, line2, line3)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            return { context ->
                context.hasElements(solution)
            }
        }

        private fun constructSolution(params: Params): List<Element.Line> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // From optimal 6L solution
                    val perpB = perpendicularTool(line3, baseB)
                    val solutionB = intersectOnePoint(perpB, line3)
                    val perpC = perpendicularTool(line1, baseC)
                    val solutionC = intersectOnePoint(perpC, line1)
                    val center = intersectOnePoint(perpB, perpC)
                    val cross = lineTool(baseA, center)
                    val solutionA = intersectOnePoint(cross, line2)
                    val solution1 = lineTool(solutionA, solutionB)
                    val solution2 = lineTool(solutionB, solutionC)
                    val solution3 = lineTool(solutionC, solutionA)
                    return listOf(solution1, solution2, solution3)
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 5E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isCircleFromCircle
//                    1 -> !element.isCircleFromCircle
//                    2 -> !element.isLineFromLine
//                    3 -> !element.isLineFromLine
//                    4 -> !element.isLineFromLine
//                    else -> false
//                }
//            }
//        }

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Optimal 6L solution
                    @Suppress("unused") val context = object {
                        val perpB = perpendicularTool(line3, baseB)
                        val solutionB = intersectOnePoint(perpB, line3)
                        val perpC = perpendicularTool(line1, baseC)
                        val solutionC = intersectOnePoint(perpC, line1)
                        val center = intersectOnePoint(perpB, perpC)
                        val cross = lineTool(baseA, center)
                        val solutionA = intersectOnePoint(cross, line2)
                        val solution1 = lineTool(solutionA, solutionB)
                        val solution2 = lineTool(solutionB, solutionC)
                        val solution3 = lineTool(solutionC, solutionA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
