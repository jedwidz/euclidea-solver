package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle10_4Test {
    // Rotation 90deg

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(8, 8)
    }

    data class Params(
        val center: Point,
        val point1A: Point,
        val point1B: Point
    )

    data class Setup(
        // actually the solution...
        val point2A: Point,
        val point2B: Point,
        val line1: Element.Line,
        val line2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                point1A = Point(-0.2, -0.18),
                point1B = Point(0.15, 0.22)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                point1A = Point(-0.214, -0.1834),
                point1B = Point(0.1555, 0.2211)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                fun rotate(point: Point) = rotatePoint(center, point, 90.0)
                val context = object {
                    val line1 = Element.Line(point1A, point1B, limit1 = true, limit2 = true)
                    val point2A = rotate(point1A)
                    val point2B = rotate(point1B)
                    val line2 = Element.Line(point2A, point2B, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(point2A, point2B, line1, line2) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0)
                        ),
                        points = listOf(center, point1A, point1B),
                        elements = listOf(line1)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            return { context ->
                with(setup) {
                    context.hasElement(line2)
                }
            }
        }

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea L-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
////                    0 -> !(element.isLineFromLine && coincides(element, lineTool(params.centerA, params.centerB)))
//                    0 -> !element.isLineFromLine
//                    1 -> !element.isLineFromPerpendicularBisector
//                    2 -> !element.isLineFromPerpendicularBisector
//                    3 -> !element.isCircleFromCircle
//                    4 -> !element.isCircleFromCircle
//                    5 -> !element.isLineFromLine
//                    else -> false
//                }
//            }

//        override fun referenceSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Optimal 2L solution
//                    @Suppress("unused") val context = object {
//                        val aim1 = perpendicularBisectorTool(point1A, point2A)
//                        val aim2 = perpendicularBisectorTool(point1B, point2B)
//                        val solution = intersectOnePoint(aim1, aim2)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
