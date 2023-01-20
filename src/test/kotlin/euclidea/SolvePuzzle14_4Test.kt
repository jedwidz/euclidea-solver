package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertEquals

class SolvePuzzle14_4Test {
    // Parallelogram on Four Lines

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // ?
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 9,
//            nonNewElementLimit = 7,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val baseA: Point,
        val baseB: Point,
        val baseC: Point,
        val baseDScale: Double
    ) {
        val baseD = baseC + (baseB - baseA) * baseDScale
    }

    data class Setup(
        val lineAB: Element.Line,
        val lineAC: Element.Line,
        val lineBD: Element.Line,
        val lineCD: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                baseA = Point(-0.4, -0.3),
                baseB = Point(0.6, -0.3),
                baseC = Point(-0.2, 0.15),
                baseDScale = 0.6
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                baseA = Point(-0.401, -0.302),
                baseB = Point(0.603, -0.304),
                baseC = Point(-0.205, 0.156),
                baseDScale = 0.607
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineAB = Element.Line(baseA, baseB)
                    val lineAC = Element.Line(baseA, baseC)
                    val lineBD = Element.Line(baseB, baseD)
                    val lineCD = Element.Line(baseC, baseD)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineAB, lineAC, lineBD, lineCD) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(8.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(baseA, baseB, baseC, baseD, center),
                        elements = listOf(lineAB, lineAC, lineBD, lineCD)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            validateSolution(setup, solution)
            val solutionElements = solution.elements
            return { context ->
//                context.hasElements(solution)
                // partial solution - just the first line
                solutionElements.any { context.hasElement(it) }
            }
        }

        private fun validateSolution(setup: Setup, solution: Solution) {
            val solutionLines = LineSet.of(solution.elements)
            val solutionPoints = PointSet.of(solution.points)
            val baseLines = with(setup) {
                listOf(lineAB, lineAC, lineBD, lineCD)
            }
            assertEquals(4, solutionLines.size)
            assertEquals(4, solutionPoints.size)

            val invalidLinePairs = solutionLines.items().pairs().filterNot { (line1, line2) ->
                when (val intersection = intersect(line1, line2)) {
                    Intersection.Disjoint -> true
                    is Intersection.OnePoint -> intersection.point in solutionPoints
                    else -> false
                }
            }
            assertEquals(listOf(), invalidLinePairs)

            val baseLineWithoutSolutionPoint = baseLines.filterNot { baseLine ->
                solutionPoints.items().any { solutionPoint ->
                    pointAndLineCoincide(solutionPoint, baseLine)
                }
            }
            assertEquals(listOf(), baseLineWithoutSolutionPoint)
        }

        data class Solution(
            val solution1: Element.Line,
            val solution2: Element.Line,
            val solution3: Element.Line,
            val solution4: Element.Line,
            val solutionAB: Point,
            val solutionAC: Point,
            val solutionBD: Point,
            val solutionCD: Point
        ) {
            val elements = listOf(solution1, solution2, solution3, solution4)
            val points = listOf(solutionAB, solutionAC, solutionBD, solutionCD)
        }

        private fun constructSolution(params: Params): Solution {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 9L solution
                    val parAB = parallelTool(lineAB, center, probe = baseA)
                    val onAC = intersectOnePoint(parAB, lineAC)
                    val circleToC = circleTool(onAC, baseC)
                    val solutionAC = intersectTwoPointsOther(circleToC, lineAC, baseC)
                    val parBD = parallelTool(lineBD, center, probe = baseB)
                    val onAB = intersectOnePoint(parBD, lineAB)
                    val circleToB = circleTool(onAB, baseB)
                    val solutionAB = intersectTwoPointsOther(circleToB, lineAB, baseB)
                    val solution1 = lineTool(solutionAB, solutionAC)
                    val diagonal = lineTool(solutionAC, center)
                    val solutionCD = intersectOnePoint(diagonal, lineCD)
                    val solution2 = lineTool(solutionAB, solutionCD)
                    val solution4 = parallelTool(solution2, solutionAC, probe = solutionAB)
                    val solutionBD = intersectOnePoint(solution4, lineBD)
                    val solution3 = lineTool(solutionBD, solutionCD)

                    return Solution(
                        solution1,
                        solution2,
                        solution3,
                        solution4,
                        solutionAB,
                        solutionAC,
                        solutionBD,
                        solutionCD
                    )
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
            val solutionElements = solution.elements
            val solutionPoints = solution.points
            return { context ->
//                solutionElements.count { !context.hasElement(it) }
                // Assume that two solution points are first found separately
                val targetElementCount = 2
                val foundElementCount = solutionElements.count { !context.hasElement(it) }
                val remainingElements = max(0, targetElementCount - foundElementCount)
                if (remainingElements < targetElementCount)
                    remainingElements
                else {
                    val remainingByPoint = solutionPoints.map { point ->
                        max(
                            0,
                            2 - context.elements.count { pointAndElementCoincide(point, it) })
                    }
                    remainingByPoint.sorted().subList(0, 2).sum() + targetElementCount
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 12E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromParallel
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
                    5 -> !element.isLineFromLine
                    6 -> !element.isLineFromLine
                    7 -> !element.isLineFromLine
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
                        // Sub-optimal 9L solution
                        val parAB = parallelTool(lineAB, center, probe = baseA)
                        val onAC = intersectOnePoint(parAB, lineAC)
                        val circleToC = circleTool(onAC, baseC)
                        val solutionAC = intersectTwoPointsOther(circleToC, lineAC, baseC)
                        val parBD = parallelTool(lineBD, center, probe = baseB)
                        val onAB = intersectOnePoint(parBD, lineAB)
                        val circleToB = circleTool(onAB, baseB)
                        val solutionAB = intersectTwoPointsOther(circleToB, lineAB, baseB)
                        val solution1 = lineTool(solutionAB, solutionAC)
                        val diagonal = lineTool(solutionAC, center)
                        val solutionCD = intersectOnePoint(diagonal, lineCD)
                        val solution2 = lineTool(solutionAB, solutionCD)
                        val solution4 = parallelTool(solution2, solutionAC, probe = solutionAB)
                        val solutionBD = intersectOnePoint(solution4, lineBD)
                        val solution3 = lineTool(solutionBD, solutionCD)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal8LSolution)
        }

        fun optimal8LSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 8L solution
                        val parAB = parallelTool(lineAB, center, probe = baseA)
                        val onAC = intersectOnePoint(parAB, lineAC)
                        val circleToC = circleTool(onAC, baseC)
                        val solutionAC = intersectTwoPointsOther(circleToC, lineAC, baseC)
                        val onBD = intersectOnePoint(parAB, lineBD)
                        val circleToB = circleTool(onBD, baseB)
                        val solutionBD = intersectTwoPointsOther(circleToB, lineBD, baseB)
                        val solution4 = lineTool(solutionBD, solutionAC)
                        val diagonal = lineTool(solutionAC, center)
                        val solutionCD = intersectOnePoint(diagonal, lineCD)
                        val solution3 = lineTool(solutionBD, solutionCD)
                        val solution2 = parallelTool(solution4, solutionCD, probe = solutionAC)
                        val solutionAB = intersectOnePoint(solution2, lineAB)
                        val solution1 = lineTool(solutionAB, solutionAC)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
