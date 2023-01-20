package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertTrue

class SolvePuzzle14_5Test {
    // Arbelos

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 1 sec
        Solver().improveSolution(
            maxExtraElements = 4,
            maxDepth = 5,
//            nonNewElementLimit = 7,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val centerA: Point,
        val sampleA: Point,
        val radiusBScale: Double,
        val probe: Point
    ) {
        val dirA = sampleA - centerA
        val radiusA = dirA.distance
        val radiusB = radiusA * radiusBScale
        val radiusC = radiusA * (1.0 - radiusBScale)
        val centerB = centerA + dirA * (radiusC / radiusA)
        val centerC = centerA - dirA * (radiusB / radiusA)
    }

    data class Setup(
        val circleA: Element.Circle,
        val circleB: Element.Circle,
        val circleC: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                sampleA = Point(1.0, 0.0),
                radiusBScale = 0.56,
                probe = Point(0.4, 0.15)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                sampleA = Point(1.0, 0.0),
                radiusBScale = 0.5601,
                probe = Point(0.402, 0.1503)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circleA = Element.Circle(centerA, radiusA)
                    val circleB = Element.Circle(centerB, radiusB)
                    val circleC = Element.Circle(centerC, radiusC)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circleA, circleB, circleC) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(50.0),
//                            parallelToolEnabled = true,
                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(centerA, centerB, centerC, probe),
                        elements = listOf(circleA, circleB, circleC)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params).solution
            // validate solution
            with(setup) {
                assertTrue(meetAtOnePoint(solution, circleA))
                assertTrue(meetAtOnePoint(solution, circleB))
                assertTrue(meetAtOnePoint(solution, circleC))
                assertTrue(solution.radius < circleA.radius)
            }
            return { context ->
                context.hasElement(solution)
            }
        }

        data class Solution(
            val solution: Element.Circle
        )

        private fun constructSolution(params: Params): Solution {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 12L solution (gah)
                    val base = lineTool(centerA, centerB)
                    val perpA = perpendicularTool(base, centerA, probe = probe)
                    val perpB = perpendicularTool(base, centerB, probe = probe)
                    val perpC = perpendicularTool(base, centerC, probe = probe)
                    val verticalA = intersectTwoPoints(perpA, circleA).second
                    val verticalB = intersectTwoPoints(perpB, circleB).first
                    val verticalC = intersectTwoPoints(perpC, circleC).first
                    val diagonalAB = lineTool(verticalA, verticalB)
                    val diagonalAC = lineTool(verticalA, verticalC)
                    val triB = intersectTwoPointsOther(diagonalAB, circleA, verticalA)
                    val triC = intersectTwoPointsOther(diagonalAC, circleA, verticalA)
                    val tri = lineTool(triB, triC)
                    val bar = lineTool(verticalB, verticalC)
                    val peg = intersectOnePoint(bar, base)
                    val par = parallelTool(tri, peg, probe = triB)
                    val tangentPointB = intersectTwoPoints(par, circleB).second
                    val tangentPointC = intersectTwoPoints(par, circleC).first
                    val coDiameterB = lineTool(centerB, tangentPointB)
                    val coDiameterC = lineTool(centerC, tangentPointC)
                    val solutionCenter = intersectOnePoint(coDiameterB, coDiameterC)
                    val solution = circleTool(solutionCenter, tangentPointB)

                    return Solution(solution)
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
            val solution = constructSolution(params).solution
            val center = solution.center
            return { context ->
                // Assumes that solution is the last element (no extraneous elements)
                if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
                    0
                else {
                    val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
                    // Need two elements to locate center, then the solution circle itself
                    max(0, 2 - onCenter) + 1
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5L/7E L/E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromPerpendicularBisector
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromLine
                    4 -> !element.isCircleFromCircle
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
                        // Sub-optimal 12L solution (gah)
                        val base = lineTool(centerA, centerB)
                        val perpA = perpendicularTool(base, centerA, probe = probe)
                        val perpB = perpendicularTool(base, centerB, probe = probe)
                        val perpC = perpendicularTool(base, centerC, probe = probe)
                        val verticalA = intersectTwoPoints(perpA, circleA).second
                        val verticalB = intersectTwoPoints(perpB, circleB).first
                        val verticalC = intersectTwoPoints(perpC, circleC).first
                        val diagonalAB = lineTool(verticalA, verticalB)
                        val diagonalAC = lineTool(verticalA, verticalC)
                        val triB = intersectTwoPointsOther(diagonalAB, circleA, verticalA)
                        val triC = intersectTwoPointsOther(diagonalAC, circleA, verticalA)
                        val tri = lineTool(triB, triC)
                        val bar = lineTool(verticalB, verticalC)
                        val peg = intersectOnePoint(bar, base)
                        val par = parallelTool(tri, peg, probe = triB)
                        val tangentPointB = intersectTwoPoints(par, circleB).second
                        val tangentPointC = intersectTwoPoints(par, circleC).first
                        val coDiameterB = lineTool(centerB, tangentPointB)
                        val coDiameterC = lineTool(centerC, tangentPointC)
                        val solutionCenter = intersectOnePoint(coDiameterB, coDiameterC)
                        val solution = circleTool(solutionCenter, tangentPointB)
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
//        fun optimal6LSolution(
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
//                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
//                        val midAB = intersectOnePoint(bisectAB, lineAB)
//                        val cross = lineTool(midAB, baseC)
//                        val circle1 = circleTool(baseC, midAB)
//                        val doubled = intersectTwoPointsOther(circle1, cross, midAB)
//                        val circle2 = circleTool(doubled, baseC)
//                        val tripled = intersectTwoPointsOther(circle2, cross, baseC)
//                        val solutionA = lineTool(baseA, tripled)
//                        val solutionB = lineTool(baseB, tripled)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
