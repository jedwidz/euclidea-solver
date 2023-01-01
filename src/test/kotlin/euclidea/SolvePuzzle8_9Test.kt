package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle8_9Test {
    // Egyptian Triangle by Side of Length 4

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found in ~1 sec
        Solver().improveSolution(5, 7)
    }

    data class Params(
        val pointA: Point,
        val pointB: Point,
        val probe1: Point,
        val probeScale: Double
    ) {
        val probe2 = pointA + (pointB - pointA) * probeScale
    }

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.0, 0.0),
                probe1 = Point(0.6, 0.2),
                probeScale = -0.5623
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.0011, 0.0133),
                probe1 = Point(0.6101, 0.2022),
                probeScale = -0.564111
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = Element.Line(pointA, pointB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(10.0)
                        ),
                        points = listOf(pointA, pointB/*, probe1, probe2*/),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            return { context ->
                coincides(context.elements.last(), solution)
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
                    5 -> !element.isLineFromLine
                    6 -> !element.isLineFromLine
                    else -> false
                }
            }
        }

        private fun constructSolution(params: Params): Element.Line {
            // cheekily use reference solution
            return referenceSolution(params, Namer()).second.elements.last() as Element.Line
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
                    // Optimal 5L solution
                    @Suppress("unused") val context = object {
                        val perpAt2 = perpendicularBisectorTool(pointA, pointB)
                        val at2 = intersectOnePoint(perpAt2, base)
                        val perpAt1 = perpendicularBisectorTool(pointA, at2)
                        val at1 = intersectOnePoint(perpAt1, base)
                        val perpAt4 = perpendicularTool(base, pointB)
                        val circle = circleTool(pointB, at1)
                        val target = intersectTwoPoints(circle, perpAt4).second
                        val solution = lineTool(pointA, target)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
