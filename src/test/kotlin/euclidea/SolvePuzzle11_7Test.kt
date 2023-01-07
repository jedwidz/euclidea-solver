package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle11_7Test {
    // Geometric Mean of Trapezoid Bases

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            maxExtraElements = 3,
            maxDepth = 6,
//            nonNewElementLimit = 4,
//            consecutiveNonNewElementLimit = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val baseA1: Point,
        val baseA2: Point,
        val baseB1: Point,
        val baseBScale: Double,
        val probe: Point
    ) {
        val baseB2 = baseB1 + (baseA2 - baseA1) * baseBScale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val side1: Element.Line,
        val side2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA1 = Point(0.0, 0.0),
                baseA2 = Point(0.7, 0.0),
                baseB1 = Point(0.2, 0.6),
                baseBScale = 0.4,
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA1 = Point(0.0, 0.0),
                baseA2 = Point(0.7043, 0.0034),
                baseB1 = Point(0.2043, 0.604),
                baseBScale = 0.4011,
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val lineA = Element.Line(baseA1, baseA2, limit1 = true, limit2 = true)
                    val lineB = Element.Line(baseB1, baseB2, limit1 = true, limit2 = true)
                    val side1 = Element.Line(baseA1, baseB1, limit1 = true, limit2 = true)
                    val side2 = Element.Line(baseA2, baseB2, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(lineA, lineB, side1, side2) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
                            maxSqDistance = sq(10.0)
                        ),
                        // dir excluded
                        points = listOf(baseA1, baseA2, baseB1, baseB2 /*, probe*/),
                        elements = listOf(lineA, lineB, side1, side2)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            // cheekily use reference solution
            val solution = constructSolution(params)
            return { context ->
                coincides(context.elements.last(), solution)
//                val last = context.elements.last()
//                last is Element.Line && linesParallel(last, solution)
            }
//            with(params) {
//                val oa = (baseA2 - baseA1).distance
//                val ob = (baseB2 - baseB1).distance
//                val oc = (dir - baseA1).distance
//                val od = 2.0 * oa * ob / (oa + ob)
//                val checkSolutionPoint = baseA1 + (dir - baseA1) * (od / oc)
//                return { context ->
//                    context.hasPoint(checkSolutionPoint)
//                }
//        }
        }

        private fun constructSolution(params: Params): Element.Line {
            // cheekily use reference solution
            return referenceSolution(params, Namer()).second.elements.last() as Element.Line
        }

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 3L L-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> element !is Element.Line
//                    1 -> element !is Element.Line
//                    2 -> element !is Element.Circle
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
                    // Sub-optimal 8L/9L solution
                    @Suppress("unused") val context = object {
                        val extendA = lineTool(baseA1, baseA2)
                        val circle1 = nonCollapsingCompassTool(baseB1, baseB2, baseA1)
                        val left = intersectTwoPoints(circle1, extendA).first
                        val bisect = perpendicularBisectorTool(left, baseA2)

                        // Can make this an 8L solution by tweaking parameters, and using side1 directly
                        val extend1 = lineTool(baseA1, baseB1)
                        val solutionP1 = intersectOnePoint(bisect, extend1 /*side1*/)
                        val perp = perpendicularTool(side1, baseA1, probe = baseA2)
                        val center2 = intersectOnePoint(perp, bisect)
                        val circle2 = circleTool(center2, baseA2)
                        val sample3 = intersectOnePoint(circle2, side1)
                        val circle3 = circleTool(baseA1, sample3)
                        val point = intersectOnePoint(circle3, line1)
                        val parallel = parallelTool(side1, point, probe = baseA1)
                        val solutionP2 = intersectOnePoint(parallel, side2)
                        val solution = lineTool(solutionP1, solutionP2)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
