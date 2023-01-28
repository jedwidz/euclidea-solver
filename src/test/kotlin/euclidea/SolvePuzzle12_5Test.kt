package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle12_5Test {
    // Triangle by Excenters

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 1 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 9,
            maxNonNewElements = 4,
            maxConsecutiveNonNewElements = 3,
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
                baseB = Point(0.6, 0.0),
                baseC = Point(0.31, -0.42),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(0.6, 0.0),
                baseC = Point(0.3101, -0.4207),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext.of(
                    config = EuclideaConfig(
                        maxSqDistance = sq(10.0),
                        perpendicularBisectorToolEnabled = true
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
                    // Optimal 9L (but sub-optimal 15E) solution
                    val lineAB = lineTool(baseA, baseB)
                    val lineBC = lineTool(baseB, baseC)
                    val lineCA = lineTool(baseC, baseA)
                    val perpB = perpendicularTool(lineCA, baseB, probe = baseA)
                    val perpA = perpendicularTool(lineBC, baseA, probe = baseB)
                    val perpC = perpendicularTool(lineAB, baseC, probe = baseA)
                    val oppB = intersectOnePoint(perpB, lineCA)
                    val oppA = intersectOnePoint(perpA, lineBC)
                    val oppC = intersectOnePoint(perpC, lineAB)
                    val solutionA = lineTool(oppB, oppC)
                    val solutionB = lineTool(oppA, oppC)
                    val solutionC = lineTool(oppA, oppB)

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

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 9L/11E L/E-star moves hint
            return listOf(
                EuclideaTool.LineTool,
                EuclideaTool.LineTool,
                EuclideaTool.LineTool,
                EuclideaTool.PerpendicularBisectorTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.LineTool
            )
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
                        // Optimal 9L (but sub-optimal 15E) solution
                        val lineAB = lineTool(baseA, baseB)
                        val lineBC = lineTool(baseB, baseC)
                        val lineCA = lineTool(baseC, baseA)
                        val perpB = perpendicularTool(lineCA, baseB, probe = baseA)
                        val perpA = perpendicularTool(lineBC, baseA, probe = baseB)
                        val perpC = perpendicularTool(lineAB, baseC, probe = baseA)
                        val oppB = intersectOnePoint(perpB, lineCA)
                        val oppA = intersectOnePoint(perpA, lineBC)
                        val oppC = intersectOnePoint(perpC, lineAB)
                        val solutionA = lineTool(oppB, oppC)
                        val solutionB = lineTool(oppA, oppC)
                        val solutionC = lineTool(oppA, oppB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::suboptimal11ESolution)
//        }
//
//        fun suboptimal11ESolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Sub-optimal 11E solution
//                        val circleCB = circleTool(baseC, baseB)
//                        val circleBC = circleTool(baseB, baseC)
//                        val intersectBC = intersectTwoPoints(circleBC, circleCB)
//                        val intersectBC1 = intersectBC.first
//                        val intersectBC2 = intersectBC.second
//                        val bisectBC = lineTool(intersectBC1, intersectBC2)
//
//                        val circleAB = circleTool(baseA, baseB)
//                        val circleBA = circleTool(baseB, baseA)
//                        val intersectAB = intersectTwoPoints(circleBA, circleAB)
//                        val intersectAB1 = intersectAB.first
//                        val intersectAB2 = intersectAB.second
//                        val bisectAB = lineTool(intersectAB1, intersectAB2)
//
//                        val center = intersectOnePoint(bisectAB, bisectBC)
//                        val radialB = lineTool(center, baseB)
//
//                        val aim = intersectTwoPoints(radialB, circleCB).first
//                        val cross = lineTool(aim, baseC)
//                        val aimB = intersectTwoPointsOther(cross, circleCB, aim)
//                        val solutionB = lineTool(aimB, baseB)
//
//                        val aimC = intersectOnePoint(solutionB, bisectBC)
//                        val solutionC = lineTool(aimC, baseC)
//
//                        val aimA = intersectOnePoint(solutionB, bisectAB)
//                        val solutionA = lineTool(aimA, baseA)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
