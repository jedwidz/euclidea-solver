package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle10_2Test {
    // Outer Tangent

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(6, 6)
    }

    data class Params(
        val centerA: Point,
        val radiusA: Double,
        val centerB: Point,
        val radiusB: Double,
        val probe1: Point,
        val probeScale: Double
    ) {
        val probe2 = centerA + (centerB - centerA) * probeScale
    }

    data class Setup(
        val circleA: Element.Circle,
        val circleB: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                radiusA = 0.2,
                centerB = Point(1.0, 0.0),
                radiusB = 0.351,
                probe1 = Point(0.6, 0.2),
                probeScale = -0.5623
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                centerA = Point(0.0, 0.0),
                radiusA = 0.201234,
                centerB = Point(1.0011, 0.0133),
                radiusB = 0.3534,
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
                    val circleA = Element.Circle(centerA, radiusA)
                    val circleB = Element.Circle(centerB, radiusB)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circleA, circleB) to EuclideaContext(
                        config = EuclideaConfig(
                            // limited by 6L hint
                            perpendicularBisectorToolEnabled = true,
                            maxSqDistance = sq(50.0)
                        ),
                        points = listOf(centerA, centerB, probe1, probe2),
                        elements = listOf(circleA, circleB)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(setup) {
                return { context ->
                    context.elements.any {
                        it is Element.Line && meetAtOnePoint(it, circleA) && meetAtOnePoint(
                            it,
                            circleB
                        )
                    }
                }
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea L-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isLineFromPerpendicularBisector
                    2 -> !element.isLineFromPerpendicularBisector
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isCircleFromCircle
                    5 -> !element.isLineFromLine
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
                    // Suboptimal 7E solution
                    @Suppress("unused") val context = object {
                        val base = lineTool(centerA, centerB)
                        val perpA = perpendicularTool(base, centerA)
                        val perpB = perpendicularTool(base, centerB)
                        val refA = intersectTwoPoints(perpA, circleA).second
                        val refB = intersectTwoPoints(perpB, circleB).second
                        val ref = lineTool(refA, refB)
                        val focus = intersectOnePoint(ref, base)
                        val roof = perpendicularTool(perpB, refB)
                        val circle = circleTool(focus, centerB)
                        val target = intersectTwoPoints(circle, roof).second
                        val solution = lineTool(focus, target)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
