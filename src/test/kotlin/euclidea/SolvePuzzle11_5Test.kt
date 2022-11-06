package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle11_5Test {
    // Third Parallel Line
    // (lines only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Starting point includes initial probe lines
        // Solution is detected as any novel point on the solution line (one step less than the solution line itself)
        Solver().improveSolution(1, 7 - 2 - 1)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val center: Point,
        val dir: Point,
        val probePoint1: Point,
        val probePoint2: Point
    )

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line,
        val probeLine1: Element.Line,
        val probeLine2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.8),
                base2 = Point(0.0, 2.0),
                center = Point(0.0, 0.0),
                dir = Point(1.0, 0.0),
                probePoint1 = Point(-0.2, 1.0),
                probePoint2 = Point(0.2, 1.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.0123, 0.8833),
                base2 = Point(0.0012, 2.04343),
                center = Point(0.0, 0.0),
                dir = Point(1.0, 0.0),
                probePoint1 = Point(-0.2, 1.0111),
                probePoint2 = Point(0.2, 1.0222)
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
            namer.set("center", params.center)
            // dir not really a point
            namer.set("probe1", params.probePoint1)
            namer.set("probe2", params.probePoint2)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val line1 = namer.set("line1", Element.Line(base1, base1.plus(dir)))
                val line2 = namer.set("line2", Element.Line(base2, base2.plus(dir)))
                val baseContext = EuclideaContext(
                    config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(25.0)),
                    points = listOf(center),
                    elements = listOf(line1, line2)
                )
                // 'probe' lines to cut across the given lines
                val probeLine1 = namer.set("probe1", Element.Line(center, probePoint1))
                val probeLine2 = namer.set("probe2", Element.Line(center, probePoint2))
                val probeLineContext = baseContext.withElements(listOf(probeLine1, probeLine2))
                return Pair(Setup(line1, line2, probeLine1, probeLine2), probeLineContext)
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                val checkSolutionLine = Element.Line(center, center.plus(dir))
                return { context ->
                    // Any point on solution line...
                    // context.hasElement(checkSolutionLine)
                    context.points.any { point ->
                        pointAndLineCoincide(point, checkSolutionLine) && !coincides(point, center)
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
                    // Sub-optimal 8E solution
                    val startP11 = namer.set("startP11", intersectOnePoint(line1, probeLine1))
                    val startP12 = namer.set("startP12", intersectOnePoint(line1, probeLine2))
                    val startP21 = namer.set("startP21", intersectOnePoint(line2, probeLine1))
                    val startP22 = namer.set("startP22", intersectOnePoint(line2, probeLine2))
                    val x1 = namer.set("x1", Element.Line(startP11, startP22))
                    val x2 = namer.set("x2", Element.Line(startP12, startP21))
                    val pivot = namer.set("pivot", intersectOnePoint(x1, x2))
                    val mid = namer.set("mid", Element.Line(center, pivot))
                    val half = namer.set("half", intersectOnePoint(line1, mid))
                    val skew = namer.set("skew", Element.Line(startP21, half))
                    val side = namer.set("side", intersectOnePoint(skew, probeLine2))
                    val pincer = namer.set("pincer", Element.Line(startP11, side))
                    val prop = namer.set("prop", intersectOnePoint(pincer, x2))
                    val solution = namer.set("solution", Element.Line(center, prop))

                    val solutionContext = initialContext.withElements(
                        listOf(x1, x2, mid, skew, pincer, solution)
                    )
                    return Pair(setup, solutionContext)
                }
            }
        }
    }
}
