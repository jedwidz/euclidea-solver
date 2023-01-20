package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle13_2Test {
    // Equilateral Triangle - 2

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found ~17 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 8,
            nonNewElementLimit = 5,
            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val vertex: Point,
        val base1: Point,
        val base2: Point,
        val dir1: Point
    ) {
        // parallel
        val dir2 = base2 + (dir1 - base1)
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                vertex = Point(0.0, 0.0),
                base1 = Point(0.1, -0.65),
                base2 = Point(0.2, -1.0),
                dir1 = Point(0.22, -0.65)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                vertex = Point(0.0, 0.0),
                base1 = Point(0.1011, -0.65031),
                base2 = Point(0.2013, -1.0033),
                dir1 = Point(0.2203, -0.6501)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(base1, dir1)
                    val line2 = Element.Line(base2, dir2)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2) to EuclideaContext.of(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(5.0)
                        ),
                        // base1 and base2 act as probe points
                        points = listOf(vertex, base1/*, base2*/),
                        elements = listOf(line1, line2)
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
            // Check reference solution forms equilateral triangle
            val vertices = listOf(params.vertex, solution.solutionP1, solution.solutionP2)
            val distances = vertices.pairs().map { (p1, p2) -> distance(p1, p2) }
            assertTrue(distances.pairs().all { (d1, d2) -> coincides(d1, d2) })
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
                    // Sub-optimal 11L solution
                    val axis = perpendicularTool(line1, vertex, probe = base1)
                    val m1 = intersectOnePoint(axis, line1)
                    val m2 = intersectOnePoint(axis, line2)
                    val half = perpendicularBisectorTool(m1, m2)
                    val mid = intersectOnePoint(half, axis)
                    val circle = circleTool(mid, m1)
                    val side = intersectTwoPoints(circle, half).first
                    val lens = circleTool(side, mid)
                    val sixtyAim = intersectTwoPoints(circle, lens).second
                    val sixty = lineTool(mid, sixtyAim)
                    val horizon = perpendicularTool(axis, vertex, probe = sixtyAim)
                    val len = intersectOnePoint(horizon, sixty)
                    val measureLen = nonCollapsingCompassTool(len, mid, m2)
                    val len2 = intersectTwoPoints(measureLen, line2).second
                    val guide = nonCollapsingCompassTool(len2, m1, vertex)
                    val solutionP1 = intersectTwoPoints(guide, line1).first
                    val solutionP2 = intersectTwoPoints(guide, line2).second
                    val solution1 = lineTool(vertex, solutionP1)
                    val solution2 = lineTool(vertex, solutionP2)
                    val solution3 = lineTool(solutionP1, solutionP2)
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

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 9E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isLineFromLine
//                    1 -> !element.isLineFromLine
//                    2 -> !element.isCircleFromCircle
//                    3 -> !element.isCircleFromCircle
//                    4 -> !element.isLineFromLine
//                    5 -> !element.isCircleFromCircle
//                    6 -> !element.isLineFromLine
//                    7 -> !element.isCircleFromCircle
//                    8 -> !element.isLineFromLine
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
                        // Sub-optimal 11L solution
                        val axis = perpendicularTool(line1, vertex, probe = base1)
                        val m1 = intersectOnePoint(axis, line1)
                        val m2 = intersectOnePoint(axis, line2)
                        val half = perpendicularBisectorTool(m1, m2)
                        val mid = intersectOnePoint(half, axis)
                        val circle = circleTool(mid, m1)
                        val side = intersectTwoPoints(circle, half).first
                        val lens = circleTool(side, mid)
                        val sixtyAim = intersectTwoPoints(circle, lens).second
                        val sixty = lineTool(mid, sixtyAim)
                        val horizon = perpendicularTool(axis, vertex, probe = sixtyAim)
                        val len = intersectOnePoint(horizon, sixty)
                        val measureLen = nonCollapsingCompassTool(len, mid, m2)
                        val len2 = intersectTwoPoints(measureLen, line2).second
                        val guide = nonCollapsingCompassTool(len2, m1, vertex)
                        val solutionP1 = intersectTwoPoints(guide, line1).first
                        val solutionP2 = intersectTwoPoints(guide, line2).second
                        val solution1 = lineTool(vertex, solutionP1)
                        val solution2 = lineTool(vertex, solutionP2)
                        val solution3 = lineTool(solutionP1, solutionP2)
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
//                        val circle1 = nonCollapsingCompassTool(base2, dir2, vertex)
//                        val right = intersectOnePoint(circle1, line1)
//                        val circle2 = circleTool(right, vertex)
//                        val bisect = perpendicularBisectorTool(vertex, base1)
//                        val center3 = intersectTwoPoints(bisect, circle2).second
//                        val circle3 = circleTool(vertex, center3)
//                        val point = intersectTwoPointsOther(circle3, line1, vertex)
//                        val parallel = parallelTool(side1, point, probe = vertex)
//                        val solutionP2 = intersectOnePoint(parallel, side2)
//                        val solution = parallelTool(line1, solutionP2, probe = base1)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
