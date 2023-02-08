package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolvePuzzle15_9Test {
    // Circle with Center on Line

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    companion object {
        const val maxDepth = 11
    }

    @Test
    fun improveSolution() {
        // improved solution found 43 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = maxDepth,
            maxUnfamiliarElements = 1,
            maxNonNewElements = 4,
            maxConsecutiveNonNewElements = 2,
            maxLinesPerHeading = 2,
            maxCirclesPerRadius = 2,
            useTargetConstruction = true,
            fillKnownElements = true
        )
    }

    data class Params(
        val center: Point,
        val sample1: Point,
        val base: Point,
        val dir: Point,
        val sample: Point,
    )

    data class Setup(
        val circle: Element.Circle,
        val line: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.5),
                sample1 = Point(0.1, 0.13),
                base = Point(0.11, 0.0),
                dir = Point(0.832, 0.0),
                sample = Point(0.6, 0.22),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.5),
                sample1 = Point(0.1362, 0.134),
                base = Point(0.112, 0.003),
                dir = Point(0.834, 0.0),
                sample = Point(0.676, 0.223),
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
                    val probeLine = lineTool(base, sample1)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, line) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // sample1, base and dir act as probes
                        points = listOf(center, sample, base /* dir, sample1 */),
                        elements = listOf(circle, line /* probeLine */)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutions = constructSolutions(params)
            // Validate solutions
            assertFalse(solutions.isEmpty())
            solutions.forEach { solution ->
                assertTrue(pointAndLineCoincide(solution.center, setup.line))
                assertTrue(pointAndCircleCoincide(params.sample, solution))
                assertTrue(meetAtOnePoint(setup.circle, solution))
            }
            // Look for partial solution
            val linesOfInterest = solutions.flatMap { solution ->
                listOf(
                    lineTool(params.center, solution.center),
                    lineTool(params.sample, solution.center),
                    perpendicularTool(setup.line, solution.center),
                )
            }
            val ignorePoints = listOf(params.center, params.sample)
            val initialElementCount = initialContext(params, Namer()).second.elements.size
            val targetDepth = initialElementCount + maxDepth
            return { context ->
                if (context.elements.size < targetDepth) false
                else {
                    context.points.any { point ->
                        !ignorePoints.any { coincides(it, point) } &&
                                linesOfInterest.any { lineOfInterest ->
                                    val howzat = pointAndElementCoincide(point, lineOfInterest)
                                    if (howzat) {
                                        println(
                                            "Howzat?\nlineOfInterest: $lineOfInterest\npoint: $point\nsource: ${
                                                context.pointSourceFor(
                                                    point
                                                )
                                            }"
                                        )
                                    }
                                    howzat
                                }
                    }
                }
//                context.hasPoint(solution.center)
//            return { context ->
//                context.hasElement(solution)
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
                        val p1 = projection(line, center)
                        val p2 = projection(line, sample)
                        val d = p2 - p1
                        val sign = if (other) -1 else 1
                        val param = solveByExpansionAndBisection(0.0) { x ->
                            val p = p1 + d * x
                            val d1 = (p - center).distance - sign * circle.radius
                            val d2 = (p - sample).distance
                            d1 - d2
                        }
                        val solutionCenter = p1 + d * param
                        val solution = circleTool(solutionCenter, sample)
                        return solution
                    }
                    return listOf(impl(false), impl(true)).filter { solution ->
                        // Genuine solutions only...
                        pointAndCircleCoincide(params.sample, solution) &&
                                meetAtOnePoint(setup.circle, solution)
                    }
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
                        // Likely start for E solution
