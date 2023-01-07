package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle14_2Test {
    // Circle Tangent to Two Circles

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // no solution found 20 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 4,
            nonNewElementLimit = 2,
//            consecutiveNonNewElementLimit = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center1: Point,
        val sample1: Point,
        val center2: Point,
        val sample2: Point
    )

    data class Setup(
        val circle1: Element.Circle,
        val circle2: Element.Circle
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center1 = Point(0.0, 0.0),
                sample1 = Point(0.5, 0.0),
                center2 = Point(1.0, 4.0),
                sample2 = Point(0.9, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center1 = Point(0.0, 0.0),
                sample1 = Point(0.501, 0.002),
                center2 = Point(1.03, 4.04),
                sample2 = Point(0.91, 0.311)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle1 = circleTool(center1, sample1)
                    val circle2 = circleTool(center2, sample2)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle1, circle2) to EuclideaContext(
                        config = EuclideaConfig(
                            perpendicularBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
                            angleBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true,
                            parallelToolEnabled = true,
                            maxSqDistance = sq(10.0)
                        ),
                        points = listOf(center1, sample1, center2/*, sample2*/),
                        elements = listOf(circle1, circle2)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // Validate
            assertTrue(listOf(setup.circle1, setup.circle2).all { meetAtOnePoint(solution, it) })
            return { context ->
                context.hasElement(solution)
            }
        }

        private fun constructSolution(params: Params): Element.Circle {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 5L solution
                    val through = lineTool(center1, sample1)
                    val parallel = parallelTool(through, center2, probe = center1)
                    val same = intersectTwoPoints(parallel, circle2).second
                    val cross = lineTool(same, sample1)
                    val aim = intersectTwoPointsOther(cross, circle2, same)
                    val pin = lineTool(aim, center2)
                    val center = intersectOnePoint(pin, through)
                    val solution = circleTool(center, sample1)
                    return solution
                }
            }
        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solutionElements = constructSolution(params, setup).elements
//            return { context ->
//                solutionElements.count { !context.hasElement(it) }
//            }
//        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 4L L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromNonCollapsingCompass
                    2 -> !element.isLineFromPerpendicularBisector
                    3 -> !element.isCircleFromCircle
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
                        // Sub-optimal 5L solution
                        val through = lineTool(center1, sample1)
                        val parallel = parallelTool(through, center2, probe = center1)
                        val same = intersectTwoPoints(parallel, circle2).second
                        val cross = lineTool(same, sample1)
                        val aim = intersectTwoPointsOther(cross, circle2, same)
                        val pin = lineTool(aim, center2)
                        val center = intersectOnePoint(pin, through)
                        val solution = circleTool(center, sample1)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::optimal7LSolution)
//        }
//
//        private fun optimal7LSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 7L solution
//                        val circleBack = circleTool(sample1, center1)
//                        val circleBackInt = intersectTwoPoints(circleBack, circle1)
//                        val circleBackInt1 = circleBackInt.second
//                        val circleBackInt2 = circleBackInt.first
//                        val cross = angleBisectorTool(circleBackInt1, circleBackInt2, sample1)
//                        val target = intersectTwoPointsOther(cross, circle1, circleBackInt2)
//                        val big = circleTool(circleBackInt2, target)
//                        val opp = intersectTwoPoints(big, circleBack).first
//                        val solution1 = perpendicularBisectorTool(opp, circleBackInt1)
//                        val solutionP12 = intersectTwoPoints(solution1, circle1)
//                        val solutionP1 = solutionP12.first
//                        val solutionP2 = solutionP12.second
//                        val solution2 = perpendicularTool(solution1, solutionP1, probe = center1)
//                        val solutionP4 = intersectTwoPointsOther(solution2, circle1, solutionP1)
//                        val solution3 = perpendicularTool(solution2, solutionP4, probe = center1)
//                        val solutionP3 = intersectTwoPointsOther(solution3, circle1, solutionP4)
//                        val solution4 = lineTool(solutionP3, solutionP2)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
