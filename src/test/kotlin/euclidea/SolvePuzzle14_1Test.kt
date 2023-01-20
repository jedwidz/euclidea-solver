package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle14_1Test {
    // Rhombus in Triangle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // partial solution found 2 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 6,
            nonNewElementLimit = 3,
            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val baseC: Point
    )

    data class Setup(
        val lineAB: Element.Line,
        val lineBC: Element.Line,
        val lineCA: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.4, 0.55)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.402, 0.553)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineAB = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val lineBC = Element.Line(baseB, baseC, limit1 = true, limit2 = true)
                    val lineCA = Element.Line(baseC, baseA, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineAB, lineBC, lineCA) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(5.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(baseA, baseB, baseC),
                        elements = listOf(lineAB, lineBC, lineCA)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // validate solution
            with(setup) {
                with(solution) {
                    assertTrue(linesParallel(lineAB, solutionC))
                    assertTrue(linesParallel(lineCA, solutionB))
                    assertTrue(pointAndLineCoincide(pointD, lineBC))
                }
            }
            val sidePointB = intersectOnePoint(setup.lineAB, solution.solutionB)
            val sidePointC = intersectOnePoint(setup.lineCA, solution.solutionC)
            val sidePoints = listOf(sidePointB, sidePointC)
            return { context ->
                // Partial solution
                sidePoints.any { context.hasPoint(it) }
//                context.hasElements(solution.elements)
            }
        }

        data class Solution(
            val solutionB: Element.Line,
            val solutionC: Element.Line
        ) {
            val elements = listOf(solutionB, solutionC)
            val pointD = intersectOnePoint(solutionB, solutionC)
        }

        private fun constructSolution(params: Params): Solution {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Optimal 3L solution
                    val diagonal = angleBisectorTool(baseB, baseA, baseC)
                    val pointD = intersectOnePoint(diagonal, lineBC)
                    val solutionB = parallelTool(lineCA, pointD, probe = baseA)
                    val solutionC = parallelTool(lineAB, pointD, probe = baseA)

                    return Solution(solutionB, solutionC)
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

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solution = constructSolution(params)
//            val solutionElements = solution.elements
//            val sidePointB = intersectOnePoint(setup.lineAB, solution.solutionB)
//            val sidePointC = intersectOnePoint(setup.lineCA, solution.solutionC)
//            val sidePoints = listOf(sidePointB, sidePointC)
//            return { context ->
//                // Partial solution
//                sidePoints.any { context.hasPoint(it) }
////                val remainingElements = solutionElements.count { !context.hasElement(it) }
////                // Assume Point D found first
////                val remainingPoints =
////                    if (context.hasPoint(solution.pointD)) 0 else 1
////                remainingPoints + remainingElements
////                remainingElements
//            }
//        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 8E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromLine
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isLineFromLine
                    6 -> !element.isLineFromLine
                    7 -> !element.isLineFromLine
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
                        // Optimal 3L solution
                        val diagonal = angleBisectorTool(baseB, baseA, baseC)
                        val pointD = intersectOnePoint(diagonal, lineBC)
                        val solutionB = parallelTool(lineCA, pointD, probe = baseA)
                        val solutionC = parallelTool(lineAB, pointD, probe = baseA)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::suboptimal9ESolution)
        }

        fun suboptimal9ESolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Sub-optimal 9E solution
                        val diagonal = angleBisectorTool(baseB, baseA, baseC)
                        val pointD = intersectOnePoint(diagonal, lineBC)
                        val perpDiagonal = perpendicularBisectorTool(baseA, pointD)
                        val sidePointB = intersectOnePoint(perpDiagonal, lineAB)
                        val sidePointC = intersectOnePoint(perpDiagonal, lineCA)
                        val solutionB = lineTool(sidePointB, pointD)
                        val solutionC = lineTool(sidePointC, pointD)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
