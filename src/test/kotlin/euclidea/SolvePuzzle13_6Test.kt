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
        // improved suboptimal solution found 34 sec
        Solver().improveSolution(
            maxExtraElements = 1,
            maxDepth = 8,
            maxUnfamiliarElements = 0,
            maxNonNewElements = 3,
            maxConsecutiveNonNewElements = 2,
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
                            maxSqDistance = sq(16.0),
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

//        override fun toolSequence(): List<EuclideaTool> {
//            // Euclidea 10E E-star moves hint
//            return listOf(
//                // Moved to setup
//                // EuclideaTool.PerpendicularBisectorTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                // This is skipped, just look for center of a solution
//                EuclideaTool.CircleTool
//            )
//        }

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
                optimal6LSolution(true)
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
    }
}
