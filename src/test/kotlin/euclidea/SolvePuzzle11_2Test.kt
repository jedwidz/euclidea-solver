package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class SolvePuzzle11_2Test {
    // Geometric Mean of Segments

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
        val baseBScale: Double,
        val dir: Point,
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
                baseA = Point(-0.3, 0.0),
                baseBScale = -2.3,
                dir = Point(0.5, 0.4),
                probe = Point(0.6, 0.3)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                baseA = Point(-0.7143, 0.0134),
                baseBScale = -1.31111,
                dir = Point(0.5012, 0.4443),
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
                    val line2 = Element.Line(baseO, dir)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(line1, line2) to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(50.0)),
                        // dir excluded
                        points = listOf(baseO, baseA, baseB /*, probe*/),
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
                val oc = (dir - baseO).distance
                val od = sqrt(oa * ob)
                val checkSolutionPoint = baseO + (dir - baseO) * (od / oc)
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
                        val circle1 = circleTool(baseA, baseO)
                        val circle2 = circleTool(baseO, baseB)
                        val left = intersectTwoPointsOther(circle2, line1, baseB)
                        val circle3 = circleTool(left, baseO)
                        val bisect = intersectTwoPoints(circle3, circle2)
                        val bisect1 = bisect.first
                        val bisect2 = bisect.second
                        val bisectLine = lineTool(bisect1, bisect2)
                        val top = intersectTwoPoints(circle1, bisectLine).first
                        val circle4 = circleTool(baseO, top)
                        val solution = intersectTwoPoints(circle4, line2).second
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
