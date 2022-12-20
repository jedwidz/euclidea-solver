package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle6_11Test {
    // Parallelogram by Three Midpoints

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 10, 5)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val base3: Point
    )

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        private val partialSolutionSize = 4

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(2.0011, 0.01),
                base3 = Point(1.5012, 1.2012)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.043, 0.012),
                base2 = Point(2.011, 0.0001),
                base3 = Point(1.5091, 1.2031)
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.nameReflected(params)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                return Setup to EuclideaContext(
                    config = EuclideaConfig(maxSqDistance = sq(8.0)),
                    points = listOf(base1, base2, base3),
                    elements = listOf()
                )
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solutionElements = constructSolution(params)
            return { context ->
                // Partial solution
                solutionElements.count { context.hasElement(it) } >= partialSolutionSize
                // context.hasElements(solutionElements)
            }
        }

        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
            val referenceElements = ElementSet()
            referenceElements += referenceSolution(params, Namer()).second.elements
            referenceElements += solutionPrefix(params, Namer()).second.elements

            val solutionElements = ElementSet()
            solutionElements += constructSolution(params)

            val interestPoints = pointsOfInterest(params)

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                val interestPointsScore = interestPoints.count { pointAndElementCoincide(it, element) }
                solutionScore * 10 + referenceScore * 4 + interestPointsScore
            }
        }

        private fun pointsOfInterest(params: Params): List<Point> {
            return with(params) {
                val center = midpoint(base1, base2)
                val d = base3.minus(center)
                listOf(
                    center,
                    base1.plus(d),
                    base2.plus(d),
                    base2.minus(d),
                    base1.minus(d),
                    center.minus(d)
                )
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 10E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> element !is Element.Line
                    1 -> element !is Element.Circle
                    2 -> element !is Element.Line
                    3 -> element !is Element.Circle
                    4 -> element !is Element.Circle
                    5 -> element !is Element.Line
                    6 -> element !is Element.Line
                    7 -> element !is Element.Line
                    8 -> element !is Element.Circle
                    9 -> element !is Element.Line
                    else -> false
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params)
            return { context ->
                // Partial solution
                val res = max(0, partialSolutionSize - solutionElements.count { context.hasElement(it) })
                res
                // solutionElements.count { !context.hasElement(it) }
            }
        }

        private fun constructSolution(params: Params): List<Element> {
            with(params) {
                // arbitrary choice of possible solutions
                val center = midpoint(base1, base2)
                val d = base3.minus(center)
                val p1 = base1.plus(d)
                val p2 = base2.plus(d)
                val p3 = base2.minus(d)
                val p4 = base1.minus(d)
                return listOf(
                    lineTool(p1, p2),
                    lineTool(p2, p3),
                    lineTool(p3, p4),
                    lineTool(p4, p1)
                )
            }
        }

        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Partial solution
                    @Suppress("unused") val context = object {
                        val line1 = lineTool(base1, base3)
                        val circle1 = circleTool(base3, base1)
                        val point1 = intersectTwoPointsOther(circle1, line1, base1)
                        val solution1 = lineTool(base2, point1)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
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
                    // Optimal 10E solution
                    @Suppress("unused") val context = object {
                        val base = lineTool(base1, base3)
                        val start2 = circleTool(base3, base1)
                        val sol2P = intersectTwoPointsOther(base, start2, base1)
                        val solution2 = lineTool(sol2P, base2)
                        val start1 = circleTool(base1, base3)
                        val sol3P1 = intersectTwoPointsOther(base, start1, base3)
                        val spread = circleTool(base2, sol3P1)
                        val sol1P = intersectTwoPointsOther(spread, start1, sol3P1)
                        val solution1 = lineTool(sol1P, base3)
                        val crossP = intersectTwoPointsOther(solution2, start2, sol2P)
                        val cross = lineTool(crossP, base3)
                        val sol4P = intersectTwoPointsOther(cross, start2, crossP)
                        val solution4 = lineTool(sol4P, base1)
                        val spread2 = circleTool(base2, base3)
                        val sol3P2 = intersectTwoPointsOther(spread2, start1, base3)
                        val solution3 = lineTool(sol3P2, sol3P1)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
