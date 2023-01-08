package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle13_5Test {
    // Point Equidistant from Side of Angle and Point

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 10 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 5,
//            nonNewElementLimit = 5,
//            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseB: Point,
        val dirA: Point,
        val dirC: Point,
        val baseM: Point
    )

    data class Setup(
        val rayA: Element.Line,
        val rayC: Element.Line
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseB = Point(0.0, 0.0),
                dirA = Point(1.0, 0.0),
                dirC = Point(0.9, 0.7),
                baseM = Point(0.7, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseB = Point(0.0, 0.0),
                dirA = Point(1.001, 0.002),
                dirC = Point(0.903, 0.704),
                baseM = Point(0.7051, 0.3061)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val rayA = Element.Line(baseB, dirA, limit1 = true)
                    val rayC = Element.Line(baseB, dirC, limit1 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(rayA, rayC) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(8.0)
                        ),
                        // dirA and dirC act as probe points
                        points = listOf(baseB, baseM, dirA/*, dirC */),
                        elements = listOf(rayA, rayC)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params, setup)
            val solutionElements = solution.elements
            with(params) {
                with(setup) {
                    with(solution) {
                        assertTrue(pointAndLineCoincide(solutionD, solutionDE))
                        assertTrue(pointAndLineCoincide(solutionE, solutionDE))
                        assertTrue(pointAndLineCoincide(solutionD, solutionDM))
                        assertTrue(pointAndLineCoincide(baseM, solutionDM))
                        assertTrue(pointAndLineCoincide(solutionD, rayA))
                        assertTrue(pointAndLineCoincide(solutionE, rayC))
                        assertTrue(coincides(distance(solutionD, solutionE), distance(solutionD, baseM)))
                        assertTrue(linesPerpendicular(rayC, solutionDE))
                    }
                }
            }
            return { context ->
                context.hasElements(solutionElements)
            }
        }

        data class Solution(
            val solutionDE: Element.Line,
            val solutionDM: Element.Line,
            val solutionD: Point,
            val solutionE: Point
        ) {
            val elements = listOf(solutionDE, solutionDM)
        }

        private fun constructSolution(params: Params, setup: Setup): Solution {
            // based on reference solution
            with(params) {
                with(setup) {
                    // Sub-optimal 6L solution
                    val lineBM = lineTool(baseB, baseM)
                    val sampleDE = perpendicularTool(rayC, dirA)
                    val sampleE = intersectOnePoint(sampleDE, rayC)
                    val sampleD = intersectOnePoint(sampleDE, rayA)
                    val circleSampleDE = circleTool(sampleD, sampleE)
                    val sampleM = intersectTwoPoints(circleSampleDE, lineBM).second
                    val sampleDM = lineTool(sampleD, sampleM)
                    val solutionDM = parallelTool(sampleDM, baseM, probe = dirA)
                    val solutionD = intersectOnePoint(solutionDM, rayA)
                    val solutionDE = perpendicularTool(rayC, solutionD, probe = baseB)
                    val solutionE = intersectOnePoint(solutionDE, rayC)
                    return Solution(solutionDE, solutionDM, solutionD, solutionE)
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params, setup).elements
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            with(setup) {
//                val solution = constructSolution(params, setup)
//                val point1 = solution.solutionP1
//                val point2 = solution.solutionP2
//                return { context ->
//                    // Assumes that solution is the last element (no extraneous elements)
//                    if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                        0
//                    else {
//                        val onPoint1 = context.elements.count { pointAndElementCoincide(point1, it) }
//                        val onPoint2 = context.elements.count { pointAndElementCoincide(point2, it) }
//                        // Assume solution uses at least one of the highlighted points
//                        max(0, min(2 - onPoint1, 2 - onPoint2)) + 1
//                    }
//                }
//            }
//        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5L L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromPerpendicular
                    1 -> !element.isCircleFromNonCollapsingCompass
                    2 -> !element.isCircleFromNonCollapsingCompass
                    3 -> !element.isLineFromPerpendicular
                    4 -> !element.isLineFromLine
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
                        val lineBM = lineTool(baseB, baseM)
                        val sampleDE = perpendicularTool(rayC, dirA)
                        val sampleE = intersectOnePoint(sampleDE, rayC)
                        val sampleD = intersectOnePoint(sampleDE, rayA)
                        val circleSampleDE = circleTool(sampleD, sampleE)
                        val sampleM = intersectTwoPoints(circleSampleDE, lineBM).second
                        val sampleDM = lineTool(sampleD, sampleM)
                        val solutionDM = parallelTool(sampleDM, baseM, probe = dirA)
                        val solutionD = intersectOnePoint(solutionDM, rayA)
                        val solutionDE = perpendicularTool(rayC, solutionD, probe = baseB)
                        val solutionE = intersectOnePoint(solutionDE, rayC)
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
//        private fun optimal6LSolution(
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
//                        val circle1 = nonCollapsingCompassTool(base2, dir2, vertex)
//                        val right = intersectOnePoint(circle1, line1)
//                        val circle2 = circleTool(right, vertex)
//                        val bisect = perpendicularBisectorTool(vertex, base1)
//                        val center3 = intersectTwoPoints(bisect, circle2).second
//                        val circle3 = circleTool(vertex, center3)
//                        val point = intersectTwoPointsOther(circle3, line1, vertex)
//                        val parallel = parallelTool(side1, point, probe = vertex)
//                        val solutionP2 = intersectOnePoint(parallel, side2)
//                        val solution = parallelTool(line1, solutionP2, probe = base1)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
