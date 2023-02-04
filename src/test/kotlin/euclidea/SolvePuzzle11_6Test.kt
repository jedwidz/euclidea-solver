package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertFalse

class SolvePuzzle11_6Test {
    // Circle in Angle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // improve suboptimal solution not found
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 7,
            maxUnfamiliarElements = 1,
            maxNonNewElements = 4,
            maxConsecutiveNonNewElements = 2,
            useTargetConstruction = true,
            fillKnownElements = true
        )
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseB: Point,
        val sample: Point,
        val probe1Scale: Double,
        val probe2Scale: Double
    ) {
        val probe1 = baseO + (baseA - baseO) * probe1Scale
        val probe2 = baseO + (baseB - baseO) * probe2Scale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val half: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                baseB = Point(0.41, 0.8),
                sample = Point(0.35, 0.45),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.01, 0.0),
                baseB = Point(0.411, 0.8005),
                sample = Point(0.350101, 0.451),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseO, baseA, limit1 = true)
                    val line2 = Element.Line(baseO, baseB, limit1 = true)
                    val half = angleBisectorTool(baseA, baseO, baseB)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, half) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(15.0),
//                            angleBisectorToolEnabled = true
                        ),
                        points = listOf(baseO, baseA, baseB, sample/*, probe1, probe2*/),
                        elements = listOf(line1, line2, half)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutions = constructSolutions(params)
            // Check tangent condition
            assertFalse(solutions.pairs().any { (a, b) -> coincides(a, b) })
            for (solution in solutions) {
                listOf(setup.line1, setup.line2).forEach { line -> intersectOnePoint(line, solution) }
            }
            return { context ->
                // solutions.any { solution -> context.hasElement(solution) }
                // Just look for center of a solution
                context.elements.size > 1 && context.elements.last().let { element ->
                    solutions.any { solution ->
                        val pointOfInterest = solution.center
                        pointAndElementCoincide(pointOfInterest, element) &&
                                // Just checking point/line has some false positives with 'almost coincident' lines
                                // Note this still avoids evaluating context points in most cases
                                context.hasPoint(pointOfInterest)
                    }
                }
            }
        }

        private fun constructSolutions(params: Params): List<Element.Circle> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 7L solution
                    val half = angleBisectorTool(baseA, baseO, baseB)
                    val perp = perpendicularTool(line2, sample, probe = baseO)
                    val touch = intersectOnePoint(perp, line2)
                    val center1 = intersectOnePoint(perp, half)
                    val circle1 = circleTool(center1, touch)
                    val line = lineTool(baseO, sample)

                    val aim1 = intersectTwoPoints(circle1, line).second
                    val cross11 = lineTool(center1, aim1)
                    val cross12 = parallelTool(cross11, sample, probe = center1)
                    val center12 = intersectOnePoint(cross12, half)
                    val solution1 = circleTool(center12, sample)

                    // Second solution
                    val aim2 = intersectTwoPoints(circle1, line).first
                    val cross21 = lineTool(center1, aim2)
                    val cross22 = parallelTool(cross21, sample, probe = center1)
                    val center22 = intersectOnePoint(cross22, half)
                    val solution2 = circleTool(center22, sample)

                    return listOf(solution1, solution2)
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
//                        // Moved to setup
//                        // val half = angleBisectorTool(baseA, baseO, baseB)
//
//                        // val perp = perpendicularTool(line2, sample, probe = baseO)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutions = constructSolutions(params)
            val centers = solutions.map { it.center }
            return { context ->
                // Assumes that solution is the last element (no extraneous elements)
                if (context.elements.lastOrNull()
                        ?.let { solutions.any { solution -> coincides(it, solution) } } == true
                )
                    0
                else {
                    val onCenter =
                        centers.maxOf { center -> context.elements.count { pointAndElementCoincide(center, it) } }
                    // Need two elements to locate center, then the solution circle itself
                    // max(0, 2 - onCenter) + 1
                    // Just look for center of a solution
                    max(0, 2 - onCenter)
                }
            }
        }

        override fun toolsSequence(): List<Set<EuclideaTool>> {
            // Euclidea 11E E-star moves hint
            return toolsList(
                // Moved to setup
                // EuclideaTool.AngleBisectorTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                // This is skipped, just look for center of a solution
                // EuclideaTool.CircleTool
                // Extra for suboptimal solution
                setOf(EuclideaTool.CircleTool, EuclideaTool.LineTool),
            )
        }

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            return suboptimal7LSolution(false)(params, namer)
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(
                suboptimal7LSolution(true),
                optimal6LSolution(false),
                optimal6LSolution(true),
                suboptimal14ESolution(false),
                suboptimal14ESolution(true),
                suboptimal13ESolution(false),
                suboptimal13ESolution(true),
            )
        }

        private fun suboptimal7LSolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            // Sub-optimal 7L solution (other)
                            // Moved to setup
                            // val half = angleBisectorTool(baseA, baseO, baseB)
                            val perp = perpendicularTool(line2, sample, probe = baseO)
                            val touch = intersectOnePoint(perp, line2)
                            val center1 = intersectOnePoint(perp, half)
                            val circle1 = circleTool(center1, touch)
                            val line = lineTool(baseO, sample)
                            val aim1 = intersectTwoPoints(circle1, line, other).second
                            val cross1 = lineTool(center1, aim1)
                            val cross2 = parallelTool(cross1, sample, probe = center1)
                            val center2 = intersectOnePoint(cross2, half)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun optimal6LSolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            // Optimal 6L solution
                            // Moved to setup
                            // val half = angleBisectorTool(baseA, baseO, baseB)
                            val perp = perpendicularTool(half, sample, probe = baseO)
                            val touch1 = intersectOnePoint(perp, line1)
                            val mid = intersectOnePoint(perp, half)
                            val circle1 = nonCollapsingCompassTool(touch1, mid, sample)
                            val aim1 = intersectTwoPoints(circle1, half).first
                            val touch2 = intersectOnePoint(perp, line2)
                            val circle2 = nonCollapsingCompassTool(aim1, mid, touch2)
                            val ref = intersectTwoPoints(circle2, line2, other).first
                            val cross = perpendicularBisectorTool(ref, sample)
                            val center2 = intersectOnePoint(cross, half)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal14ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            //Given point (0) point1_baseO at (0.0, 0.0)
                            //Given point (1) point2_baseA at (1.0, 0.0)
                            //line1_line1 from point1_baseO to point2_baseA
                            //Given point (2) point3_baseB at (0.41, 0.8)
                            //line2_line2 from point1_baseO to point3_baseB
                            //line3_half bisecting angle from point2_baseA through point1_baseO to point3_baseB
                            // Moved to setup
                            // val half = angleBisectorTool(baseA, baseO, baseB)

                            //Given point (3) point4_sample at (0.35, 0.45)
                            //circle1 with center point1_baseO extending to point4_sample (radius 0.570087712549569)
                            val c1 = circleTool(baseO, sample)

                            //line4 from point1_baseO to point3_baseB
                            val l4 = lineTool(baseO, baseB)

                            //point5 at intersection (1/2) of circle1 and line4 (-0.26001175570503304, -0.5073400111317717)
                            val p5 = intersectTwoPoints(c1, l4).first

                            //circle2 with center point5 extending to point4_sample (radius 1.1351714579798586)
                            val c2 = circleTool(p5, sample)

                            //line5_line from point1_baseO to point4_sample
                            val line = lineTool(baseO, sample)

                            //point6 at intersection (1/2) of circle1 and circle2 (0.1609144907808447, 0.5469063234748173)
                            val p6 = intersectTwoPoints(c1, c2).first

                            //line6_perp from point6 to point4_sample
                            val perp = lineTool(p6, sample)

                            //point7_center1 at intersection (1/1) of line3_half and line6_perp (0.5601017395692335, 0.3423228584707677)
                            val center1 = intersectOnePoint(half, perp)

                            //circle3 with center point7_center1 extending to point4_sample (radius 0.23608707668554063)
                            val c3 = circleTool(center1, sample)

                            //point8_touch at intersection (1/1) of line2_line2 and line6_perp (0.255457245390422, 0.49845316173740883)
                            val touch = intersectOnePoint(line2, perp)

                            //circle4_circle1 with center point7_center1 extending to point8_touch (radius 0.34232285847076765)
                            val circle1 = circleTool(center1, touch)

                            //point9_aim1 at intersection (1/2) of line5_line and circle4_circle1 (0.22244416876862572, 0.2859996455596617)
                            val aim1 = intersectTwoPoints(line, circle1, other).first

                            //point10 at intersection (2/2) of line6_perp and circle3 (0.7702034791384669, 0.23464571694153535)
                            val p10 = intersectTwoPoints(perp, c3).second

                            //circle5 with center point9_aim1 extending to point10 (radius 0.5501613291402437)
                            val c5 = circleTool(aim1, p10)

                            //point11 at intersection (1/2) of circle3 and circle5 (0.7238783087268739, 0.5123650390404045)
                            val p11 = intersectTwoPoints(c3, c5, other).first

                            //line7_cross2 from point11 to point4_sample
                            val cross2 = lineTool(p11, sample)

                            val center2 = intersectOnePoint(cross2, half)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal13ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            //Given point (0) point1_baseO at (0.0, 0.0)
                            //Given point (1) point2_baseA at (1.0, 0.0)
                            //line1_line1 from point1_baseO to point2_baseA
                            //Given point (2) point3_baseB at (0.41, 0.8)
                            //line2_line2 from point1_baseO to point3_baseB
                            //line3_half bisecting angle from point2_baseA through point1_baseO to point3_baseB
                            //Given point (3) point4_sample at (0.35, 0.45)
                            //circle1_c1 with center point1_baseO extending to point4_sample (radius 0.570087712549569)
                            val c1 = circleTool(baseO, sample)

                            //line4_line from point1_baseO to point4_sample
                            val line = lineTool(baseO, sample)

                            //circle2 with center point3_baseB extending to point4_sample (radius 0.3551056180912941)
                            val c2 = circleTool(baseB, sample)

                            //point5_p6 at intersection (2/2) of circle1_c1 and circle2 (0.16091449078084388, 0.5469063234748175)
                            val p5 = intersectTwoPointsOther(c1, c2, sample)

                            //line5_perp from point5_p6 to point4_sample
                            val perp = lineTool(p5, sample)

                            //point6_center1 at intersection (1/1) of line3_half and line5_perp (0.5601017395692336, 0.3423228584707677)
                            val center1 = intersectOnePoint(half, perp)

                            //circle3_c3 with center point6_center1 extending to point4_sample (radius 0.23608707668554071)
                            val c3 = circleTool(center1, sample)

                            //point7_touch at intersection (1/1) of line2_line2 and line5_perp (0.25545724539042197, 0.4984531617374087)
                            val touch = intersectOnePoint(line2, perp)

                            //circle4_circle1 with center point6_center1 extending to point7_touch (radius 0.34232285847076777)
                            val circle1 = circleTool(center1, touch)

                            //point8_aim1 at intersection (1/2) of line4_line and circle4_circle1 (0.22244416876862572, 0.2859996455596617)
                            val aim1 = intersectTwoPoints(line, circle1, other).first

                            //point9_p10 at intersection (2/2) of line5_perp and circle3_c3 (0.7702034791384672, 0.2346457169415356)
                            val p9 = intersectTwoPoints(perp, c3).second

                            //circle5_c5 with center point8_aim1 extending to point9_p10 (radius 0.5501613291402441)
                            val c5 = circleTool(aim1, p9)

                            //point10_p11 at intersection (1/2) of circle3_c3 and circle5_c5 (0.7238783087268745, 0.5123650390404042)
                            val p10 = intersectTwoPoints(c3, c5, other).first

                            //line6_cross2 from point10_p11 to point4_sample
                            val cross2 = lineTool(p10, sample)

                            val center2 = intersectOnePoint(cross2, half)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }
    }
}
