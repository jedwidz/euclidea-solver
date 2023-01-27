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
        // no solution found ?
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 7,
//            nonNewElementLimit = 3,
//            consecutiveNonNewElementLimit = 2,
            useTargetConstruction = true
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
                sample1 = Point(0.3, 0.6),
                base = Point(0.1, 0.0),
                dir = Point(0.9, 0.0),
                sample = Point(0.8, 0.55),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.721),
                sample1 = Point(0.312, 0.608),
                base = Point(0.1011, 0.0),
                dir = Point(0.9011, 0.0),
                sample = Point(0.81, 0.574),
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
                            nonCollapsingCompassToolEnabled = true,
                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // sample1, base and dir act as probes
                        points = listOf(center, sample /*, base, sample1 , dir */),
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
                context.elements.lastOrNull()
                    ?.let { element ->
                        pointAndElementCoincide(pointOfInterest, element) &&
                                // Just checking point/line has some false positives with 'almost coincident' lines
                                // Note this still avoids evaluating context points in most cases
                                context.hasPoint(pointOfInterest)
                    } == true
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
                        val perp = perpendicularTool(line, center, probe = base)

                        // Likely next element...
                        val point5 = intersectOnePoint(line, perp)
                        val circle2 = circleTool(point5, sample)

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

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 7L L-star moves hint
            return listOf(
                EuclideaTool.PerpendicularTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.NonCollapsingCompassTool,
                EuclideaTool.NonCollapsingCompassTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool
            )
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
                        val point12 = intersectTwoPoints(line2, circle).first

                        //line4 from point11 to point12
                        val line4 = lineTool(point11, point12)
                        val tangentPoint = intersectTwoPoints(line4, circle).first
                        val diameter = lineTool(center, tangentPoint)
                        val solutionCenter = intersectOnePoint(diameter, line)
//                        val solution = circleTool(solutionCenter, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::alternateSolution)
//        }
//
//        fun alternateSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 5L solution
//                        val lineA = lineTool(center, baseA)
//                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
//                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
//
//                        // Needs to be the 'further away' intersection
//                        val pointC = intersectTwoPoints(bisectAB, circle).second
//                        val solutionC = perpendicularTool(bisectAB, pointC, probe = baseA)
//                        val vertex = intersectOnePoint(bisectAB, solutionA)
//                        val solutionB = lineTool(vertex, baseB)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
