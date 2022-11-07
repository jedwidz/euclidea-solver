package euclidea

import org.junit.jupiter.api.Test

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

    data class Params(
        val base1: Point,
        val base2: Point,
        val base3: Point
    )

    object Setup

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(2.0, 0.0),
                base3 = Point(1.5, 1.2)
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
                context.hasElements(solutionElements)
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

        override fun referenceSolution(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            val (setup, initialContext) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Suboptimal 15E solution
                    val start1 = namer.set("start1", EuclideaTools.circleTool(base1, base2)!!)
                    val start2 = namer.set("start2", EuclideaTools.circleTool(base2, base1)!!)
                    val (adj1, adj2) = namer.setAll("adj1", "adj2", intersectTwoPoints(start1, start2))
                    val bisect = namer.set("bisect", EuclideaTools.lineTool(adj1, adj2)!!)
                    val base = namer.set("base", EuclideaTools.lineTool(base1, base2)!!)
                    val center = namer.set("center", intersectOnePoint(base, bisect))
                    val axis = namer.set("axis", EuclideaTools.lineTool(center, base3)!!)
                    val centric = namer.set("centric", EuclideaTools.circleTool(center, base3)!!)
                    val sideP = namer.set("sideP", intersectAnyPoint(centric, base))
                    val sideP2 = namer.set("sideP2", intersectTwoPointsOther(centric, axis, base3))
                    val sideC = namer.set("sideC", EuclideaTools.circleTool(sideP, sideP2)!!)
                    val solP1 = namer.set("solP1", intersectTwoPointsOther(sideC, centric, sideP2))
                    val solution1 = namer.set("solution1", EuclideaTools.lineTool(solP1, base3)!!)
                    val small = namer.set("small", EuclideaTools.circleTool(center, base1)!!)
                    val axialP = namer.set("axialP", intersectAnyPoint(small, axis))
                    val axialC = namer.set("axialC", EuclideaTools.circleTool(axialP, base1)!!)
                    val solP2 = namer.set("solP2", intersectTwoPointsOther(axialC, small, base1))
                    val solution2 = namer.set("solution2", EuclideaTools.lineTool(solP2, base2)!!)
                    val corner = namer.set("corner", intersectOnePoint(solution1, solution2))
                    val cross = namer.set("cross", EuclideaTools.lineTool(corner, center)!!)
                    val big = namer.set("big", EuclideaTools.circleTool(center, corner)!!)
                    val opp = namer.set("opp", intersectTwoPointsOther(big, cross, corner))
                    val solution3 = namer.set("solution3", EuclideaTools.lineTool(opp, base1)!!)
                    val solution4 = namer.set("solution4", EuclideaTools.lineTool(opp, sideP2)!!)

                    return setup to initialContext.withElements(
                        listOf(
                            start1,
                            start2,
                            bisect,
                            base,
                            axis,
                            centric,
                            sideC,
                            solution1,
                            small,
                            axialC,
                            solution2,
                            cross,
                            big,
                            solution3,
                            solution4
                        )
                    )
                }
            }
        }
    }
}
