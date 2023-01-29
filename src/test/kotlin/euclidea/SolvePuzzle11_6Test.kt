package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertFalse

class SolvePuzzle11_6Test {
    // Circle in Angle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // ?
        Solver().improveSolution(
            maxExtraElements = 1,
            maxDepth = 9,
            maxNonNewElements = 4,
            maxConsecutiveNonNewElements = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseB: Point,
        val sample: Point,
        val probe1Scale: Double,
        val probe2Scale: Double
    ) {
        val probe1 = baseO + (baseA - baseO) * probe1Scale
        val probe2 = baseO + (baseB - baseO) * probe2Scale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val half: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                baseB = Point(0.4, 0.8),
                sample = Point(0.35, 0.5),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.01, 0.0),
                baseB = Point(0.401, 0.8005),
                sample = Point(0.350101, 0.503),
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
                    val half = angleBisectorTool(baseA, baseO, baseB)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, half) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(12.0),
//                            angleBisectorToolEnabled = true
                        ),
                        points = listOf(baseO, baseA, baseB, sample/*, probe1, probe2*/),
                        elements = listOf(line1, line2, half)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutions = constructSolutions(params)
            // Check tangent condition
            assertFalse(solutions.pairs().any { (a, b) -> coincides(a, b) })
            for (solution in solutions) {
                listOf(setup.line1, setup.line2).forEach { line -> intersectOnePoint(line, solution) }
            }
            return { context ->
                solutions.any { solution -> context.hasElement(solution) }
            }
        }

        private fun constructSolutions(params: Params): List<Element.Circle> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 7L solution
                    val half = angleBisectorTool(baseA, baseO, baseB)
                    val perp = perpendicularTool(line2, sample, probe = baseO)
                    val touch = intersectOnePoint(perp, line2)
                    val center1 = intersectOnePoint(perp, half)
                    val circle1 = circleTool(center1, touch)
                    val line = lineTool(baseO, sample)

                    val aim1 = intersectTwoPoints(circle1, line).second
                    val cross11 = lineTool(center1, aim1)
                    val cross12 = parallelTool(cross11, sample, probe = center1)
                    val center12 = intersectOnePoint(cross12, half)
                    val solution1 = circleTool(center12, sample)

                    // Second solution
                    val aim2 = intersectTwoPoints(circle1, line).first
                    val cross21 = lineTool(center1, aim2)
                    val cross22 = parallelTool(cross21, sample, probe = center1)
                    val center22 = intersectOnePoint(cross22, half)
                    val solution2 = circleTool(center22, sample)

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
//                        // Moved to setup
//                        // val half = angleBisectorTool(baseA, baseO, baseB)
//
//                        // val perp = perpendicularTool(line2, sample, probe = baseO)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutions = constructSolutions(params)
            val centers = solutions.map { it.center }
            return { context ->
                // Assumes that solution is the last element (no extraneous elements)
                if (context.elements.lastOrNull()
                        ?.let { solutions.any { solution -> coincides(it, solution) } } == true
                )
                    0
                else {
                    val onCenter =
                        centers.maxOf { center -> context.elements.count { pointAndElementCoincide(center, it) } }
                    // Need two elements to locate center, then the solution circle itself
                    max(0, 2 - onCenter) + 1
                }
            }
        }

//        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
//            val referenceSolutionContext = referenceSolution(params, Namer()).second
//
//            val referenceElements = ElementSet()
//            referenceElements += referenceSolutionContext.elements
//            referenceElements += referenceSolutionContext.constructionElementSet().items()
//
//            val solutionElements = ElementSet()
//            solutionElements += constructSolution(params)
//
//            val interestPoints = referenceSolutionContext.constructionPointSet().items()
//
//            return { _, element ->
//                val solutionScore = if (element in solutionElements) 1 else 0
//                val referenceScore = if (element in referenceElements) 1 else 0
//                val interestPointsScore = interestPoints.count { pointAndElementCoincide(it, element) }
//                solutionScore * 100 + referenceScore * 20 + interestPointsScore
//            }
//        }

//        override fun toolSequence(): List<EuclideaTool> {
//            // Euclidea 11E E-star moves hint
//            return listOf(
//                // Moved to setup
//                // EuclideaTool.AngleBisectorTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.CircleTool,
//                EuclideaTool.LineTool,
//                EuclideaTool.CircleTool
//            )
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
                        // Sub-optimal 7L solution
                        // Moved to setup
                        // val half = angleBisectorTool(baseA, baseO, baseB)
                        val perp = perpendicularTool(line2, sample, probe = baseO)
                        val touch = intersectOnePoint(perp, line2)
                        val center1 = intersectOnePoint(perp, half)
                        val circle1 = circleTool(center1, touch)
                        val line = lineTool(baseO, sample)
                        val aim1 = intersectTwoPoints(circle1, line).second
                        val cross1 = lineTool(center1, aim1)
                        val cross2 = parallelTool(cross1, sample, probe = center1)
                        val center2 = intersectOnePoint(cross2, half)
                        val solution = circleTool(center2, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
            return listOf(this::optimal6LSolution, this::optimal6LSolution2)
        }

        fun optimal6LSolution(
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
                        // Moved to setup
                        // val half = angleBisectorTool(baseA, baseO, baseB)
                        val perp = perpendicularTool(half, sample, probe = baseO)
                        val touch1 = intersectOnePoint(perp, line1)
                        val mid = intersectOnePoint(perp, half)
                        val circle1 = nonCollapsingCompassTool(touch1, mid, sample)
                        val aim1 = intersectTwoPoints(circle1, half).first
                        val touch2 = intersectOnePoint(perp, line2)
                        val circle2 = nonCollapsingCompassTool(aim1, mid, touch2)
                        val ref = intersectTwoPoints(circle2, line2).first
                        val cross = perpendicularBisectorTool(ref, sample)
                        val center2 = intersectOnePoint(cross, half)
                        val solution = circleTool(center2, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

        fun optimal6LSolution2(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    @Suppress("unused") val context = object {
                        // Optimal 6L solution (other)
                        // Moved to setup
                        // val half = angleBisectorTool(baseA, baseO, baseB)
                        val perp = perpendicularTool(half, sample, probe = baseO)
                        val touch1 = intersectOnePoint(perp, line1)
                        val mid = intersectOnePoint(perp, half)
                        val circle1 = nonCollapsingCompassTool(touch1, mid, sample)
                        val aim1 = intersectTwoPoints(circle1, half).first
                        val touch2 = intersectOnePoint(perp, line2)
                        val circle2 = nonCollapsingCompassTool(aim1, mid, touch2)
                        val ref = intersectTwoPoints(circle2, line2).second // spot the difference
                        val cross = perpendicularBisectorTool(ref, sample)
                        val center2 = intersectOnePoint(cross, half)
                        val solution = circleTool(center2, sample)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
