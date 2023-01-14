package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle14_3Test {
    // Triangle by Tangent Point on Hypotenuse

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 31 sec
        Solver().improveSolution(
            maxExtraElements = 2,
            maxDepth = 9,
//            nonNewElementLimit = 7,
//            consecutiveNonNewElementLimit = 4,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val tangentPointScale: Double,
        val probe: Point
    ) {
        val tangentPoint = baseA + (baseB - baseA) * tangentPointScale
    }

    data class Setup(
        val lineAB: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                tangentPointScale = 0.24,
                probe = Point(0.4, 0.15)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(1.0, 0.0),
                tangentPointScale = 0.2401,
                probe = Point(0.402, 0.1533)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineAB = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineAB) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(5.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        points = listOf(baseA, baseB, tangentPoint, probe),
                        elements = listOf(lineAB)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params).elements
            return { context ->
                context.hasElements(solution)
            }
        }

        data class Solution(
            val solutionA: Element.Line,
            val solutionB: Element.Line,
            val vertex: Point
        ) {
            val elements = listOf(solutionA, solutionB)
        }

        private fun constructSolution(params: Params): Solution {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 7L solution
                    val bisectAB = perpendicularBisectorTool(baseA, baseB)
                    val midAB = intersectOnePoint(bisectAB, lineAB)
                    val circle1 = circleTool(midAB, baseA)
                    val down = intersectTwoPoints(circle1, bisectAB).first
                    val circle2 = circleTool(down, baseA)
                    val perpTangent = perpendicularTool(lineAB, tangentPoint, probe = down)
                    val aim = intersectTwoPoints(perpTangent, circle2).second
                    val cross = lineTool(down, aim)
                    val vertex = intersectTwoPoints(cross, circle1).second
                    val solutionA = lineTool(vertex, baseA)
                    val solutionB = lineTool(vertex, baseB)

                    return Solution(solutionA, solutionB, vertex)
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
            val solutionElements = constructSolution(params).elements
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
                        // Sub-optimal 7L solution
                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
                        val midAB = intersectOnePoint(bisectAB, lineAB)
                        val circle1 = circleTool(midAB, baseA)
                        val down = intersectTwoPoints(circle1, bisectAB).first
                        val circle2 = circleTool(down, baseA)
                        val perpTangent = perpendicularTool(lineAB, tangentPoint, probe = down)
                        val aim = intersectTwoPoints(perpTangent, circle2).second
                        val cross = lineTool(down, aim)
                        val vertex = intersectTwoPoints(cross, circle1).second
                        val solutionA = lineTool(vertex, baseA)
                        val solutionB = lineTool(vertex, baseB)
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
//                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
//                        val midAB = intersectOnePoint(bisectAB, lineAB)
//                        val cross = lineTool(midAB, baseC)
//                        val circle1 = circleTool(baseC, midAB)
//                        val doubled = intersectTwoPointsOther(circle1, cross, midAB)
//                        val circle2 = circleTool(doubled, baseC)
//                        val tripled = intersectTwoPointsOther(circle2, cross, baseC)
//                        val solutionA = lineTool(baseA, tripled)
//                        val solutionB = lineTool(baseB, tripled)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
