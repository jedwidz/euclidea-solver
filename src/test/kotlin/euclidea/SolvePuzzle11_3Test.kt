package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class SolvePuzzle11_3Test {
    // Golden Section

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(3, 5)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val probe: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0, 0.0),
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(1.0011, 0.0133),
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    // TODO should be a line segment
                    val base = Element.Line(baseO, baseA)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(50.0)
                        ),
                        points = listOf(baseO, baseA /*, probe*/),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val phi = (1.0 + sqrt(5.0)) / 2.0
            with(params) {
                val checkSolutionPoint = baseO + (baseA - baseO) * (1.0 / phi)
                return { context ->
                    context.hasPoint(checkSolutionPoint)
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
                    // Glitchy 4L solution
                    @Suppress("unused") val context = object {
                        val circle = circleTool(baseO, baseA)

                        // Shouldn't be allowed, point is outside line segment
                        val left = intersectTwoPoints(circle, base).first
                        val perpLeft = perpendicularBisectorTool(left, baseO)
                        val center = intersectOnePoint(perpLeft, base)
                        val perpO = perpendicularTool(base, baseO)
                        val top = intersectTwoPoints(perpO, circle).second
                        val circle2 = circleTool(center, top)
                        val solution = intersectTwoPoints(circle2, base).second
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
