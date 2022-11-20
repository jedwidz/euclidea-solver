package euclidea

import euclidea.EuclideaTools.circleTool
import org.junit.jupiter.api.Test

class SolvePuzzle15_3Test {
    // Line-Circle Intersection
    // (circles only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(2, 7)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
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
                base2 = Point(1.0, 0.0),
                center = Point(0.02, -0.1234),
                radius = 0.3123
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(-1.0, 0.0),
                base2 = Point(1.0, 0.0),
                center = Point(0.01983, -0.1258),
                radius = 0.3078
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
            namer.set("center", params.center)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val circle = namer.set("circle", Element.Circle(center, radius))
                val baseContext = EuclideaContext(
                    config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(100.0)),
                    points = listOf(base1, base2, center),
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
                    val solutionLine = Element.Line(base1, base2)
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
                    // Optimal 4E solution
                    val start1 = namer.set("start1", circleTool(base1, center))
                    val start2 = namer.set("start2", circleTool(base2, center))
                    val upper = namer.set("upper", intersectAnyPoint(start1, circle))
                    val tweak = namer.set("tweak", circleTool(base2, upper))
                    val lower = namer.set("lower", intersectTwoPointsOther(start1, tweak, upper))
                    val up = namer.set("up", intersectTwoPointsOther(start1, start2, center))
                    val dupe = namer.set("dupe", circleTool(up, lower))
                    namer.setAll("solution1", "solution2", intersectTwoPoints(dupe, circle))

                    val solutionContext = initialContext.withElements(
                        listOf(
                            start1, start2, tweak, dupe
                        )
                    )
                    return Pair(setup, solutionContext)
                }
            }
        }
    }
}
