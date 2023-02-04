package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle15_9Test {
    // Circle with Center on Line

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // gave up
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 15,
            maxUnfamiliarElements = 0,
//            maxNonNewElements = 2,
//            maxConsecutiveNonNewElements = 1,
            useTargetConstruction = true,
            fillKnownElements = true
        )
    }

    data class Params(
        val center: Point,
        val sample1: Point,
        val base: Point,
        val dir: Point,
        val sample: Point
    )

    data class Setup(
        val circle: Element.Circle,
        val line: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.7),
                sample1 = Point(0.4, 0.8),
                base = Point(0.1, 0.0),
                dir = Point(0.9, 0.0),
                sample = Point(1.0, 0.3),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.7),
                sample1 = Point(0.401, 0.804),
                base = Point(0.1, 0.0),
                dir = Point(0.9, 0.0),
                sample = Point(1.01, 0.303),
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = circleTool(center, sample1)
                    val line = Element.Line(base, dir)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, line) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(25.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // sample1, base and dir act as probes
                        points = listOf(center, sample, base /*, sample1 , dir */),
                        elements = listOf(circle, line)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // Validate solution
            assertTrue(pointAndLineCoincide(solution.center, setup.line))
            assertTrue(pointAndCircleCoincide(params.sample, solution))
            assertTrue(meetAtOnePoint(setup.circle, solution))
            // Look for partial solution
            val pointOfInterest = solution.center
            return { context ->
//                context.hasPoint(solution.center)
                // TODO just check last; workaround for hack
//                context.elements.lastOrNull()
                context.elements.any { element ->
                    pointAndElementCoincide(pointOfInterest, element) &&
                            // Just checking point/line has some false positives with 'almost coincident' lines
                            // Note this still avoids evaluating context points in most cases
                            context.hasPoint(pointOfInterest)
                }
            }
//            return { context ->
//                context.hasElement(solution)
//            }
        }

        private fun constructSolution(params: Params): Element.Circle {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    val bracket1 = perpendicularTool(line, center, probe = base)
                    val bracket2 = perpendicularTool(line, sample, probe = base)
                    val p1 = intersectOnePoint(line, bracket1)
                    val p2 = intersectOnePoint(line, bracket2)
                    val d = p2 - p1
                    val param = solveByBisection(0.0, 1.0) { x ->
                        val p = p1 + d * x
                        val d1 = (p - center).distance - circle.radius
                        val d2 = (p - sample).distance
                        d1 - d2
                    }
                    val solutionCenter = p1 + d * param
                    val solution = circleTool(solutionCenter, sample)
                    return solution
                }
            }
        }

        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Guess initial steps, informed by hint
                    @Suppress("unused") val context = object {
                        // Nope (E)
                        // val lens1 = circleTool(center, sample)
                        // val lens2 = circleTool(sample, center)

                        // Nope...
                        // val perp = perpendicularTool(line, sample, probe = base)

                        // Nope... actually, yup...
//                        val perp = perpendicularTool(line, center, probe = base)

                        // Likely next elements...
//                        val point5 = intersectOnePoint(line, perp)
//                        val circle2 = circleTool(point5, sample)
//                        val intersection = intersectTwoPoints(circle2, circle)
//                        val point7 = intersection.second
//                        val point8 = intersection.first
//                        val line3 = lineTool(point7, point8)

                        // Nope...
                        // val perp = perpendicularTool(line, center, probe = base)
                        // val foot = intersectOnePoint(perp, line)
                        // val meet = intersectTwoPoints(perp, circle).first
                        // val measure = circleTool(foot, meet)

                        // Nope...
                        // val perp = perpendicularTool(line, center, probe = base)
                        // val foot = intersectOnePoint(perp, line)
                        // val measure = circleTool(foot, center)

                        // Nope...
                        // val perp = perpendicularTool(line, sample, probe = base)
                        // val foot = intersectOnePoint(perp, line)
                        // val measure = circleTool(foot, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solution = constructSolution(params)
//            val center = solution.center
//            return { context ->
//                // Assumes that solution is the last element (no extraneous elements)
//                if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                    0
//                else {
//                    val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
//                    // Need two elements to locate center, then the solution circle itself
//                    max(0, 2 - onCenter) + 1
//                }
//            }
//        }

//        override fun toolSequence(): List<EuclideaTool> {
//            // Euclidea 8E E-star moves hint
//            return listOf(
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                // Allow some extra elements
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//            )
//        }
//        override fun toolSequence(): List<EuclideaTool> {
//            // Euclidea 7L L-star moves hint
//            return listOf(
//                EuclideaTool.PerpendicularTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.NonCollapsingCompassTool,
//                EuclideaTool.NonCollapsingCompassTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool
//            )
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
                        // Sub-optimal 8L solution
                        //point1_center at (0.0, 0.7)
                        //point2_sample1 at (0.3, 0.6)
                        //circle1_circle with center point1_center extending to point2_sample1 (radius 0.31622776601683794)
                        //point3_base at (0.1, 0.0)
                        //point4_dir at (0.9, 0.0)
                        //line1_line from point3_base to point4_dir
                        //line2 perpendicular to line1_line through point1_center
                        val line2 = perpendicularTool(line, center, probe = base)

                        //point5 at intersection (1/1) of line1_line and line2 (-0.0, -0.0)
                        val point5 = intersectOnePoint(line, line2)

                        //point6_sample at (0.8, 0.55)
                        //circle2 with center point5 extending to point6_sample (radius 0.97082439194738)
                        val circle2 = circleTool(point5, sample)

                        //point7 at intersection (1/2) of circle1_circle and circle2 (-0.19132159857588635, 0.9517857142857145)
                        val intersection = intersectTwoPoints(circle2, circle)
                        val point7 = intersection.second

                        //point8 at intersection (2/2) of circle1_circle and circle2 (0.19132159857588635, 0.9517857142857145)
                        val point8 = intersection.first

                        //line3 from point7 to point8
                        val line3 = lineTool(point7, point8)

                        //point9 at intersection (1/1) of line2 and line3 (0.0, 0.9517857142857145)
                        val point9 = intersectOnePoint(line3, line2)

                        //circle3 with center point7 from distance between point9 and point6_sample (radius 0.895227211496658)
                        val circle3 = nonCollapsingCompassTool(point9, sample, point7)

                        //point10 at intersection (1/2) of line2 and circle3 (-2.7755575615628914E-17, 1.8263300562847062)
                        val point10 = intersectTwoPoints(circle3, line2).first

                        //circle4 with center point6_sample from distance between point9 and point10 (radius 0.8745443419989918)
                        val circle4 = nonCollapsingCompassTool(point9, point10, sample)

                        //point11 at intersection (1/2) of line3 and circle4 (0.0232142857142853, 0.9517857142857145)
                        val point11 = intersectTwoPoints(circle4, line3).first

                        //                        //point12 at intersection (1/2) of circle1_circle and line2 (0.0, 1.016227766016838)
                        val point12 = intersectTwoPoints(line2, circle).first

                        //                        //line4 from point11 to point12
                        val line4 = lineTool(point11, point12)
                        val tangentPoints = intersectTwoPoints(line4, circle)
                        val tangentPoint = tangentPoints.first
                        val diameter = lineTool(center, tangentPoint)
                        val solutionCenter = intersectOnePoint(diameter, line)
                        val solution = circleTool(solutionCenter, sample)

                        // Extra elements, as a hacky way to make them 'familiar'
                        // TODO be less hacky
                        val backToSample = lineTool(solutionCenter, sample)
                        val crossToSample = lineTool(tangentPoint, sample)

                        val point12_2 = intersectTwoPoints(line2, circle).second // spot the difference
                        val line4_2 = lineTool(point11, point12_2)
                        val tangentPoints_2 = intersectTwoPoints(line4_2, circle)
                        val tangentPoint_2 = tangentPoints_2.first
                        val diameter_2 = lineTool(center, tangentPoint_2)
                        val solutionCenter_2 = intersectOnePoint(diameter_2, line)
                        val solution_2 = circleTool(solutionCenter_2, sample)

                        val backToSample_2 = lineTool(solutionCenter_2, sample)
                        val crossToSample_2 = lineTool(tangentPoint_2, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::suboptimal17ESolution)
        }

        fun suboptimal17ESolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        //point1_center at (0.0, 0.7)
                        //point2_sample1 at (0.4, 0.8)
                        //circle1_circle with center point1_center extending to point2_sample1 (radius 0.4123105625617661)
                        //point3_base at (0.1, 0.0)
                        //point4_dir at (0.9, 0.0)
                        //line1_line from point3_base to point4_dir

                        //circle2 with center point3_base extending to point1_center (radius 0.7071067811865475)
                        val c2 = circleTool(base, center)

                        //point5 at intersection (1/2) of line1_line and circle2 (-0.6071067811865474, 0.0)
                        val p5 = intersectTwoPoints(line, c2).first

                        //circle3 with center point5 extending to point1_center (radius 0.926595188721963)
                        val c3 = circleTool(p5, center)

                        //point6 at intersection (2/2) of circle2 and circle3 (2.7755575615628914E-17, -0.7)
                        val p6 = intersectTwoPoints(c2, c3).second

                        //line2_line2 from point6 to point1_center
                        val l2 = lineTool(p6, center)

                        //point7_point5 at intersection (1/1) of line1_line and line2_line2 (1.3877787807814457E-17, -0.0)
                        val p7 = intersectOnePoint(line, l2)

                        //point8_sample at (1.0, 0.3)
                        //circle4_circle2 with center point7_point5 extending to point8_sample (radius 1.044030650891055)
                        val c4 = circleTool(p7, sample)

                        //point9_point7 at intersection (1/2) of circle1_circle and circle4_circle2 (-0.2750695644852816, 1.0071428571428571)
                        val i1 = intersectTwoPoints(circle, c4)
                        val p9 = i1.first

                        //point10_point8 at intersection (2/2) of circle1_circle and circle4_circle2 (0.2750695644852816, 1.0071428571428571)
                        val p10 = i1.second

                        //line3_line3 from point9_point7 to point10_point8
                        val l3 = lineTool(p9, p10)

                        //point11_point9 at intersection (1/1) of line2_line2 and line3_line3 (-6.08923342587777E-18, 1.007142857142857)
                        val p11 = intersectOnePoint(l2, l3)

                        //circle5 with center point9_point7 extending to point11_point9 (radius 0.2750695644852816)
                        val c5 = circleTool(p9, p11)

                        //circle6 with center point11_point9 extending to point9_point7 (radius 0.2750695644852816)
                        val c6 = circleTool(p11, p9)

                        //point12 at intersection (1/2) of circle5 and circle6 (-0.13753478224264098, 0.7689256264906814)
                        val i2 = intersectTwoPoints(c5, c6)
                        val p12 = i2.first

                        //point13 at intersection (2/2) of circle5 and circle6 (-0.1375347822426406, 1.2453600877950326)
                        val p13 = i2.second

                        //line4 from point12 to point13
                        val l4 = lineTool(p12, p13)

                        //circle7 with center point11_point9 extending to point8_sample (radius 1.2247657002088859)
                        val c7 = circleTool(p11, sample)

                        //point14 at intersection (1/2) of line4 and circle7 (-0.1375347822426418, -0.20987612548709045)
                        val p14 = intersectTwoPoints(l4, c7).first

                        //circle8_circle3 with center point9_point7 extending to point14 (radius 1.224765700208886)
                        val c8 = circleTool(p9, p14)

                        //circle9 with center point8_sample extending to point11_point9 (radius 1.2247657002088859)
                        val c9 = circleTool(sample, p11)
                        val i3 = intersectTwoPoints(c7, c9)

                        //point15 at intersection (1/2) of circle7 and circle9 (-0.11240367839042398, -0.21245397521301013)
                        val p15 = i3.first

                        //point16_point10 at intersection (2/2) of line2_line2 and circle8_circle3 (-5.551115123125783E-17, 2.200620027055227)
                        val p16 = intersectTwoPoints(l2, c8).second

                        //circle10 with center point15 extending to point16_point10 (radius 2.4156905280558076)
                        val c10 = circleTool(p15, p16)

                        //point17 at intersection (2/2) of circle7 and circle9 (1.1124036783904243, 1.5195968323558668)
                        val p17 = i3.second

                        //circle11 with center point17 extending to point16_point10 (radius 1.3043138178425733)
                        val c11 = circleTool(p17, p16)

                        //point18 at intersection (1/2) of circle10 and circle11 (2.1252402010125815, 0.6977715991963318)
                        val p18 = intersectTwoPoints(c10, c11).first

                        //circle12_circle4 with center point8_sample extending to point18 (radius 1.1934771699123707)
                        val c12 = circleTool(sample, p18)

                        //point19_point11 at intersection (1/2) of line3_line3 and circle12_circle4 (0.0385756739639469, 1.007142857142857)
                        val p19 = intersectTwoPoints(l3, c12).first

                        //point20_point12 at intersection (2/2) of circle1_circle and line2_line2 (-8.174226425932569E-18, 1.112310562561766)
                        val p20 = intersectTwoPoints(circle, l2).second

                        //line5_line4 from point19_point11 to point20_point12
                        val l5 = lineTool(p19, p20)

                        //point21_tangentPoint at intersection (1/2) of circle1_circle and line5_line4 (0.26660266382887, 0.3854796991618019)
                        val tangentPoint = intersectTwoPoints(circle, l5).first

                        //line6_diameter from point21_tangentPoint to point1_center
                        val diameter = lineTool(tangentPoint, center)

                        val solutionCenter = intersectOnePoint(diameter, line)
                        val solution = circleTool(solutionCenter, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
