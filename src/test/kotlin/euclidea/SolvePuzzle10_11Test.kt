package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle10_11Test {
    // Three Circles - 2

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Solution found 11 hr 55 min
        Solver().improveSolution(
            maxExtraElements = 4,
            maxDepth = 13,
            maxNonNewElements = 6,
            maxConsecutiveNonNewElements = 3,
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
                        maxSqDistance = sq(10.0)
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
                    // From optimal 9L solution
                    val perpAC = perpendicularBisectorTool(baseA, baseC)
                    val perpAB = perpendicularBisectorTool(baseA, baseB)
                    val center = intersectOnePoint(perpAB, perpAC)
                    val centerA = lineTool(center, baseA)
                    val lineA = perpendicularTool(centerA, baseA)
                    val dualC = intersectOnePoint(lineA, perpAB)
                    val solutionC = circleTool(dualC, baseA)
                    val dualB = intersectOnePoint(lineA, perpAC)
                    val solutionB = circleTool(dualB, baseC)
                    val lineB = lineTool(dualB, baseC)
                    val lineC = lineTool(dualC, baseB)
                    val dualA = intersectOnePoint(lineB, lineC)
                    val solutionA = circleTool(dualA, baseB)

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
            val centers = solutionElements.map { it.center }
            return { context ->
                val forSolutionElements = solutionElements.count { !context.hasElement(it) }
                // Conservatively assume one step could cover all three centers
                val forCircleCenters = centers.maxOf { center ->
                    val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
                    // Need two elements to locate center
                    max(0, 2 - onCenter)
                }
                forSolutionElements + forCircleCenters
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 13E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isLineFromLine
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
                    5 -> !element.isLineFromLine
                    6 -> !element.isLineFromLine
                    7 -> !element.isCircleFromCircle
                    8 -> !element.isCircleFromCircle
                    9 -> !element.isLineFromLine
                    10 -> !element.isCircleFromCircle
                    11 -> !element.isLineFromLine
                    12 -> !element.isCircleFromCircle
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
                        // Optimal 9L solution
                        val perpAC = perpendicularBisectorTool(baseA, baseC)
                        val perpAB = perpendicularBisectorTool(baseA, baseB)
                        val center = intersectOnePoint(perpAB, perpAC)
                        val centerA = lineTool(center, baseA)
                        val lineA = perpendicularTool(centerA, baseA, probe = baseB)
                        val dualC = intersectOnePoint(lineA, perpAB)
                        val solutionC = circleTool(dualC, baseA)
                        val dualB = intersectOnePoint(lineA, perpAC)
                        val solutionB = circleTool(dualB, baseC)
                        val lineB = lineTool(dualB, baseC)
                        val lineC = lineTool(dualC, baseB)
                        val dualA = intersectOnePoint(lineB, lineC)
                        val solutionA = circleTool(dualA, baseB)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
