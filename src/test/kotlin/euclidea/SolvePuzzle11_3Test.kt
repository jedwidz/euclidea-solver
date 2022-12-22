package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
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
        Solver().improveSolution(5, 5)
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
                    val base = Element.Line(baseO, baseA, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(
                            maxSqDistance = sq(20.0)
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
                    // Optimal 5E solution
                    @Suppress("unused") val context = object {
                        val circle = circleTool(baseO, baseA)
                        val circle2 = circleTool(baseA, baseO)
                        val bisectP = intersectTwoPoints(circle, circle2)
                        val bisectP1 = bisectP.first
                        val bisectP2 = bisectP.second
                        val bisectL = lineTool(bisectP1, bisectP2)
                        val circle3 = circleTool(bisectP1, baseO)
                        val left = intersectTwoPointsOther(circle3, circle, baseA)
                        val down = intersectTwoPoints(circle3, bisectL).first
                        val solutionC = circleTool(left, down)
                        val solution = intersectOnePoint(solutionC, base)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
