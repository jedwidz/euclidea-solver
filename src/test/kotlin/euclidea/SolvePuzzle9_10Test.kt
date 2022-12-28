package euclidea

import euclidea.EuclideaTools.parallelTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import org.junit.jupiter.api.Test

class SolvePuzzle9_10Test {
    // Triangle Mid-Segment

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(4, 5)
    }

    data class Params(
        val baseA: Point,
        val baseB: Point,
        val baseC: Point,
        val probeScale: Double
    ) {
        val probe = baseB + (baseA - baseB) * probeScale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val line3: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(0.7, 0.0),
                baseC = Point(0.3, 1.0),
                probeScale = 0.13
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseA = Point(0.0, 0.0),
                baseB = Point(0.7143, 0.0134),
                baseC = Point(0.3043, 1.0123),
                probeScale = 0.13013
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseA, baseB, limit1 = true, limit2 = true)
                    val line2 = Element.Line(baseA, baseC, limit1 = true, limit2 = true)
                    val line3 = Element.Line(baseB, baseC, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2, line3) to EuclideaContext(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(baseA, baseB, baseC, probe),
                        elements = listOf(line1, line2, line3)
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
            }
        }

        private fun constructSolution(params: Params): Element.Line {
            // cheekily use reference solution
            return referenceSolution(params, Namer()).second.elements.last() as Element.Line
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isLineFromLine
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
                    // Optimal 2L solution
                    @Suppress("unused") val context = object {
                        val midL = perpendicularBisectorTool(baseB, baseC)
                        val midP = intersectOnePoint(midL, line3)
                        val solution = parallelTool(line1, midP)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
