package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle13_6Test {
    // Circle Through Two Points and Tangent to Line

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 10,
//            nonNewElementLimit = 7,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
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
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(-0.2, 0.9),
                pointB = Point(-1.0, 0.0),
                base = Point(0.4, -1.5),
                dir = Point(0.0, -1.5),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(-0.203, 0.9011),
                pointB = Point(-1.0111, 0.002),
                base = Point(0.4022, -1.503),
                dir = Point(0.0, -1.5013),
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line = Element.Line(base, dir)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
//                            parallelToolEnabled = true,
                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // base is included as a probe point
                        points = listOf(pointA, pointB, base),
                        elements = listOf(line)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            return { context ->
                context.hasElement(solution)
            }
        }

        private fun constructSolution(params: Params): Element.Circle {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 7L solution
                    val bisect = perpendicularBisectorTool(pointA, pointB)
                    val intercept = intersectOnePoint(bisect, line)
                    val lineB = lineTool(intercept, pointB)
                    val perp = perpendicularTool(line, base, probe = pointB)
                    val center1 = intersectOnePoint(perp, bisect)
                    val circle1 = circleTool(center1, base)
                    val aim1 = intersectTwoPoints(circle1, lineB).first
                    val cross1 = lineTool(center1, aim1)
                    val cross2 = parallelTool(cross1, pointB, probe = center1)
                    val center2 = intersectOnePoint(cross2, bisect)
                    val solution = circleTool(center2, pointB)
                    return solution
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
            val solution = constructSolution(params)
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
            // Euclidea 10E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromPerpendicularBisector
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isLineFromLine
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isCircleFromCircle
                    6 -> !element.isLineFromLine
                    7 -> !element.isCircleFromCircle
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
                        // Sub-optimal 7L solution
                        val bisect = perpendicularBisectorTool(pointA, pointB)
                        val intercept = intersectOnePoint(bisect, line)
                        val lineB = lineTool(intercept, pointB)
                        val perp = perpendicularTool(line, base, probe = pointB)
                        val center1 = intersectOnePoint(perp, bisect)
                        val circle1 = circleTool(center1, base)
                        val aim1 = intersectTwoPoints(circle1, lineB).first
                        val cross1 = lineTool(center1, aim1)
                        val cross2 = parallelTool(cross1, pointB, probe = center1)
                        val center2 = intersectOnePoint(cross2, bisect)
                        val solution = circleTool(center2, pointB)
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
