package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class SolvePuzzle11_7Test {
    // Geometric Mean of Trapezoid Bases

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            // gave up 1 hr 11 min
            maxExtraElements = 3,
            maxDepth = 9,
            nonNewElementLimit = 5,
            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA1: Point,
        val baseA2: Point,
        val baseB1: Point,
        val baseBScale: Double,
        val probe: Point
    ) {
        val baseB2 = baseB1 + (baseA2 - baseA1) * baseBScale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val side1: Element.Line,
        val side2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA1 = Point(0.0, 0.0),
                baseA2 = Point(0.7, 0.0),
                baseB1 = Point(0.2, 0.6),
                baseBScale = 0.4,
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA1 = Point(0.0, 0.0),
                baseA2 = Point(0.7043, 0.0034),
                baseB1 = Point(0.2043, 0.604),
                baseBScale = 0.4011,
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineA = Element.Line(baseA1, baseA2, limit1 = true, limit2 = true)
                    val lineB = Element.Line(baseB1, baseB2, limit1 = true, limit2 = true)
                    val side1 = Element.Line(baseA1, baseB1, limit1 = true, limit2 = true)
                    val side2 = Element.Line(baseA2, baseB2, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineA, lineB, side1, side2) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(10.0)
                        ),
                        // dir excluded
                        points = listOf(baseA1, baseA2, baseB1, baseB2 /*, probe*/),
                        elements = listOf(lineA, lineB, side1, side2)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                with(setup) {
                    val oa = (baseA2 - baseA1).distance
                    val ob = (baseB2 - baseB1).distance
                    val oc = sqrt(oa * ob)
                    return { context ->
                        val last = context.elements.last()
                        last is Element.Line && linesParallel(last, line1) &&
                                onePointIntersection(last, side1)?.let { intersect1 ->
                                    onePointIntersection(last, side2)?.let { intersect2 ->
                                        coincides(oc, distance(intersect1, intersect2))
                                    }
                                } ?: false
                    }
                }
            }
        }

        private fun constructSolution(params: Params): Element.Line {
            // cheekily use reference solution
            return referenceSolution(params, Namer()).second.elements.last() as Element.Line
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            with(setup) {
                val solution = constructSolution(params)
                val point1 = intersectOnePoint(solution, side1)
                val point2 = intersectOnePoint(solution, side2)
                return { context ->
                    // Assumes that solution is the last element (no extraneous elements)
                    if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
                        0
                    else {
                        val onPoint1 = context.elements.count { pointAndElementCoincide(point1, it) }
                        val onPoint2 = context.elements.count { pointAndElementCoincide(point2, it) }
                        // Assume solution uses at least one of the highlighted points
                        max(0, min(2 - onPoint1, 2 - onPoint2)) + 1
                    }
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 9E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isLineFromLine
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
                    5 -> !element.isCircleFromCircle
                    6 -> !element.isLineFromLine
                    7 -> !element.isCircleFromCircle
                    8 -> !element.isLineFromLine
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
                        // Sub-optimal 8L/9L solution; fussy about starting parameters
                        val extendA = lineTool(baseA1, baseA2)
                        val circle1 = nonCollapsingCompassTool(baseB1, baseB2, baseA1)
                        val left = intersectTwoPoints(circle1, extendA).first
                        val bisect = perpendicularBisectorTool(left, baseA2)
                        val perp = perpendicularTool(side1, baseA1, probe = baseA2)
                        val center2 = intersectOnePoint(perp, bisect)
                        val circle2 = circleTool(center2, baseA2)

                        // Can make this an 8L solution by tweaking parameters, and using side1 directly
                        val extend1 = lineTool(baseA1, baseB1)

                        // val sample3 = intersectOnePoint(circle2, side1)
                        val sample3 = intersectTwoPoints(circle2, extend1).second
                        val circle3 = circleTool(baseA1, sample3)
                        val point = intersectOnePoint(circle3, line1)
                        val parallel = parallelTool(side1, point, probe = baseA1)
                        val solutionP2 = intersectOnePoint(parallel, side2)
                        val solution = parallelTool(line1, solutionP2, probe = baseA2)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal6LSolution)
        }

        private fun optimal6LSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 6L solution
                        val circle1 = nonCollapsingCompassTool(baseB1, baseB2, baseA1)
                        val right = intersectOnePoint(circle1, line1)
                        val circle2 = circleTool(right, baseA1)
                        val bisect = perpendicularBisectorTool(baseA1, baseA2)
                        val center3 = intersectTwoPoints(bisect, circle2).second
                        val circle3 = circleTool(baseA1, center3)
                        val point = intersectTwoPointsOther(circle3, line1, baseA1)
                        val parallel = parallelTool(side1, point, probe = baseA1)
                        val solutionP2 = intersectOnePoint(parallel, side2)
                        val solution = parallelTool(line1, solutionP2, probe = baseA2)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
