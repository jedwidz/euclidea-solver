package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertTrue

class SolvePuzzle12_8Test {
    // Hypotenuse and Altitude

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // no solution found ?
        Solver().improveSolution(
            maxExtraElements = 6,
            maxDepth = 9,
            maxNonNewElements = 5,
            maxConsecutiveNonNewElements = 3,
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
        val altitude: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(1.1, 0.25),
                baseD = Point(0.9, 0.45)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(1.122, 0.2511),
                baseD = Point(0.913, 0.4533)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val hypotenuse = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val altitude = Element.Line(baseC, baseD, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(hypotenuse, altitude) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0),
                            nonCollapsingCompassToolEnabled = true
                        ),
                        points = listOf(baseA, baseB, baseC, baseD),
                        elements = listOf(hypotenuse, altitude)
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
            val expectedAltitude = setup.altitude.distance()
            val actualAltitude = distance(projection(setup.hypotenuse, solution.apex), solution.apex)
            assertTrue(coincides(expectedAltitude, actualAltitude))

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
                    // Optimal 6L solution
                    val bisectAB = perpendicularBisectorTool(baseA, baseB)
                    val midAB = intersectOnePoint(bisectAB, hypotenuse)
                    val spanAB = circleTool(midAB, baseA)
                    val measure = nonCollapsingCompassTool(baseC, baseD, midAB)
                    val top = intersectTwoPoints(measure, bisectAB).second
                    val roof = perpendicularTool(bisectAB, top, probe = baseC)
                    val apex = intersectTwoPoints(roof, spanAB).second
                    val solutionA = lineTool(apex, baseA)
                    val solutionB = lineTool(apex, baseB)

                    return Solution(solutionA, solutionB, apex)
                }
            }
        }

        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Assumed partial solution, agreeing with hints
                    @Suppress("unused") val context = object {
                        // Bisect base for starters?
                        val circleAB = circleTool(baseA, baseB)
                        val circleBA = circleTool(baseB, baseA)
                        val intersectAB = intersectTwoPoints(circleAB, circleBA)
                        val intersectAB1 = intersectAB.first
                        val intersectAB2 = intersectAB.second
                        val bisectAB = lineTool(intersectAB1, intersectAB2)
                        val midAB = intersectOnePoint(bisectAB, hypotenuse)

                        // Hint order is consistent with measuring from the center...
                        val circle = nonCollapsingCompassTool(baseC, baseD, midAB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solution = constructSolution(params)
            val solutionElements = solution.elements
            // Assume apex is found first
            val apex = solution.apex
            return { context ->
                val onPoint = context.elements.count { pointAndElementCoincide(apex, it) }
                // Need two elements to locate center, then the solution circle itself
                val pointRemaining = max(0, 2 - onPoint)
                val elementsRemaining = solutionElements.count { !context.hasElement(it) }
                pointRemaining + elementsRemaining
            }
        }

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 13E E-star moves hint
            return listOf(
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.NonCollapsingCompassTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
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
                        // Optimal 6L solution
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        val midAB = intersectOnePoint(bisectAB, hypotenuse)
                        val spanAB = circleTool(midAB, baseA)
                        val measure = nonCollapsingCompassTool(baseC, baseD, midAB)
                        val top = intersectTwoPoints(measure, bisectAB).second
                        val roof = perpendicularTool(bisectAB, top, probe = baseC)
                        val apex = intersectTwoPoints(roof, spanAB).second
                        val solutionA = lineTool(apex, baseA)
                        val solutionB = lineTool(apex, baseB)
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
