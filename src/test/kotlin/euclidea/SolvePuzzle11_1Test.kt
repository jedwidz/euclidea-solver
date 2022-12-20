package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle11_1Test {
    // Fourth Proportional

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(3, 3)
    }

    data class Params(
        val baseO: Point,
        val baseA: Point,
        val baseBScale: Double,
        val baseC: Point,
        val probe: Point
    ) {
        val baseB = baseO + (baseA - baseO) * baseBScale
    }

    data class Setup(
        val line1: Element.Line,
        val line2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(0.7, 0.0),
                baseBScale = 1.3,
                baseC = Point(0.5, 0.4),
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(0.7143, 0.0134),
                baseBScale = 1.31111,
                baseC = Point(0.5012, 0.4443),
                probe = Point(0.6001, 0.3022)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val line1 = Element.Line(baseO, baseA)

                    // TODO should be a ray
                    val line2 = Element.Line(baseO, baseC)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2) to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(20.0)),
                        points = listOf(baseO, baseA, baseB, baseC, probe),
                        elements = listOf(line1, line2)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(params) {
                val oa = (baseA - baseO).distance
                val ob = (baseB - baseO).distance
                val oc = (baseC - baseO).distance
                val od = oc * ob / oa
                val checkSolutionPointD = baseO + (baseC - baseO) * (od / oc)
                return { context ->
                    context.hasPoint(checkSolutionPointD)
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
                    // Optimal 3E solution
                    @Suppress("unused") val context = object {
                        val circle1 = circleTool(baseA, baseB)
                        val left = intersectTwoPointsOther(circle1, line1, baseB)
                        val circle2 = circleTool(baseC, left)
                        val top = intersectTwoPointsOther(circle1, circle2, left)
                        val aim = lineTool(top, baseB)
                        val solution = intersectOnePoint(aim, line2)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
