package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.parallelTool
import org.junit.jupiter.api.Test

class SolvePuzzle11_9Test {
    // Point Farthest from Angle Sides

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // found solution ~18 sec
        Solver().improveSolution(4, 4)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseB: Point,
        val center: Point,
        val radius: Double,
        val probe: Point
    )

    data class Setup(
        val base1: Element.Line,
        val base2: Element.Line,
        val circle: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(0.3, 0.0),
                baseB = Point(0.5, 0.4),
                center = Point(1.0, 0.35),
                radius = 0.15,
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(0.7143, 0.0134),
                baseB = Point(0.5012, 0.4443),
                center = Point(1.00111, 0.35434),
                radius = 0.1511,
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base1 = Element.Line(baseO, baseA, limit1 = true)
                    val base2 = Element.Line(baseO, baseB, limit1 = true)
                    val circle = Element.Circle(center, radius)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base1, base2, circle) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(50.0)
                        ),
                        points = listOf(baseO, baseA, baseB, center /*, probe*/),
                        elements = listOf(base1, base2, circle)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                with(setup) {
                    val line1 = angleBisectorTool(baseA, baseO, baseB)
                    val line2 = parallelTool(line1, center)
                    val solution = intersectTwoPoints(line2, circle).second
                    return { context ->
                        context.hasPoint(solution)
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
                    // Optimal 2L solution
                    @Suppress("unused") val context = object {
                        val line1 = angleBisectorTool(baseA, baseO, baseB)
                        val line2 = parallelTool(line1, center)
                        val solution = intersectTwoPoints(line2, circle).second
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
