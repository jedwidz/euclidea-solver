package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle12_9Test {
    // Hypotenuse and Leg

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 2 hr 39 min
        Solver().improveSolution(
            maxExtraElements = 4,
            maxDepth = 9,
            maxNonNewElements = 5,
            maxConsecutiveNonNewElements = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val baseC: Point,
        val baseD: Point
    )

    data class Setup(
        val hypotenuse: Element.Line,
        val leg: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(1.12, 0.5),
                baseD = Point(0.8, 0.55)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(1.122, 0.5511),
                baseD = Point(0.813, 0.5533)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val hypotenuse = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val leg = Element.Line(baseC, baseD, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(hypotenuse, leg) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
//                            nonCollapsingCompassToolEnabled = true
                        ),
                        points = listOf(baseA, baseB, baseC, baseD),
                        elements = listOf(hypotenuse, leg)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            val solutionElements = solution.elements

            // Validate solution
            val expectedLength = setup.leg.distance()
            val actualLength = distance(params.baseB, solution.apex)
            assertTrue(coincides(expectedLength, actualLength))

            return { context ->
                context.hasElements(solutionElements)
            }
        }

        data class Solution(
            val solutionA: Element.Line,
            val solutionB: Element.Line,
            val apex: Point
        ) {
            val elements = listOf(solutionA, solutionB)
        }

        private fun constructSolution(params: Params): Solution {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Optimal 5L solution
                    val bisectAB = perpendicularBisectorTool(baseA, baseB)
                    val midAB = intersectOnePoint(bisectAB, hypotenuse)
                    val spanAB = circleTool(midAB, baseA)
                    val measure = nonCollapsingCompassTool(baseC, baseD, baseB)
                    val apex = intersectTwoPoints(measure, spanAB).second
                    val solutionA = lineTool(apex, baseA)
                    val solutionB = lineTool(apex, baseB)

                    return Solution(solutionA, solutionB, apex)
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
            val solution = constructSolution(params)
            val solutionElements = solution.elements
            return { context ->
                val elementsRemaining = solutionElements.count { !context.hasElement(it) }
                // Note hint has the second-last element being a circle
                when (elementsRemaining) {
                    0 -> 0
                    1 -> 1
                    else -> 3
                }
            }
        }

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 9E E-star moves hint
            return listOf(
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
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
                        // Optimal 5L solution
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        val midAB = intersectOnePoint(bisectAB, hypotenuse)
                        val spanAB = circleTool(midAB, baseA)
                        val measure = nonCollapsingCompassTool(baseC, baseD, baseB)
                        val apex = intersectTwoPoints(measure, spanAB).second
                        val solutionA = lineTool(apex, baseA)
                        val solutionB = lineTool(apex, baseB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::suboptimal10ESolution)
        }

        fun suboptimal10ESolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Sub-optimal 10E solution
                        val circleBD = circleTool(baseB, baseD)
                        val circleDB = circleTool(baseD, baseB)
                        val intersectBD = intersectTwoPoints(circleDB, circleBD)
                        val intersectBD1 = intersectBD.first
                        val intersectBD2 = intersectBD.second
                        val nccCDB1 = circleTool(intersectBD1, baseC)
                        val nccCDB2 = circleTool(intersectBD2, baseC)
                        val nccCDBAim = intersectTwoPointsOther(nccCDB1, nccCDB2, baseC)
                        val nccCDB = circleTool(baseB, nccCDBAim)
                        val cut = intersectOnePoint(nccCDB, hypotenuse)
                        val stretch = circleTool(cut, baseB)
                        val cut2 = intersectTwoPointsOther(stretch, hypotenuse, baseB)
                        val twice = circleTool(baseB, cut2)

                        val circleAB = circleTool(baseA, baseB)
                        val aimB = intersectTwoPoints(circleAB, twice).first
                        val solutionB = lineTool(baseB, aimB)
                        val aimA = intersectTwoPoints(nccCDB, solutionB).second
                        val solutionA = lineTool(baseA, aimA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
