package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle12_1Test {
    // Triangle by Midpoints

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 2 min 24 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 8,
//            nonNewElementLimit = 7,
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

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.4, 0.7),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.01, 0.0),
                baseC = Point(0.401, 0.7005),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext(
                    config = EuclideaConfig(
                        maxSqDistance = sq(10.0),
                    ),
                    points = listOf(baseA, baseB, baseC/*, probe1, probe2*/),
                    elements = listOf()
                )
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
                    // Sub-optimal 6L solution
                    val lineAB = lineTool(baseA, baseB)
                    val lineBC = lineTool(baseB, baseC)
                    val lineCA = lineTool(baseC, baseA)
                    val solutionA = parallelTool(lineBC, baseA, probe = baseB)
                    val solutionB = parallelTool(lineCA, baseB, probe = baseC)
                    val solutionC = parallelTool(lineAB, baseC, probe = baseA)

                    return listOf(solutionA, solutionB, solutionC)
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

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 8E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isCircleFromCircle
//                    1 -> !element.isCircleFromCircle
//                    2 -> !element.isCircleFromCircle
//                    3 -> !element.isCircleFromCircle
//                    4 -> !element.isLineFromLine
//                    5 -> !element.isCircleFromCircle
//                    6 -> !element.isLineFromLine
//                    7 -> !element.isLineFromLine
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
                    @Suppress("unused") val context = object {
                        // Sub-optimal 6L solution
                        val lineAB = lineTool(baseA, baseB)
                        val lineBC = lineTool(baseB, baseC)
                        val lineCA = lineTool(baseC, baseA)
                        val solutionA = parallelTool(lineBC, baseA, probe = baseB)
                        val solutionB = parallelTool(lineCA, baseB, probe = baseC)
                        val solutionC = parallelTool(lineAB, baseC, probe = baseA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal5LSolution)
        }

        fun optimal5LSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 5L solution
                        val bisectBC = perpendicularBisectorTool(baseB, baseC)
                        val solutionA = perpendicularTool(bisectBC, baseA)
                        val measure = nonCollapsingCompassTool(baseB, baseC, baseA)
                        val aim = intersectTwoPoints(measure, solutionA)
                        val aimB = aim.second
                        val aimC = aim.first
                        val solutionB = lineTool(baseB, aimB)
                        val solutionC = lineTool(baseC, aimC)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
