package euclidea

import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle15_10Test {
    // Angle of 3 degrees

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found 25 sec
        Solver().improveSolution(
            maxExtraElements = 7,
            maxDepth = 7,
            maxNonNewElements = 1,
            consecutiveNonNewElementLimit = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val probe: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(2.0011, 0.01),
                probe = Point(0.1233, 0.88)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.043, 0.012),
                base2 = Point(2.011, 0.0001),
                probe = Point(0.1235, 0.86)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = Element.Line(base1, base2, limit1 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(12.0),
//                            angleBisectorToolEnabled = true
                        ),
                        points = listOf(base1, base2 /*, probe*/),
                        elements = listOf(base)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            return { context ->
                context.hasElement(solution)
            }
        }

        private fun constructSolution(params: Params): Element.Line {
            with(params) {
                // arbitrary choice of possible solutions
                val solP = rotatePoint(base1, base2, 3.0)
                val solution = lineTool(base1, solP)
                return solution
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        val bisect = perpendicularBisect(pointA, pointB)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            val solution = constructSolution(params)
//            return { context ->
//                // Assumes that solution is the last element (no extraneous elements)
//                if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                    0
//                else {
//                    val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
//                    // Need two elements to locate center, then the solution circle itself
//                    max(0, 2 - onCenter) + 1
//                }
//            }
//        }

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 7E E-star moves hint
            return listOf(
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool
            )
        }

//        override fun referenceSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Sub-optimal 7L solution
////                        val bisect = perpendicularBisectorTool(pointA, pointB)
//                        val intercept = intersectOnePoint(bisect, line)
//                        val lineB = lineTool(intercept, pointB)
//                        val perp = perpendicularTool(line, base, probe = pointB)
//                        val center1 = intersectOnePoint(perp, bisect)
//                        val circle1 = circleTool(center1, base)
//                        val aim1 = intersectTwoPoints(circle1, lineB).first
//                        val cross1 = lineTool(center1, aim1)
//                        val cross2 = parallelTool(cross1, pointB, probe = center1)
//                        val center2 = intersectOnePoint(cross2, bisect)
//                        val solution = circleTool(center2, pointB)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
