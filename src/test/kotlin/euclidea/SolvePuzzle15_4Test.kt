package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertTrue

class SolvePuzzle15_4Test {
    // Three Equal Segments

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // no solution found 45 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 7,
            maxNonNewElements = 4,
            maxConsecutiveNonNewElements = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseB: Point
    )

    data class Setup(
        val lineA: Element.Line,
        val lineB: Element.Line
    )

    private class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                baseB = Point(0.55, 0.6)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.001, 0.002),
                baseB = Point(0.553, 0.604)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineA = Element.Line(baseO, baseA, limit1 = true, limit2 = true)
                    val lineB = Element.Line(baseO, baseB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineA, lineB) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(baseO, baseA, baseB),
                        elements = listOf(lineA, lineB)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params, setup)
            val solutionElement = solution.solutionLine
            // Validate solution
            with(params) {
                with(setup) {
                    with(solution) {
                        assertTrue(pointAndLineCoincide(solutionPointA, solutionLine))
                        assertTrue(pointAndLineCoincide(solutionPointB, solutionLine))
                        assertTrue(pointAndLineCoincide(solutionPointA, lineA))
                        assertTrue(pointAndLineCoincide(solutionPointB, lineB))
                        assertTrue(coincides(distance(solutionPointA, solutionPointB), distance(solutionPointA, baseA)))
                        assertTrue(coincides(distance(solutionPointA, solutionPointB), distance(solutionPointB, baseB)))
                    }
                }
            }
            return { context ->
                context.hasElement(solutionElement)
            }
        }

        data class Solution(
            val solutionLine: Element.Line,
            val solutionPointA: Point,
            val solutionPointB: Point
        ) {
            val elements = listOf(solutionLine)
        }

        private fun constructSolution(params: Params, setup: Setup): Solution {
            // based on reference solution
            with(params) {
                with(setup) {
                    // Optimal 6L solution
                    val circle1 = nonCollapsingCompassTool(baseO, baseB, baseA)
                    val circle2 = circleTool(baseB, baseA)
                    val foot = intersectOnePoint(circle1, lineA)
                    val parB = parallelTool(lineB, foot, probe = baseB)
                    val pivot = intersectTwoPoints(parB, circle2).first
                    val bisect = perpendicularBisectorTool(pivot, baseA)
                    val solutionPointA = intersectOnePoint(bisect, lineA)
                    val measure = circleTool(solutionPointA, baseA)
                    val solutionPointB = intersectTwoPoints(measure, lineB).second
                    val solutionLine = lineTool(solutionPointA, solutionPointB)

                    return Solution(solutionLine, solutionPointA, solutionPointB)
                }
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Suspected partial solution, following hints and optimal L solution
//                    @Suppress("unused") val context = object {
//                        // Construction for:
//                        // val perpAM = perpendicularTool(rayA, baseM)
//                        val circleBM = circleTool(baseB, baseM)
//                        val circleAM = circleTool(dirA, baseM)
//                        val intersection = intersectTwoPoints(circleAM, circleBM)
//                        val intersection1 = intersection.first
//                        val intersection2 = intersection.second
//                        val perpAM = lineTool(intersection1, intersection2)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solution = constructSolution(params, setup)
            val point1 = solution.solutionPointA
            val point2 = solution.solutionPointB
            return { context ->
                // Assumes that solution is the last element (no extraneous elements)
                if (context.elements.lastOrNull()?.let { coincides(it, solution.solutionLine) } == true)
                    0
                else {
                    val onPoint1 = context.elements.count { pointAndElementCoincide(point1, it) }
                    val onPoint2 = context.elements.count { pointAndElementCoincide(point2, it) }
                    // Assume solution uses at least one of the highlighted points
                    max(0, min(2 - onPoint1, 2 - onPoint2)) + 1
                }
            }
        }

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 7E E-star moves hint
            return listOf(
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool
            )
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
                        // Optimal 6L solution
                        val circle1 = nonCollapsingCompassTool(baseO, baseB, baseA)
                        val circle2 = circleTool(baseB, baseA)
                        val foot = intersectOnePoint(circle1, lineA)
                        val parB = parallelTool(lineB, foot, probe = baseB)
                        val pivot = intersectTwoPoints(parB, circle2).first
                        val bisect = perpendicularBisectorTool(pivot, baseA)
                        val solutionPointA = intersectOnePoint(bisect, lineA)
                        val measure = circleTool(solutionPointA, baseA)
                        val solutionPointB = intersectTwoPoints(measure, lineB).second
                        val solutionLine = lineTool(solutionPointA, solutionPointB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::sample6LSolution)
//        }
//
//        private fun sample6LSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Sub-optimal 6L solution
//                        val lineBM = lineTool(baseB, baseM)
//                        val sampleDE = perpendicularTool(rayC, dirA)
//                        val sampleE = intersectOnePoint(sampleDE, rayC)
//                        val sampleD = intersectOnePoint(sampleDE, rayA)
//                        val circleSampleDE = circleTool(sampleD, sampleE)
//                        val sampleM = intersectTwoPoints(circleSampleDE, lineBM).second
//                        val sampleDM = lineTool(sampleD, sampleM)
//                        val solutionDM = parallelTool(sampleDM, baseM, probe = dirA)
//                        val solutionD = intersectOnePoint(solutionDM, rayA)
//                        val solutionDE = perpendicularTool(rayC, solutionD, probe = baseB)
//                        val solutionE = intersectOnePoint(solutionDE, rayC)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
