package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularBisectorTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle10_8Test {
    // Chord Trisection

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(1, 3)
    }

    data class Params(
        val center: Point,
        val radiusA: Double,
        // Must be greater than radiusA
        val radiusB: Double,
        val probe1: Point,
        val probe2: Point
    )

    data class Setup(
        val circleA: Element.Circle,
        val circleB: Element.Circle,
        val chordB: Point
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                radiusA = 0.2,
                radiusB = 0.351,
                probe1 = Point(0.1, 0.1),
                probe2 = Point(0.6, 0.2)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.0),
                radiusA = 0.20492,
                radiusB = 0.351222,
                probe1 = Point(0.101, 0.133),
                probe2 = Point(0.644, 0.2342)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circleA = Element.Circle(center, radiusA)
                    val circleB = Element.Circle(center, radiusB)
                }
                namer.nameReflected(context)
                with(context) {
                    val prep = object {
                        val probe = lineTool(probe1, probe2)
                        val probeA = intersectTwoPoints(probe, circleA).first
                        val chordB = intersectTwoPoints(probe, circleB).first
                    }
                    namer.nameReflected(prep)
                    with(prep) {
                        return Setup(circleA, circleB, chordB) to EuclideaContext(
                            config = EuclideaConfig(
                                maxSqDistance = sq(20.0),
                                perpendicularBisectorToolEnabled = true,
                                perpendicularToolEnabled = true,
                                parallelToolEnabled = true,
                                angleBisectorToolEnabled = true,
                                nonCollapsingCompassToolEnabled = true
                            ),
                            points = listOf(center, chordB, probeA),
                            elements = listOf(circleA, circleB)
                        )
                    }
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            return { context ->
                val last = context.elements.last()
                with(setup) {
                    if (last is Element.Line && pointAndLineCoincide(chordB, last)) {
                        try {
                            val intersectA = intersectTwoPoints(last, circleA)
                            val intersectB = intersectTwoPoints(last, circleB)
                            val lenA = (intersectA.second - intersectA.first).distance
                            val lenB = (intersectB.second - intersectB.first).distance
                            val lenAB = (intersectA.first - intersectB.first).distance
                            coincides(lenA, lenAB) && coincides(lenB, lenA * 3.0)
                        } catch (ex: IllegalStateException) {
                            // missing intersection points
                            false
                        }
                    } else false
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
//        }

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Suboptimal 7E solution
                    @Suppress("unused") val context = object {
                        val base = lineTool(center, chordB)
                        val pointA = intersectTwoPoints(base, circleA).first
                        val perpA = perpendicularTool(base, pointA)
                        val pointB = intersectTwoPoints(perpA, circleB).first
                        val slant = angleBisectorTool(pointB, pointA, chordB)
                        val parallel = perpendicularBisectorTool(pointB, pointA)
                        val slantP = intersectOnePoint(parallel, slant)
                        val circle = nonCollapsingCompassTool(pointB, slantP, chordB)
                        val aim = intersectTwoPoints(circle, circleA).first
                        val solution = lineTool(chordB, aim)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
