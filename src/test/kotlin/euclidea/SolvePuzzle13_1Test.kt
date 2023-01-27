package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle13_1Test {
    // Circle Tangent to Line and Circle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 8,
//            maxNonNewElements = 6,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val radius: Double,
        val base: Point,
        val dir: Point,
    )

    data class Setup(
        val circle: Element.Circle,
        val line: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                radius = 1.0,
                base = Point(0.4, -1.5),
                dir = Point(0.0, -1.5),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                radius = 1.001,
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
                    val circle = Element.Circle(center, radius)
                    val line = Element.Line(base, dir)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, line) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(center, base),
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
                    // Sub-optimal 5L solution
                    val perpCenter = perpendicularTool(line, center, probe = base)
                    val aim1 = intersectTwoPoints(perpCenter, circle).second
                    val cross1 = lineTool(aim1, base)
                    val touch = intersectTwoPoints(cross1, circle).second
                    val cross2 = lineTool(touch, center)
                    val perpBase = perpendicularTool(line, base, probe = touch)
                    val solutionCenter = intersectOnePoint(perpBase, cross2)
                    val solution = circleTool(solutionCenter, touch)
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

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 7E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isLineFromLine
//                    1 -> !element.isCircleFromCircle
//                    2 -> !element.isCircleFromCircle
//                    3 -> !element.isLineFromLine
//                    4 -> !element.isLineFromLine
//                    5 -> !element.isLineFromLine
//                    6 -> !element.isLineFromLine
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
                        // Sub-optimal 5L solution
                        val perpCenter = perpendicularTool(line, center, probe = base)
                        val aim1 = intersectTwoPoints(perpCenter, circle).second
                        val cross1 = lineTool(aim1, base)
                        val touch = intersectTwoPoints(cross1, circle).second
                        val cross2 = lineTool(touch, center)
                        val perpBase = perpendicularTool(line, base, probe = touch)
                        val solutionCenter = intersectOnePoint(perpBase, cross2)
                        val solution = circleTool(solutionCenter, touch)
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
