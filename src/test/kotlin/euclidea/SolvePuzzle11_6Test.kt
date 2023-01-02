package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle11_6Test {
    // Circle in Angle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Nothing found 3 min 41 sec
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 6,
            nonNewElementLimit = 4,
            consecutiveNonNewElementLimit = 3,
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
        val line2: Element.Line
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
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0),
                            angleBisectorToolEnabled = true,
                            perpendicularToolEnabled = true,
                            perpendicularBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true
                        ),
                        points = listOf(baseO, baseA, baseB, sample/*, probe1, probe2*/),
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
            // Check tangent condition
            listOf(setup.line1, setup.line2).forEach { line -> intersectOnePoint(line, solution) }
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
                    val half = angleBisectorTool(baseA, baseO, baseB)
                    val line = lineTool(baseO, sample)
                    // Or could use a perpendicular bisector here
                    // val perpB = perpendicularBisectorTool(baseO, baseB)
                    val perpB = perpendicularTool(line2, baseB)
                    val center1 = intersectOnePoint(perpB, half)
                    val circle1 = circleTool(center1, baseB)
                    val aim1 = intersectTwoPoints(circle1, line).second
                    val cross1 = lineTool(center1, aim1)
                    val cross2 = parallelTool(cross1, sample)
                    val center2 = intersectOnePoint(cross2, half)
                    val solution = circleTool(center2, sample)
                    return solution
                }
            }
        }

        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Assumed partial solution, agreeing with hints
                    @Suppress("unused") val context = object {
                        val half = angleBisectorTool(baseA, baseO, baseB)
                        val perpB = perpendicularTool(line2, baseB, probe = baseO)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

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

        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
            val referenceSolutionContext = referenceSolution(params, Namer()).second

            val referenceElements = ElementSet()
            referenceElements += referenceSolutionContext.elements
            referenceElements += referenceSolutionContext.constructionElementSet().items()

            val solutionElements = ElementSet()
            solutionElements += constructSolution(params)

            val interestPoints = referenceSolutionContext.constructionPointSet().items()

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                val interestPointsScore = interestPoints.count { pointAndElementCoincide(it, element) }
                solutionScore * 100 + referenceScore * 20 + interestPointsScore
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 6L L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromAngleBisector
                    1 -> !element.isLineFromPerpendicular
                    2 -> !element.isCircleFromNonCollapsingCompass
                    3 -> !element.isCircleFromNonCollapsingCompass
                    4 -> !element.isLineFromPerpendicularBisector
                    5 -> !element.isCircleFromCircle
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
                        val half = angleBisectorTool(baseA, baseO, baseB)
                        val line = lineTool(baseO, sample)

                        // Or could use a perpendicular bisector here
                        // val perpB = perpendicularBisectorTool(baseO, baseB)
                        val perpB = perpendicularTool(line2, baseB, probe = baseO)
                        val center1 = intersectOnePoint(perpB, half)
                        val circle1 = circleTool(center1, baseB)
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
    }
}