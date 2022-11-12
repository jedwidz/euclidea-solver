package euclidea

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
        Solver().improveSolution(0, 10)
    }

    @Test
    fun checkPrefixSolution() {
        Solver().checkPrefixSolution()
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
            // #$# temporarily the same, to work around false negatives
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(2.0011, 0.01),
                base3 = Point(1.5012, 1.2012)
            )
        }

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
            namer.set("base3", params.base3)
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
            val namer = Namer()

            // TODO factor out with same set in ImprovingSolver
            val referenceElements = ElementSet()
            referenceElements += referenceSolution(params, namer).second.elements

            val prefixNamer = Namer()
            val prefixContext = solutionPrefix(params, prefixNamer)?.second
            prefixContext?.let { referenceElements += it.elements }

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

//        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
//            // Euclidea 10E E-star moves hint
//            return { solveContext, element ->
//                when (solveContext.depth) {
//                    0 -> element !is Element.Line
//                    1 -> element !is Element.Circle
//                    2 -> element !is Element.Line
//                    3 -> element !is Element.Circle
//                    4 -> element !is Element.Circle
//                    5 -> element !is Element.Line
//                    6 -> element !is Element.Line
//                    7 -> element !is Element.Line
//                    8 -> element !is Element.Circle
//                    9 -> element !is Element.Line
//                    else -> false
//                }
//            }
//        }

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
                    EuclideaTools.lineTool(p1, p2)!!,
                    EuclideaTools.lineTool(p2, p3)!!,
                    EuclideaTools.lineTool(p3, p4)!!,
                    EuclideaTools.lineTool(p4, p1)!!
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
                    val line1 = namer.set("line1", EuclideaTools.lineTool(base1, base3)!!)
                    val circle1 = namer.set("circle1", EuclideaTools.circleTool(base3, base1)!!)
                    val point1 = namer.set("point1", intersectTwoPointsOther(circle1, line1, base1))
                    val solution1 = namer.set("solution1", EuclideaTools.lineTool(base2, point1)!!)
                    // Gets a second solution line, but maybe not part of optimal solution:
                    val start1 = namer.set("start1", EuclideaTools.circleTool(point1, base2)!!)
                    val start2 = namer.set("start2", EuclideaTools.circleTool(base2, point1)!!)
                    val (adj1, adj2) = namer.setAll("adj1", "adj2", intersectTwoPoints(start1, start2))
                    val bisect = namer.set("bisect", EuclideaTools.lineTool(adj1, adj2)!!)
                    val point2 = namer.set("point2", intersectOnePoint(bisect, solution1))
                    val solution2 = namer.set("solution2", EuclideaTools.lineTool(base3, point2)!!)

                    return setup to initialContext.withElements(
                        listOf(line1, circle1, solution1, start1, start2, bisect, solution2)
                    )
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
                    val start1 = namer.set("start1", EuclideaTools.circleTool(base1, base3)!!)
                    val start2 = namer.set("start2", EuclideaTools.circleTool(base3, base1)!!)
                    val base = namer.set("base", EuclideaTools.lineTool(base1, base3)!!)
                    val sol3P1 = namer.set("sol3P1", intersectTwoPointsOther(base, start1, base3))
                    val spread = namer.set("spread", EuclideaTools.circleTool(base2, sol3P1)!!)
                    val sol1P = namer.set("sol1P", intersectTwoPointsOther(spread, start1, sol3P1))
                    val solution1 = namer.set("solution1", EuclideaTools.lineTool(sol1P, base3)!!)
                    val sol2P = namer.set("sol2P", intersectTwoPointsOther(base, start2, base1))
                    val solution2 = namer.set("solution2", EuclideaTools.lineTool(sol2P, base2)!!)
                    val crossP = namer.set("crossP", intersectTwoPointsOther(solution2, start2, sol2P))
                    val cross = namer.set("cross", EuclideaTools.lineTool(crossP, base3)!!)
                    val sol4P = namer.set("sol4P", intersectTwoPointsOther(cross, start2, crossP))
                    val solution4 = namer.set("solution4", EuclideaTools.lineTool(sol4P, base1)!!)
                    val spread2 = namer.set("spread2", EuclideaTools.circleTool(base2, base3)!!)
                    val sol3P2 = namer.set("sol3P2", intersectTwoPointsOther(spread2, start1, base3))
                    val solution3 = namer.set("solution3", EuclideaTools.lineTool(sol3P2, sol3P1)!!)

                    return setup to initialContext.withElements(
                        listOf(
                            start1,
                            start2,
                            base,
                            spread,
                            solution1,
                            solution2,
                            cross,
                            solution3,
                            spread2,
                            solution4
                        )
                    )
                }
            }
        }
    }
}
