package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle13_6Test {
    // Circle Through Two Points and Tangent to Line

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // improved suboptimal solution not found 11 hr 17 min
        Solver().improveSolution(
            maxExtraElements = 6,
            maxDepth = 6,
            maxUnfamiliarElements = 3,
            maxNonNewElements = 3,
            maxConsecutiveNonNewElements = 2,
            maxLinesPerHeading = 2,
            maxCirclesPerRadius = 2,
            useTargetConstruction = true,
            fillKnownElements = true
        )
    }

    data class Params(
        val pointA: Point,
        val pointB: Point,
        val base: Point,
        val dir: Point,
    )

    data class Setup(
        val line: Element.Line,
        val bisect: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(-0.21, 0.89),
                pointB = Point(-1.0, 0.0),
                base = Point(0.42, -1.5),
                dir = Point(0.0, -1.51),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(-0.213, 0.9111),
                pointB = Point(-1.0111, 0.002),
                base = Point(0.4222, -1.503),
                dir = Point(0.0, -1.5113),
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line = Element.Line(base, dir)
                    val bisect = perpendicularBisectorTool(pointA, pointB)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line, bisect) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // base is included as a probe point
                        // TODO 'hide' base?
                        points = listOf(pointA, pointB, base),
                        elements = listOf(line, bisect)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutions = constructSolutions(params)
            return { context ->
                // solutions.any { context.hasElement(it) }
                //                context.elements.lastOrNull()
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

        override fun excludeElements(params: Params, setup: Setup): ElementSet {
            with(params) {
                val elements = ElementSet()
                // Hints suggest we don't need these...
                elements += listOf(circleTool(pointA, pointB), circleTool(pointB, pointA))
                return elements
            }
        }

        private fun constructSolutions(params: Params): List<Element.Circle> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    fun impl(other: Boolean): Element.Circle {
                        // Sub-optimal 7L solution
                        val bisect = perpendicularBisectorTool(pointA, pointB)
                        val intercept = intersectOnePoint(bisect, line)
                        val lineB = lineTool(intercept, pointB)
                        val perp = perpendicularTool(line, base, probe = pointB)
                        val center1 = intersectOnePoint(perp, bisect)
                        val circle1 = circleTool(center1, base)
                        val aim1 = intersectTwoPoints(circle1, lineB, other).first
                        val cross1 = lineTool(center1, aim1)
                        val cross2 = parallelTool(cross1, pointB, probe = center1)
                        val center2 = intersectOnePoint(cross2, bisect)
                        val solution = circleTool(center2, pointB)
                        return solution
                    }
                    return listOf(impl(false), impl(true))
                }
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        val bisect = perpendicularBisect(pointA, pointB)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        // Just look for center of a solution - nothing to check here
//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solutions = constructSolutions(params)
//            return { context ->
//                solutions.minOf { solution ->
//                    val center = solution.center
//                    // Assumes that solution is the last element (no extraneous elements)
//                    if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                        0
//                    else {
//                        val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
//                        // Need two elements to locate center, then the solution circle itself
//                        max(0, 2 - onCenter) + 1
//                    }
//                }
//            }
//        }

        override fun toolsSequence(): List<Set<EuclideaTool>> {
            // Euclidea 10E E-star moves hint
            return toolsList(
                // Moved to setup
                // EuclideaTool.PerpendicularBisectorTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                // // This is skipped, just look for center of a solution
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
                suboptimal7LSolutionAlt(false),
                suboptimal7LSolutionAlt(true),
                optimal6LSolution(false),
                optimal6LSolution(true),
                suboptimal12ESolution(false),
                suboptimal12ESolution(true)
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
                            // Sub-optimal 7L solution
                            // val bisect = perpendicularBisectorTool(pointA, pointB)
                            val intercept = intersectOnePoint(bisect, line)
                            val lineB = lineTool(intercept, pointB)
                            val perp = perpendicularTool(line, base, probe = pointB)
                            val center1 = intersectOnePoint(perp, bisect)
                            val circle1 = circleTool(center1, base)
                            val aim1 = intersectTwoPoints(circle1, lineB, other).first
                            val cross1 = lineTool(center1, aim1)
                            val cross2 = parallelTool(cross1, pointB, probe = center1)
                            val center2 = intersectOnePoint(cross2, bisect)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, pointB)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal7LSolutionAlt(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            // Sub-optimal 7L solution, with perpendicular through pointB
                            // val bisect = perpendicularBisectorTool(pointA, pointB)
                            val intercept = intersectOnePoint(bisect, line)
                            val lineB = lineTool(intercept, pointB)
                            val perp = perpendicularTool(line, pointB, probe = intercept)
                            val foot = intersectOnePoint(perp, line)
                            val center1 = intersectOnePoint(perp, bisect)
                            val circle1 = circleTool(center1, foot)
                            val aim1 = intersectTwoPoints(circle1, lineB, other).first
                            val cross1 = lineTool(center1, aim1)
                            val cross2 = parallelTool(cross1, pointB, probe = center1)
                            val center2 = intersectOnePoint(cross2, bisect)
                            // Just look for center of a solution
                            // val solution = circleTool(center2, pointB)
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
                            // val bisect = perpendicularBisectorTool(pointA, pointB)
                            val lineAB = lineTool(pointA, pointB)
                            val midAB = intersectOnePoint(bisect, lineAB)
                            val inline = intersectOnePoint(lineAB, line)
                            val circle1 = nonCollapsingCompassTool(inline, midAB, pointB)
                            val ref = intersectTwoPoints(circle1, bisect).second
                            val circle2 = nonCollapsingCompassTool(ref, midAB, inline)
                            val ground = intersectTwoPoints(circle2, line, other).second
                            val perp = perpendicularTool(line, ground, probe = midAB)

                            // Alternatively to `perp`, can bisect with the given points - mentioned here for familiarity
                            val bisectGroundA = perpendicularBisectorTool(ground, pointA)
                            val bisectGroundB = perpendicularBisectorTool(ground, pointB)

                            val center = intersectOnePoint(perp, bisect)
                            // Just look for center of a solution
                            // val solution = circleTool(center, ground)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal12ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            // Optimal 6L solution
                            // val bisect = perpendicularBisectorTool(pointA, pointB)
                            // line2_bisect bisecting points point3_pointA and point4_pointB

                            //point5_intercept at intersection (1/1) of line1_line and line2_bisect (1.555738186087467, -1.472958614616965)
                            val intercept = intersectOnePoint(line, bisect)

                            //line3_lineB from point4_pointB to point5_intercept
                            val lineB = lineTool(pointB, intercept)

                            //circle1 with center point5_intercept extending to point3_pointA (radius 2.949814359616548)
                            val c1 = circleTool(intercept, pointA)

                            //point6 at intersection (1/2) of line1_line and circle1 (4.504716785846012, -1.4027448384322376)
                            val p6 = intersectTwoPoints(line, c1).first

                            //circle2 with center point6 extending to point4_pointB (radius 5.6806337651817636)
                            val c2 = circleTool(p6, pointB)

                            //point7 at intersection (1/2) of circle1 and circle2 (-0.9270028328611901, -3.065881019830028)
                            val p7 = intersectTwoPoints(c1, c2).first

                            //line4_perp from point7 to point4_pointB
                            val perp = lineTool(p7, pointB)

                            //point8_center1 at intersection (1/1) of line2_bisect and line4_perp (-1.0193522820442742, 0.8127958458595242)
                            val center1 = intersectOnePoint(bisect, perp)

                            //circle3 with center point8_center1 extending to point3_pointA (radius 0.8130261975279891)
                            val c3 = circleTool(center1, pointA)

                            //point9_foot at intersection (1/1) of line1_line and line4_perp (-0.9635014164305951, -1.5329405099150142)
                            val foot = intersectOnePoint(line, perp)

                            //circle4_circle1 with center point8_center1 extending to point9_foot (radius 2.3464011528279234)
                            val circle1 = circleTool(center1, foot)

                            //point10_aim1 at intersection (1/2) of line3_lineB and circle4_circle1 (-3.3080009628671774, 1.3301792489174447)
                            val aim1 = intersectTwoPoints(lineB, circle1, other).first

                            //point11 at intersection (2/2) of line4_perp and circle3 (-1.0387045640885486, 1.6255916917190485)
                            val p11 = intersectTwoPoints(perp, c3).second

                            //circle5 with center point10_aim1 extending to point11 (radius 2.2884437194022627)
                            val c5 = circleTool(aim1, p11)

                            //point12 at intersection (2/2) of circle3 and circle5 (-1.3864445760762305, 0.0873615996806053)
                            val p12 = intersectTwoPoints(c3, c5, other).second

                            //line5_cross2 from point12 to point4_pointB
                            val cross2 = lineTool(p12, pointB)

                            val center = intersectOnePoint(cross2, bisect)
                            // Just look for center of a solution
                            // val solution = circleTool(center, pointA)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }
    }
}
