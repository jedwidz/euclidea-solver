package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import org.junit.jupiter.api.Test

class SolvePuzzle9_9Test {
    // Triangle by Angle and Centroid

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(5, 7)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseB: Point,
        val center: Point,
        val probe1Scale: Double,
        val probe2Scale: Double
    ) {
        val probe1 = baseB + (baseA - baseB) * probe1Scale
        val probe2 = baseA + (baseB - baseA) * probe2Scale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                baseB = Point(0.4, 0.8),
                center = Point(0.35, 0.5),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.01, 0.0),
                baseB = Point(0.401, 0.8005),
                center = Point(0.350101, 0.503),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseO, baseA, limit1 = true)
                    val line2 = Element.Line(baseO, baseB, limit1 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(15.0)
                        ),
                        points = listOf(baseO, baseA, baseB, center/*, probe1, probe2*/),
                        elements = listOf(line1, line2)
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

        private fun constructSolution(params: Params): Element.Line {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // From optimal 6L solution
                    val parallel1 = parallelTool(line1, center)
                    val other1 = intersectOnePoint(parallel1, line2)
                    val parallel2 = parallelTool(line2, center)
                    val other2 = intersectOnePoint(parallel2, line1)
                    val other = lineTool(other1, other2)
                    val circle = circleTool(center, other1)
                    val target = intersectTwoPointsOther(circle, parallel1, other1)
                    val solution = parallelTool(other, target)
                    return solution
                }
            }
        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solutionElements = constructSolution(params)
//            return { context ->
//                solutionElements.count { !context.hasElement(it) }
//            }
//        }

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 5E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> !element.isCircleFromCircle
//                    1 -> !element.isCircleFromCircle
//                    2 -> !element.isLineFromLine
//                    3 -> !element.isCircleFromCircle
//                    4 -> !element.isLineFromLine
//                    5 -> !element.isCircleFromCircle
//                    6 -> !element.isLineFromLine
//                    7 -> !element.isLineFromLine
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
                    // Optimal 6L solution
                    @Suppress("unused") val context = object {
                        // Optimal 6L solution
                        val parallel1 = parallelTool(line1, center)
                        val other1 = intersectOnePoint(parallel1, line2)
                        val parallel2 = parallelTool(line2, center)
                        val other2 = intersectOnePoint(parallel2, line1)
                        val other = lineTool(other1, other2)
                        val circle = circleTool(center, other1)
                        val target = intersectTwoPointsOther(circle, parallel1, other1)
                        val solution = parallelTool(other, target)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
