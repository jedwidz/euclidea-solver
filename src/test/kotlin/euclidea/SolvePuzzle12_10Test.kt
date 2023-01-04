package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
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
        // no solution found 2 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 5,
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
                    return Setup(circle, baseA, baseB) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
                            parallelToolEnabled = true,
                            perpendicularBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true,
                            perpendicularToolEnabled = true,
                            angleBisectorToolEnabled = true,
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
                context.hasElements(solution)
            }
        }

        private fun constructSolution(params: Params): List<Element.Line> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
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

                    return listOf(solutionA, solutionB, solutionC)
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
            val solutionElements = constructSolution(params)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
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
//                    5 -> !element.isCircleFromCircle
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

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::optimal6LSolution)
//        }
//
//        fun optimal6LSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 6L solution
//                        val bisectAB = perpendicularBisectorTool(center, dirA)
//                        val midAB = intersectOnePoint(bisectAB, lineAB)
//                        val cross = lineTool(midAB, dirB)
//                        val circle1 = circleTool(dirB, midAB)
//                        val doubled = intersectTwoPointsOther(circle1, cross, midAB)
//                        val circle2 = circleTool(doubled, dirB)
//                        val tripled = intersectTwoPointsOther(circle2, cross, dirB)
//                        val solutionA = lineTool(center, tripled)
//                        val solutionB = lineTool(dirA, tripled)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
