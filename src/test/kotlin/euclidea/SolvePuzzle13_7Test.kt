package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle13_7Test {
    // Inscribed Square - 2

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // no solution found 7 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 7,
            nonNewElementLimit = 4,
            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val base: Point,
        val sample: Point
    )

    data class Setup(
        val circle: Element.Circle
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                base = Point(0.65, 0.70),
                sample = Point(1.0, 0.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                base = Point(0.651, 0.702),
                sample = Point(1.003, 0.004)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = circleTool(center, sample)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle) to EuclideaContext(
                        config = EuclideaConfig(
                            perpendicularBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
                            angleBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true,
                            parallelToolEnabled = true,
                            maxSqDistance = sq(5.0)
                        ),
                        points = listOf(center, base/*, sample*/),
                        elements = listOf(circle)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params, setup)
            val solutionElements = solution.elements
            // Check reference solution forms a square
            assertTrue(formsSquare(LineSet.of(solutionElements)))
            return { context ->
                context.hasElements(solutionElements)
            }
        }

        data class Solution(
            val solution1: Element.Line,
            val solution2: Element.Line,
            val solution3: Element.Line,
            val solution4: Element.Line,
//            val solutionP1: Point,
//            val solutionP2: Point
        ) {
            val elements = listOf(solution1, solution2, solution3, solution4)
        }

        private fun constructSolution(params: Params, setup: Setup): Solution {
            // based on reference solution
            with(params) {
                with(setup) {
                    // Sub-optimal 10L solution
                    val join = lineTool(center, base)
                    val baseC = circleTool(center, base)
                    val perp = perpendicularTool(join, center, probe = sample)
                    val top = intersectTwoPoints(join, circle).second
                    val side = intersectTwoPoints(perp, circle).first
                    val line = lineTool(side, top)
                    val aim = intersectTwoPoints(line, baseC).second
                    val radial = lineTool(center, aim)
                    val solutionP13 = intersectTwoPoints(radial, circle)
                    val solutionP1 = solutionP13.first
                    val solutionP3 = solutionP13.second
                    val radialPerp = perpendicularBisectorTool(solutionP3, solutionP1)
                    val solutionP24 = intersectTwoPoints(radialPerp, circle)
                    val solutionP2 = solutionP24.first
                    val solutionP4 = solutionP24.second
                    val solution1 = lineTool(solutionP1, solutionP2)
                    val solution2 = lineTool(solutionP2, solutionP3)
                    val solution3 = lineTool(solutionP3, solutionP4)
                    val solution4 = lineTool(solutionP4, solutionP1)
                    return Solution(solution1, solution2, solution3, solution4)
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params, setup).elements
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            with(setup) {
//                val solution = constructSolution(params, setup)
//                val point1 = solution.solutionP1
//                val point2 = solution.solutionP2
//                return { context ->
//                    // Assumes that solution is the last element (no extraneous elements)
//                    if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                        0
//                    else {
//                        val onPoint1 = context.elements.count { pointAndElementCoincide(point1, it) }
//                        val onPoint2 = context.elements.count { pointAndElementCoincide(point2, it) }
//                        // Assume solution uses at least one of the highlighted points
//                        max(0, min(2 - onPoint1, 2 - onPoint2)) + 1
//                    }
//                }
//            }
//        }

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 12E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isLineFromPerpendicular
//                    1 -> !element.isCircleFromCircle
//                    2 -> !element.isLineFromLine
//                    3 -> !element.isLineFromPerpendicular
//                    4 -> !element.isCircleFromCircle
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
                        // Sub-optimal 10L solution
                        val join = lineTool(center, base)
                        val baseC = circleTool(center, base)
                        val perp = perpendicularTool(join, center, probe = sample)
                        val top = intersectTwoPoints(join, circle).second
                        val side = intersectTwoPoints(perp, circle).first
                        val line = lineTool(side, top)
                        val aim = intersectTwoPoints(line, baseC).second
                        val radial = lineTool(center, aim)
                        val solutionP13 = intersectTwoPoints(radial, circle)
                        val solutionP1 = solutionP13.first
                        val solutionP3 = solutionP13.second
                        val radialPerp = perpendicularBisectorTool(solutionP3, solutionP1)
                        val solutionP24 = intersectTwoPoints(radialPerp, circle)
                        val solutionP2 = solutionP24.first
                        val solutionP4 = solutionP24.second
                        val solution1 = lineTool(solutionP1, solutionP2)
                        val solution2 = lineTool(solutionP2, solutionP3)
                        val solution3 = lineTool(solutionP3, solutionP4)
                        val solution4 = lineTool(solutionP4, solutionP1)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::optimal6LSolution)
//        }
//
//        private fun optimal6LSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 6L solution
//                        val perpC = perpendicularTool(lineCA, sample, probe = base)
//                        val measure = circleTool(sample, center)
//                        val down = intersectTwoPoints(measure, perpC).first
//                        val cross = lineTool(down, base)
//                        val aim = intersectOnePoint(cross, lineCA)
//                        val solution1 = perpendicularTool(lineCA, aim, probe = base)
//                        val solutionP1 = intersectOnePoint(solution1, lineBC)
//                        val solution2 = perpendicularTool(perpC, solutionP1, probe = sample)
//                        val solutionP2 = intersectOnePoint(solution2, lineAB)
//                        val solution3 = perpendicularTool(lineCA, solutionP2, probe = center)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
