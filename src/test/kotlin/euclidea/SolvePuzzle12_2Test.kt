package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test

class SolvePuzzle12_2Test {
    // Triangle by Side and Centroid

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 12 sec
        Solver().improveSolution(
            maxExtraElements = 4,
            maxDepth = 7,
//            maxNonNewElements = 6,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
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
        val lineAB: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.4, 0.15),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.01, 0.0),
                baseC = Point(0.401, 0.1505),
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
                    val lineAB = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineAB) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(baseA, baseB, baseC/*, probe1, probe2*/),
                        elements = listOf(lineAB)
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
                    // Sub-optimal 7L solution
                    val bisectAB = perpendicularBisectorTool(baseA, baseB)
                    val midAB = intersectOnePoint(bisectAB, lineAB)
                    val cross = lineTool(midAB, baseC)
                    val quarterL = perpendicularBisectorTool(midAB, baseA)
                    val quarterP = intersectOnePoint(quarterL, lineAB)
                    val cross2 = parallelTool(cross, quarterP, probe = midAB)
                    val lineBC = lineTool(baseB, baseC)
                    val aimA = intersectOnePoint(lineBC, cross2)
                    val solutionA = lineTool(baseA, aimA)
                    val aimB = intersectOnePoint(solutionA, cross)
                    val solutionB = lineTool(baseB, aimB)

                    return listOf(solutionA, solutionB)
                }
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Assumed partial solution, agreeing with hints
//                    @Suppress("unused") val context = object {
//                        val half = EuclideaTools.angleBisectorTool(baseA, baseO, baseB)
//                        // val perp = perpendicularTool(line2, sample, probe = baseO)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 7E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isLineFromLine
                    4 -> !element.isLineFromLine
                    5 -> !element.isCircleFromCircle
                    6 -> !element.isLineFromLine
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
                    @Suppress("unused") val context = object {
                        // Sub-optimal 7L solution
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        val midAB = intersectOnePoint(bisectAB, lineAB)
                        val cross = lineTool(midAB, baseC)
                        val quarterL = perpendicularBisectorTool(midAB, baseA)
                        val quarterP = intersectOnePoint(quarterL, lineAB)
                        val cross2 = parallelTool(cross, quarterP, probe = midAB)
                        val lineBC = lineTool(baseB, baseC)
                        val aimA = intersectOnePoint(lineBC, cross2)
                        val solutionA = lineTool(baseA, aimA)
                        val aimB = intersectOnePoint(solutionA, cross)
                        val solutionB = lineTool(baseB, aimB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal6LSolution)
        }

        fun optimal6LSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 6L solution
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        val midAB = intersectOnePoint(bisectAB, lineAB)
                        val cross = lineTool(midAB, baseC)
                        val circle1 = circleTool(baseC, midAB)
                        val doubled = intersectTwoPointsOther(circle1, cross, midAB)
                        val circle2 = circleTool(doubled, baseC)
                        val tripled = intersectTwoPointsOther(circle2, cross, baseC)
                        val solutionA = lineTool(baseA, tripled)
                        val solutionB = lineTool(baseB, tripled)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
