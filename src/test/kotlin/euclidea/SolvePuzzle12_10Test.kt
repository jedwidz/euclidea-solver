package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle12_10Test {
    // Isosceles Triangle by Tangent Points

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 7,
//            nonNewElementLimit = 7,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val dirA: Point,
        val dirB: Point,
        val radius: Double,
    )

    data class Setup(
        val circle: Element.Circle,
        val baseA: Point,
        val baseB: Point,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                dirA = Point(-0.4, 0.3),
                dirB = Point(0.0, -0.5),
                radius = 1.0,
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                dirA = Point(-0.401, 0.3022),
                dirB = Point(0.0013, -0.533),
                radius = 1.0031,
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = Element.Circle(center, radius)
                    val baseA = intersectTwoPoints(circle, lineTool(center, dirA)).second
                    val baseB = intersectTwoPoints(circle, lineTool(center, dirB)).second
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, baseA, baseB) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(center, baseA, baseB),
                        elements = listOf(circle)
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
                solution.any { context.hasElements(it) }
            }
        }

        private fun constructSolution(params: Params): List<List<Element.Line>> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Accept either of the following solutions (third possibility omitted for symmetry)...

                    val solution1 = run {
                        // Sub-optimal 6L solution
                        val lineA = lineTool(center, baseA)
                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
                        val lineB = lineTool(center, baseB)
                        val solutionB = perpendicularTool(lineB, baseB, probe = baseA)
                        val vertex1 = intersectOnePoint(solutionA, solutionB)
                        val measure = circleTool(center, vertex1)
                        val vertex2 = intersectOnePoint(lineB, solutionA)
                        val vertex3 = intersectTwoPointsOther(measure, solutionB, vertex1)
                        val solutionC = lineTool(vertex2, vertex3)
                        listOf(solutionA, solutionB, solutionC)
                    }

                    val solution2 = run {
                        // Optimal 5L solution
                        val lineA = lineTool(center, baseA)
                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        // Needs to be the 'further away' intersection
                        val pointC = intersectTwoPoints(bisectAB, circle).second
                        val solutionC = perpendicularTool(bisectAB, pointC, probe = baseA)
                        val vertex = intersectOnePoint(bisectAB, solutionA)
                        val solutionB = lineTool(vertex, baseB)
                        listOf(solutionA, solutionB, solutionC)
                    }

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
//                        val half = EuclideaTools.angleBisectorTool(baseA, baseO, baseB)
//                        // val perp = perpendicularTool(line2, sample, probe = baseO)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutions = constructSolution(params)
            return { context ->
                solutions.map { solutionElements -> solutionElements.count { !context.hasElement(it) } }.min()
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 7E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isLineFromLine
                    4 -> !element.isLineFromLine
                    5 -> !element.isLineFromLine
                    6 -> !element.isLineFromLine
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
                        // Sub-optimal 6L solution
                        val lineA = lineTool(center, baseA)
                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
                        val lineB = lineTool(center, baseB)
                        val solutionB = perpendicularTool(lineB, baseB, probe = baseA)
                        val vertex1 = intersectOnePoint(solutionA, solutionB)
                        val measure = circleTool(center, vertex1)
                        val vertex2 = intersectOnePoint(lineB, solutionA)
                        val vertex3 = intersectTwoPointsOther(measure, solutionB, vertex1)
                        val solutionC = lineTool(vertex2, vertex3)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::alternateSolution)
        }

        fun alternateSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 5L solution
                        val lineA = lineTool(center, baseA)
                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)

                        // Needs to be the 'further away' intersection
                        val pointC = intersectTwoPoints(bisectAB, circle).second
                        val solutionC = perpendicularTool(bisectAB, pointC, probe = baseA)
                        val vertex = intersectOnePoint(bisectAB, solutionA)
                        val solutionB = lineTool(vertex, baseB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
