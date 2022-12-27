package euclidea

import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.parallelTool
import org.junit.jupiter.api.Test

class SolvePuzzle9_6Test {
    // Trisection by Trapezoid Diagonals

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found ~8 sec
        // using one move to extend line2
        Solver().improveSolution(3, 4)
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
        val side2: Element.Line,
        val diag1: Element.Line,
        val diag2: Element.Line,
        val center: Point
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
                baseA2 = Point(0.7143, 0.0134),
                baseB1 = Point(0.2043, 0.614),
                baseBScale = 0.4111,
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseA1, baseA2, limit1 = true, limit2 = true)
                    val line2 = Element.Line(baseB1, baseB2/*, limit1 = true, limit2 = true*/)
                    val side1 = Element.Line(baseA1, baseB1/*, limit1 = true, limit2 = true*/)
                    val side2 = Element.Line(baseA2, baseB2/*, limit1 = true, limit2 = true*/)
                    val diag1 = Element.Line(baseA1, baseB2/*, limit1 = true, limit2 = true*/)
                    val diag2 = Element.Line(baseA2, baseB1/*, limit1 = true, limit2 = true*/)
                    val center = intersectOnePoint(diag1, diag2)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, side1, side2, diag1, diag2, center) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
                            maxSqDistance = sq(10.0)
                        ),
                        // dir excluded
                        points = listOf(baseA1, baseA2, baseB1, baseB2, center /*, probe*/),
                        elements = listOf(line1, line2, side1, side2, diag1, diag2)
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

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5L L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
//                    0 -> !element.isLineFromLine
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isLineFromLine
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromLine
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
                    // Optimal 3L solution
                    @Suppress("unused") val context = object {
                        val harm = parallelTool(line1, center)
                        val side = intersectOnePoint(harm, side1)
                        val cross = lineTool(side, baseA2)
                        val aim = intersectOnePoint(cross, diag1)
                        val solution = parallelTool(line1, aim)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
