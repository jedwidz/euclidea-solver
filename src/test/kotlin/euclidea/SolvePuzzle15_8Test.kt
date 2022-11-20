package euclidea

import euclidea.EuclideaTools.circleTool
import org.junit.jupiter.api.Test

class SolvePuzzle15_8Test {
    // Line-Circle Intersection*
    // (circles only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(1, 7)
    }

    data class Params(
        val base1: Point,
        val center: Point,
        val radius: Double
    )

    data class Setup(
        val circle: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(-1.0, 0.0),
                center = Point(0.02, 0.0),
                radius = 0.3123
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(-1.0, 0.0),
                center = Point(0.01983, 0.0),
                radius = 0.3078
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("center", params.center)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val circle = namer.set("circle", Element.Circle(center, radius))
                val baseContext = EuclideaContext(
                    config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(4.0)),
                    points = listOf(base1, center),
                    elements = listOf(circle)
                )
                return Pair(Setup(circle), baseContext)
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                with(setup) {
                    val solutionLine = Element.Line(base1, circle.center)
                    val (solutionPoint1, solutionPoint2) = intersectTwoPoints(solutionLine, circle)
                    return { context ->
                        context.hasPoint(solutionPoint1) && context.hasPoint(solutionPoint2)
                    }
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
                    // Optimal 7E solution
                    val start = namer.set("start", circleTool(base1, center))
                    val (adj1, adj2) = namer.setAll("adj1", "adj2", intersectTwoPoints(start, circle))
                    val shift = namer.set("shift", circleTool(adj1, center))
                    val span = namer.set("span", circleTool(adj1, adj2))
                    val opp = namer.set("opp", intersectTwoPointsOther(shift, start, center))
                    val eye = namer.set("eye", circleTool(center, opp))
                    val perpP = namer.set("perpP", intersectTwoPointsOther(eye, shift, opp))
                    val perpC = namer.set("perpC", circleTool(perpP, adj1))
                    val focus = namer.set("focus", intersectAnyPoint(shift, perpC))
                    val bigP = namer.set("bigP", intersectTwoPointsOther(eye, start, opp))
                    val bigC = namer.set("bigC", circleTool(bigP, focus))
                    val finalP = namer.set("finalP", intersectAnyPoint(bigC, span))
                    val finalC = namer.set("finalC", circleTool(perpP, finalP))

                    namer.setAll("solution1", "solution2", intersectTwoPoints(finalC, circle))

                    val solutionContext = initialContext.withElements(
                        listOf(
                            start, shift, span, eye, perpC, bigC, finalC
                        )
                    )
                    return Pair(setup, solutionContext)
                }
            }
        }
    }
}
