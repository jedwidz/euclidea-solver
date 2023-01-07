package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle13_4Test {
    // Square in Triangle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // no solution found 1 min 20 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 8,
            nonNewElementLimit = 4,
            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val pointA: Point,
        val pointB: Point,
        val pointC: Point
    )

    data class Setup(
        val lineAB: Element.Line,
        val lineBC: Element.Line,
        val lineCA: Element.Line
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(0.55, 0.45),
                pointC = Point(1.0, 0.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(0.551, 0.452),
                pointC = Point(1.003, 0.004)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineAB = Element.Line(pointA, pointB, limit1 = true, limit2 = true)
                    val lineBC = Element.Line(pointB, pointC, limit1 = true, limit2 = true)
                    val lineCA = Element.Line(pointC, pointA, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineAB, lineBC, lineCA) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(pointA, pointB, pointC),
                        elements = listOf(lineAB, lineBC, lineCA)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params, setup)
            val solutionElements = listOf(setup.lineCA) + solution.elements
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
            val solutionP1: Point,
            val solutionP2: Point
        ) {
            val elements = listOf(solution1, solution2, solution3)
        }

        private fun constructSolution(params: Params, setup: Setup): Solution {
            // based on reference solution
            with(params) {
                with(setup) {
                    // Sub-optimal 10L solution
                    val bisectAC = perpendicularBisectorTool(pointA, pointC)
                    val midAC = intersectOnePoint(bisectAC, lineCA)
                    val rebisectAC = perpendicularBisectorTool(midAC, pointA)
                    val quarterAC = intersectOnePoint(rebisectAC, lineCA)
                    val circle = nonCollapsingCompassTool(pointA, midAC, quarterAC)
                    val top = intersectTwoPoints(circle, rebisectAC).second
                    val parallelAC = perpendicularTool(rebisectAC, top, probe = pointA)
                    val slantB = lineTool(midAC, pointB)
                    val slantQuarterAC = parallelTool(slantB, quarterAC, probe = pointB)
                    val opp = intersectOnePoint(slantQuarterAC, parallelAC)
                    val cross = lineTool(opp, midAC)
                    val solutionP1 = intersectOnePoint(cross, lineAB)
                    val solution1 = perpendicularTool(bisectAC, solutionP1, probe = midAC)
                    val solution2 = perpendicularTool(lineCA, solutionP1, probe = pointA)
                    val solutionP2 = intersectOnePoint(solution1, lineBC)
                    val solution3 = perpendicularTool(lineCA, solutionP2, probe = pointA)
                    return Solution(solution1, solution2, solution3, solutionP1, solutionP2)
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

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 12E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromPerpendicular
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromPerpendicular
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isCircleFromCircle
                    6 -> !element.isLineFromLine
                    7 -> !element.isLineFromLine
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
                        // Sub-optimal 10L solution
                        val bisectAC = perpendicularBisectorTool(pointA, pointC)
                        val midAC = intersectOnePoint(bisectAC, lineCA)
                        val rebisectAC = perpendicularBisectorTool(midAC, pointA)
                        val quarterAC = intersectOnePoint(rebisectAC, lineCA)
                        val circle = nonCollapsingCompassTool(pointA, midAC, quarterAC)
                        val top = intersectTwoPoints(circle, rebisectAC).second
                        val parallelAC = perpendicularTool(rebisectAC, top, probe = pointA)
                        val slantB = lineTool(midAC, pointB)
                        val slantQuarterAC = parallelTool(slantB, quarterAC, probe = pointB)
                        val opp = intersectOnePoint(slantQuarterAC, parallelAC)
                        val cross = lineTool(opp, midAC)
                        val solutionP1 = intersectOnePoint(cross, lineAB)
                        val solution1 = perpendicularTool(bisectAC, solutionP1, probe = midAC)
                        val solution2 = perpendicularTool(lineCA, solutionP1, probe = pointA)
                        val solutionP2 = intersectOnePoint(solution1, lineBC)
                        val solution3 = perpendicularTool(lineCA, solutionP2, probe = pointA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal6LSolution)
        }

        private fun optimal6LSolution(
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
                        val perpC = perpendicularTool(lineCA, pointC, probe = pointB)
                        val measure = circleTool(pointC, pointA)
                        val down = intersectTwoPoints(measure, perpC).first
                        val cross = lineTool(down, pointB)
                        val aim = intersectOnePoint(cross, lineCA)
                        val solution1 = perpendicularTool(lineCA, aim, probe = pointB)
                        val solutionP1 = intersectOnePoint(solution1, lineBC)
                        val solution2 = perpendicularTool(perpC, solutionP1, probe = pointC)
                        val solutionP2 = intersectOnePoint(solution2, lineAB)
                        val solution3 = perpendicularTool(lineCA, solutionP2, probe = pointA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
