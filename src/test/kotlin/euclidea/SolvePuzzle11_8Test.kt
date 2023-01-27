package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle11_8Test {
    // Regular Pentagon

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            // solution found 9 sec
            maxExtraElements = 2,
            maxDepth = 10,
            maxNonNewElements = 5,
            maxConsecutiveNonNewElements = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val vertex: Point,
        val probeDir: Point
    )

    data class Setup(
        val circle: Element.Circle,
        val probe: Point
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                vertex = Point(0.0, 1.0),
                probeDir = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                vertex = Point(0.0043, 1.0034),
                probeDir = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = circleTool(center, vertex)
                    val probe = intersectTwoPoints(circle, lineTool(center, probeDir)).first
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, probe) to EuclideaContext.of(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(5.0)
                        ),
                        points = listOf(center, vertex /*, probe*/),
                        elements = listOf(circle)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutionElements = constructSolution(params, setup)
            return { context ->
                context.hasElements(solutionElements)
            }
        }

        private fun constructSolution(params: Params, setup: Setup): List<Element.Line> {
            with(params) {
                with(setup) {
                    val solutionPoints = (0..4).map { i -> rotatePoint(center, vertex, 72.0 * (i.toDouble())) }
                    val solutionLines = (0..4).map { i -> lineTool(solutionPoints[i], solutionPoints[(i + 1) % 5]) }
                    return solutionLines
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params, setup)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

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
                        // Sub-optimal 12L solution
                        val axis = lineTool(center, vertex)
                        val h0_5 = perpendicularBisectorTool(center, vertex)
                        val h0 = perpendicularTool(axis, center, probe = probe)
                        val m0_5 = intersectOnePoint(h0_5, axis)
                        val r0 = intersectTwoPoints(h0, circle).second
                        val circleA = circleTool(m0_5, r0)
                        val m_A = intersectTwoPoints(circleA, axis).first
                        val h_B = perpendicularBisectorTool(m_A, center)
                        val r_B = intersectTwoPoints(h_B, circle).second
                        val m_1 = intersectTwoPointsOther(axis, circle, vertex)
                        val circleC = nonCollapsingCompassTool(r_B, vertex, m_1)
                        val pC = intersectTwoPoints(circleC, circle)
                        val rC = pC.first
                        val lC = pC.second
                        val circleD = nonCollapsingCompassTool(rC, r_B, m_1)
                        val solution1 = lineTool(vertex, rC)
                        val p_D = intersectTwoPoints(circleD, circle)
                        val r_D = p_D.first
                        val l_D = p_D.second
                        val solution2 = lineTool(rC, r_D)
                        val solution3 = lineTool(r_D, l_D)
                        val solution4 = lineTool(l_D, lC)
                        val solution5 = lineTool(lC, vertex)
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
//                        val circle1 = nonCollapsingCompassTool(baseB1, baseB2, center)
//                        val right = intersectOnePoint(circle1, line1)
//                        val circle2 = circleTool(right, center)
//                        val bisect = perpendicularBisectorTool(center, vertex)
//                        val center3 = intersectTwoPoints(bisect, circle2).second
//                        val circle3 = circleTool(center, center3)
//                        val point = intersectTwoPointsOther(circle3, line1, center)
//                        val parallel = parallelTool(side1, point, probe = center)
//                        val solutionP2 = intersectOnePoint(parallel, side2)
//                        val solution = parallelTool(line1, solutionP2, probe = vertex)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
