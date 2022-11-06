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
        Solver().improveSolution(0, 7 - 2 - 1)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val center: Point,
        val dir: Point,
        val probeCenter: Point,
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
                probeCenter = Point(0.001234, 0.3),
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
                probeCenter = Point(0.001234, 0.3),
                probePoint1 = Point(-0.2, 1.0111),
                probePoint2 = Point(0.2, 1.0222)
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
            namer.set("center", params.center)
            // dir not really a point
            namer.set("probeCenter", params.probeCenter)
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
                val probeLine1 = namer.set("probe1", Element.Line(probeCenter, probePoint1))
                val probeLine2 = namer.set("probe2", Element.Line(probeCenter, probePoint2))
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
                    // Optimal 7E solution
                    val startP21 = namer.set("startP21", intersectOnePoint(line2, probeLine1))
                    val startP22 = namer.set("startP22", intersectOnePoint(line2, probeLine2))
                    val mid = namer.set("mid", Element.Line(center, probeCenter))
                    val pivot = namer.set("pivot", intersectOnePoint(mid, line1))
                    val x1 = namer.set("x1", Element.Line(pivot, startP22))
                    val x2 = namer.set("x2", Element.Line(center, startP21))
                    val crux = namer.set("crux", intersectOnePoint(x1, x2))
                    val pivotOpp = namer.set("pivotOpp", intersectOnePoint(line1, probeLine1))
                    val midOpp = namer.set("midOpp", Element.Line(pivotOpp, crux))
                    val prop = namer.set("prop", intersectOnePoint(midOpp, probeLine2))
                    val solution = namer.set("solution", Element.Line(center, prop))

                    val solutionContext = initialContext.withElements(
                        listOf(mid, x1, x2, mid, midOpp, solution)
                    )
                    return Pair(setup, solutionContext)
                }
            }
        }
    }
}