//                        val perpCircle1 = circleTool(dir, sample)
//                        val perpCircle2 = circleTool(base, sample)
//                        val perpIntersect = intersectTwoPoints(perpCircle1, perpCircle2)
//                        val perpIntersect1 = perpIntersect.first
//                        val perpIntersect2 = perpIntersect.second
//                        val perp = lineTool(perpIntersect1, perpIntersect2)
//                        val intersection = intersectTwoPoints(perpCircle2, circle)
//                        val point7 = intersection.first
//                        val point8 = intersection.second
//                        val line4 = lineTool(point7, point8)

                        // Nope (E)
                        // val lens1 = circleTool(center, sample)
                        // val lens2 = circleTool(sample, center)

                        // Nope...
                        // Trying this again...
                        // val perp = perpendicularTool(line, sample, probe = base)

                        // Nope... actually, yup...
                        // val perp = perpendicularTool(line, center, probe = base)

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
//            // Euclidea 9E E-star moves hint
//            return listOf(
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.LineTool,
//                // This is skipped, just look for center of a solution
//                // EuclideaTool.CircleTool
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
//                // This is skipped, just look for center of a solution
//                // EuclideaTool.CircleTool
//            )
//        }

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            return suboptimal8LSolution(false)(params, namer)
        }

        private fun suboptimal8LSolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
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

                            //point12 at intersection (1/2) of circle1_circle and line2 (0.0, 1.016227766016838)
                            val point12 = intersectTwoPoints(line2, circle, other).first

                            //line4 from point11 to point12
                            val line4 = lineTool(point11, point12)
                            val tangentPoints = intersectTwoPoints(line4, circle)
                            val tangentPoint = tangentPoints.first
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        // TODO fix other solution...
        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(
//                suboptimal8LSolution(true),
                suboptimal17ESolution(false),
//                suboptimal17ESolution(true),
                suboptimal9L17ESolution(false),
//                suboptimal9L17ESolution(true),
                suboptimal16L16ESolution(false),
//                suboptimal16L16ESolution(true),
                optimal7LSolution(false),
                optimal7LSolution(true),
                suboptimal15ESolution(false),
                suboptimal15ESolution(true),
                suboptimal13ESolution(false),
            )
        }

        private fun optimal7LSolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            //Given point (1) point1_center at (0.0, 0.5)
                            //circle1_circle with center point1_center extending to point2_sample1 (radius 0.383275357934736)
                            //point4_dir at (0.832, 0.0)
                            //line1_line from point3_base to point4_dir
                            //line2_probeLine from point3_base to point2_sample1
                            //point3_base at intersection (0/1) of line1_line and line2_probeLine (0.11, 0.0)
                            //point2_sample1 at intersection (1/2) of circle1_circle and line2_probeLine (0.1, 0.13)
                            //Given point (2) point5_sample at (0.6, 0.22)

                            //line3 perpendicular to line1_line through point5_sample
                            val line3 = perpendicularTool(line, sample, probe = base)

                            //point6_base at intersection (1/1) of line1_line and line2_probeLine (0.11, -0.0)
                            //circle2 with center point6_base extending to point5_sample (radius 0.5371219600798314)
                            val circle2 = circleTool(base, sample)

                            //point7 at intersection (1/2) of circle1_circle and circle2 (-0.33095499091390224, 0.3066899019989415)
                            //point8 at intersection (2/2) of circle1_circle and circle2 (0.38152729156250964, 0.46343600414375213)
                            val intersection = intersectTwoPoints(circle2, circle)
                            val point7 = intersection.first
                            val point8 = intersection.second

                            //line4 from point7 to point8
                            val line4 = lineTool(point7, point8)

                            //point9 at intersection (1/1) of line1_line and line3 (0.6, -0.0)
                            // Already called that 'foot'
                            val foot = intersectOnePoint(line3, line)

                            //point10 at intersection (1/1) of line3 and line4 (0.6, 0.5115)
                            val point10 = intersectOnePoint(line3, line4)

                            //circle3 with center point5_sample from distance between point9 and point10 (radius 0.5115)
                            val circle3 = nonCollapsingCompassTool(foot, point10, sample)

                            //point11 at intersection (1/2) of line1_line and circle3 (0.13822922353184802, 2.7755575615628914E-17)
                            val point11 = intersectTwoPoints(line, circle3).first

                            //circle4 with center point10 from distance between point11 and point9 (radius 0.46177077646815196)
                            val circle4 = nonCollapsingCompassTool(point11, foot, point10)

                            val tangentPoints = intersectTwoPoints(circle4, circle, other)
                            val tangentPoint = tangentPoints.first
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal15ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            //Given point (1) point1_center at (0.0, 0.5)
                            //point2_sample1 at (0.1, 0.13)
                            //circle1_circle with center point1_center extending to point2_sample1 (radius 0.383275357934736)
                            //Given point (3) point3_base at (0.11, 0.0)
                            //Given point (4) point4_dir at (0.832, 0.0)
                            //line1_line from point3_base to point4_dir
                            //Given point (2) point5_sample at (0.6, 0.22)
                            //circle2_circle2 with center point3_base extending to point5_sample (radius 0.5371219600798314)
                            val circleBS = circleTool(base, sample)

                            //point6 at intersection (1/2) of line1_line and circle2_circle2 (-0.4271219600798314, 0.0)
                            val p6 = intersectTwoPoints(circleBS, line).first

                            //circle3 with center point6 extending to point5_sample (radius 1.050418735970677)
                            val c3 = circleTool(p6, sample)

                            //point7 at intersection (2/2) of circle2_circle2 and circle3 (0.6, -0.2200000000000001)
                            val p7 = intersectTwoPoints(circleBS, c3).second

                            //line2_line3 from point7 to point5_sample
                            val perpSample = lineTool(p7, sample)

                            //point8_foot at intersection (1/1) of line1_line and line2_line3 (0.6000000000000001, -0.0)
                            val foot = intersectOnePoint(perpSample, line)

                            //circle4 with center point5_sample extending to point8_foot (radius 0.22)
                            val c4 = circleTool(sample, foot)

                            //point9_point8 at intersection (1/2) of circle1_circle and circle2_circle2 (-0.33095499091390224, 0.3066899019989415)
                            //point10_point7 at intersection (2/2) of circle1_circle and circle2_circle2 (0.38152729156250964, 0.46343600414375213)
                            val lens = intersectTwoPoints(circleBS, circle)
                            val p9 = lens.first
                            val p10 = lens.second

                            //line3_line4 from point9_point8 to point10_point7
                            val link = lineTool(p9, p10)

                            //circle5 with center point8_foot extending to point5_sample (radius 0.22)
                            val c5 = circleTool(foot, sample)

                            //point11 at intersection (1/2) of circle4 and circle5 (0.40947441116742356, 0.1099999999999999)
                            val p11 = intersectTwoPoints(c4, c5).first

                            //point12_point10 at intersection (1/1) of line2_line3 and line3_line4 (0.6, 0.5115)
                            val hub = intersectOnePoint(link, perpSample)

                            //circle6 with center point11 extending to point12_point10 (radius 0.4444122523063468)
                            val c6 = circleTool(p11, hub)

                            //circle7 with center point8_foot extending to point12_point10 (radius 0.5115)
                            val c7 = circleTool(foot, hub)

                            //point13 at intersection (1/2) of line2_line3 and circle6 (0.6, -0.2915000000000002)
                            val p13 = intersectTwoPoints(c6, perpSample).first

                            //circle8_circle3 with center point5_sample extending to point13 (radius 0.5115000000000002)
                            val c8 = circleTool(sample, p13)

                            //circle9 with center point12_point10 extending to point8_foot (radius 0.5115)
                            val c9 = circleTool(hub, foot)

                            //point14 at intersection (2/2) of circle7 and circle9 (0.1570280059642597, 0.25574999999999987)
                            val p14 = intersectTwoPoints(c7, c9).second

                            //point15_point11 at intersection (1/2) of line1_line and circle8_circle3 (0.1382292235318478, 2.7755575615628914E-17)
                            val p15 = intersectTwoPoints(line, c8).first

                            //circle10 with center point14 extending to point15_point11 (radius 0.25643996708965056)
                            val c10 = circleTool(p14, p15)

                            //point16 at intersection (1/2) of circle7 and circle9 (1.0429719940357405, 0.2557500000000001)
                            val p16 = intersectTwoPoints(c7, c9).first

                            //circle11 with center point16 extending to point15_point11 (radius 0.9401954814181247)
                            val c11 = circleTool(p16, p15)

                            //point17_point11 at intersection (2/2) of circle10 and circle11 (0.13822922353184774, 0.5114999999999996)
                            val p17 = intersectTwoPoints(c10, c11).second

                            //circle12_circle4 with center point12_point10 extending to point17_point11 (radius 0.46177077646815223)
                            val c12 = circleTool(hub, p17)

                            val tangentPoints = intersectTwoPoints(c12, circle, other)
                            val tangentPoint = tangentPoints.first
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
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
                            val circleBS = circleTool(base, sample)
                            val baseOpp = intersectTwoPoints(circleBS, line).first
                            val circleBSOpp = circleTool(baseOpp, sample)
                            val sampleOpp = intersectTwoPoints(circleBS, circleBSOpp).second
                            val perpSample = lineTool(sampleOpp, sample)
                            val foot = intersectOnePoint(perpSample, line)
                            val lens = intersectTwoPoints(circleBS, circle)
                            val p9 = lens.first
                            val p10 = lens.second
                            val link = lineTool(p9, p10)
                            val hub = intersectOnePoint(link, perpSample)

                            val circleSF = circleTool(sample, foot)
                            val circleFS = circleTool(foot, sample)
                            val lensSF = intersectTwoPoints(circleSF, circleFS)
                            val lensSF1 = lensSF.first
                            val lensSF2 = lensSF.second
                            val circleToHub1 = circleTool(lensSF1, hub)
                            val circleToHub2 = circleTool(sample, hub)
                            val down = intersectTwoPoints(circleToHub1, perpSample).first
                            val circleSampleDown = circleTool(sample, down)
                            val up = intersectTwoPoints(perpSample, circleSampleDown).second
                            val circleFromUp = circleTool(up, lensSF2)
                            val aim = intersectTwoPoints(circleToHub2, circleFromUp).first
                            val circleKey = circleTool(hub, aim)

                            val tangentPoints = intersectTwoPoints(circleKey, circle, other)
                            val tangentPoint = tangentPoints.first
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal17ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
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
                            val p20 = intersectTwoPoints(circle, l2, other).second

                            //line5_line4 from point19_point11 to point20_point12
                            val l5 = lineTool(p19, p20)

                            //point21_tangentPoint at intersection (1/2) of circle1_circle and line5_line4 (0.26660266382887, 0.3854796991618019)
                            val tangentPoint = intersectTwoPoints(circle, l5).first

                            //line6_diameter from point21_tangentPoint to point1_center
                            val diameter = lineTool(tangentPoint, center)

                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal9L17ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
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

                            val circle3 = circleTool(point9, sample)
                            val rect = parallelTool(line2, point8, probe = point5)

                            val point10 = intersectTwoPoints(circle3, rect).first

                            //circle4 with center point6_sample from distance between point9 and point10 (radius 0.8745443419989918)
                            val circle4 = nonCollapsingCompassTool(point8, point10, sample)

                            //point11 at intersection (1/2) of line3 and circle4 (0.0232142857142853, 0.9517857142857145)
                            val point11 = intersectTwoPoints(circle4, line3).first

                            //point12 at intersection (1/2) of circle1_circle and line2 (0.0, 1.016227766016838)
                            val point12 = intersectTwoPoints(line2, circle, other).first

                            //line4 from point11 to point12
                            val line4 = lineTool(point11, point12)
                            val tangentPoints = intersectTwoPoints(line4, circle)
                            val tangentPoint = tangentPoints.first
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }

        private fun suboptimal16L16ESolution(other: Boolean): (Params, Namer) -> Pair<Setup, EuclideaContext> {
            return { params, namer ->
                val (setup, initialContext) = initialContext(
                    params, namer
                )
                with(params) {
                    with(setup) {
                        @Suppress("unused") val context = object {
                            //Given point (1) point1_center at (0.0, 0.7)
                            //point2_sample1 at (0.4, 0.8)
                            //circle1_circle with center point1_center extending to point2_sample1 (radius 0.4123105625617661)
                            //Given point (3) point3_base at (0.1, 0.0)
                            //point4_dir at (0.9, 0.0)
                            //line1_line from point3_base to point4_dir
                            //circle2_c2 with center point3_base extending to point1_center (radius 0.7071067811865475)
                            val c2 = circleTool(base, center)

                            //point5_p5 at intersection (1/2) of line1_line and circle2_c2 (-0.6071067811865474, 0.0)
                            val p5 = intersectTwoPoints(line, c2).first

                            //circle3_c3 with center point5_p5 extending to point1_center (radius 0.926595188721963)
                            val c3 = circleTool(p5, center)

                            //point6_p6 at intersection (2/2) of circle2_c2 and circle3_c3 (2.7755575615628914E-17, -0.7)
                            val p6 = intersectTwoPoints(c2, c3).second

                            //line2_line2 from point6_p6 to point1_center
                            val line2 = perpendicularTool(line, center, probe = base)

                            //point7_point5 at intersection (1/1) of line1_line and line2_line2 (1.3877787807814457E-17, -0.0)
                            val point5 = intersectOnePoint(line, line2)

                            //Given point (2) point8_sample at (1.0, 0.3)
                            //circle4_circle2 with center point7_point5 extending to point8_sample (radius 1.044030650891055)
                            val circle2 = circleTool(point5, sample)

                            //point9_point7 at intersection (1/2) of circle1_circle and circle4_circle2 (-0.2750695644852816, 1.0071428571428571)
                            val intersection = intersectTwoPoints(circle2, circle)
                            val point7 = intersection.second

                            //line3 from point9_point7 to point7_point5
                            val l3 = lineTool(point7, point5)

                            //point10 at intersection (2/2) of circle4_circle2 and line3 (0.2750695644852816, -1.0071428571428571)
                            val p10 = intersectTwoPoints(circle2, l3).second

                            //point11_point8 at intersection (2/2) of circle1_circle and circle4_circle2 (0.2750695644852816, 1.0071428571428571)
                            val point8 = intersection.first

                            //line4_rect from point10 to point11_point8
                            val rect = parallelTool(line2, point8, probe = point5)

                            //line5_line3 from point9_point7 to point11_point8
                            val line3 = lineTool(point7, point8)

                            //point12_point9 at intersection (1/1) of line2_line2 and line5_line3 (-6.08923342587777E-18, 1.007142857142857)
                            val point9 = intersectOnePoint(line3, line2)

                            //circle5_c6 with center point12_point9 extending to point9_point7 (radius 0.2750695644852816)
                            val c5 = circleTool(point9, point7)

                            //circle6_circle3 with center point12_point9 extending to point8_sample (radius 1.2247657002088859)
                            val circle3 = circleTool(point9, sample)

                            //point13_point10 at intersection (2/2) of line4_rect and circle6_circle3 (0.27506956448528164, 2.200620027055227)
                            val point10 = intersectTwoPoints(circle3, rect).second

                            //circle7 with center point8_sample extending to point13_point10 (radius 2.034178119924352)
                            val c7 = circleTool(sample, point10)

                            //circle8 with center point13_point10 extending to point8_sample (radius 2.034178119924352)
                            val c8 = circleTool(point10, sample)

                            //point14 at intersection (2/2) of circle7 and circle8 (-1.0084504441286533, 0.6225018403953506)
                            val p14 = intersectTwoPoints(c7, c8).second

                            //circle9 with center point14 extending to point11_point8 (radius 1.3399150436788412)
                            val c9 = circleTool(p14, point8)

                            //point15 at intersection (1/2) of circle5_c6 and circle9 (0.2052001272257332, 1.190326298241296)
                            val p15 = intersectTwoPoints(c5, c9).first

                            //circle10_circle4 with center point8_sample extending to point15 (radius 1.1934771699123699)
                            val circle4 = circleTool(sample, p15)

                            //point16_point11 at intersection (1/2) of line5_line3 and circle10_circle4 (0.03857567396394812, 1.007142857142857)
                            val point11 = intersectTwoPoints(circle4, line3).first

                            //point17_point12 at intersection (2/2) of circle1_circle and line2_line2 (-8.174226425932569E-18, 1.112310562561766)
                            val point12 = intersectTwoPoints(line2, circle, other).first

                            //line6_line4 from point16_point11 to point17_point12
                            val line4 = lineTool(point11, point12)

                            //point18_tangentPoint at intersection (1/2) of circle1_circle and line6_line4 (0.26660266382887643, 0.3854796991618074)
                            val tangentPoints = intersectTwoPoints(line4, circle)
                            val tangentPoint = tangentPoints.first

                            //line7_diameter from point18_tangentPoint to point1_center
                            val diameter = lineTool(center, tangentPoint)
                            val solutionCenter = intersectOnePoint(diameter, line)
                            // val solution = circleTool(solutionCenter, sample)
                        }
                        namer.nameReflected(context)
                        setup to initialContext.withElements(elementsReflected(context))
                    }
                }
            }
        }
    }
}
