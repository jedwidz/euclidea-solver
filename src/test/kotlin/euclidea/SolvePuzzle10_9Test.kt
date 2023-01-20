package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test

class SolvePuzzle10_9Test {
    // Three Circles - 1

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 1 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 8,
//            nonNewElementLimit = 4,
//            consecutiveNonNewElementLimit = 3,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val baseC: Point,
        val probe1Scale: Double,
        val probe2Scale: Double
    ) {
        val probe1 = baseC + (baseB - baseC) * probe1Scale
        val probe2 = baseB + (baseC - baseB) * probe2Scale
    }

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                baseC = Point(0.4, 0.7),
                probe1Scale = 0.24,
                probe2Scale = 0.135
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.01, 0.0),
                baseC = Point(0.401, 0.7005),
                probe1Scale = 0.2398,
                probe2Scale = 0.135
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext.of(
                    config = EuclideaConfig(
                        maxSqDistance = sq(10.0),
                        perpendicularBisectorToolEnabled = true
                    ),
                    points = listOf(baseA, baseB, baseC/*, probe1, probe2*/),
                    elements = listOf()
                )
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // Check tangent condition
            solution.forEachPair { circle, circle2 -> intersectOnePoint(circle, circle2) }
            return { context ->
                context.hasElements(solution)
            }
        }

        private fun constructSolution(params: Params): List<Element.Circle> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // From sub-optimal 8L solution
                    val lineAB = lineTool(baseA, baseB)
                    val circle = nonCollapsingCompassTool(baseA, baseC, baseB)
                    val circle2O = intersectTwoPoints(circle, lineAB).second
                    val circle2 = nonCollapsingCompassTool(baseC, baseB, circle2O)
                    val offsetAB = intersectTwoPoints(circle2, lineAB).first
                    val perpAB = perpendicularBisectorTool(baseA, offsetAB)
                    val aimAB = intersectOnePoint(perpAB, lineAB)
                    val solutionB = circleTool(baseB, aimAB)
                    val lineBC = lineTool(baseB, baseC)
                    val aimBC = intersectTwoPoints(lineBC, solutionB).second
                    val solutionC = circleTool(baseC, aimBC)
                    val solutionA = circleTool(baseA, aimAB)

                    return listOf(solutionA, solutionB, solutionC)
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
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
            // Euclidea 7L L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isLineFromLine
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromPerpendicularBisector
                    5 -> !element.isCircleFromCircle
                    6 -> !element.isCircleFromCircle
                    7 -> !element.isCircleFromCircle
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
                        // Sub-optimal 8L solution
                        val lineAB = lineTool(baseA, baseB)
                        val circle = nonCollapsingCompassTool(baseA, baseC, baseB)
                        val circle2O = intersectTwoPoints(circle, lineAB).second
                        val circle2 = nonCollapsingCompassTool(baseC, baseB, circle2O)
                        val offsetAB = intersectTwoPoints(circle2, lineAB).first
                        val perpAB = perpendicularBisectorTool(baseA, offsetAB)
                        val aimAB = intersectOnePoint(perpAB, lineAB)
                        val solutionB = circleTool(baseB, aimAB)
                        val lineBC = lineTool(baseB, baseC)
                        val aimBC = intersectTwoPoints(lineBC, solutionB).second
                        val solutionC = circleTool(baseC, aimBC)
                        val solutionA = circleTool(baseA, aimAB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
