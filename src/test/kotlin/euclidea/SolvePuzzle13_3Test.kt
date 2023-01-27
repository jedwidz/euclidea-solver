package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle13_3Test {
    // Equilateral Triangle On Concentric Circles

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 23 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 8,
//            maxNonNewElements = 3,
//            maxConsecutiveNonNewElements = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val vertex: Point,
        val center: Point,
        val base1: Point,
        val base2: Point
    )

    data class Setup(
        val circle1: Element.Circle,
        val circle2: Element.Circle
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                vertex = Point(1.0, 0.0),
                center = Point(0.0, 0.0),
                base1 = Point(0.4, 0.1),
                base2 = Point(0.6, 0.2)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                vertex = Point(1.0013, 0.002),
                center = Point(0.0, 0.0),
                base1 = Point(0.401, 0.102),
                base2 = Point(0.604, 0.2055)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle1 = circleTool(center, base1)
                    val circle2 = circleTool(center, base2)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle1, circle2) to EuclideaContext.of(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(5.0)
                        ),
                        // base1 and base2 act as probe points
                        points = listOf(vertex, center, base1, base2),
                        elements = listOf(circle1, circle2)
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
                    // Sub-optimal 7L solution
                    val circle3 = circleTool(center, vertex)
                    val lens3 = circleTool(vertex, center)
                    val top = intersectTwoPoints(lens3, circle3).second
                    val point1 = intersectTwoPoints(lens3, circle1).second
                    val wedge = nonCollapsingCompassTool(center, point1, top)
                    val solutionP2 = intersectTwoPoints(wedge, circle2).second
                    val guide = circleTool(vertex, solutionP2)
                    val solutionP1 = intersectTwoPoints(guide, circle1).first
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

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 8E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
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
                        val circle3 = circleTool(center, vertex)
                        val lens3 = circleTool(vertex, center)
                        val top = intersectTwoPoints(lens3, circle3).second
                        val point1 = intersectTwoPoints(lens3, circle1).second
                        val wedge = nonCollapsingCompassTool(center, point1, top)
                        val solutionP2 = intersectTwoPoints(wedge, circle2).second
                        val guide = circleTool(vertex, solutionP2)
                        val solutionP1 = intersectTwoPoints(guide, circle1).first
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
