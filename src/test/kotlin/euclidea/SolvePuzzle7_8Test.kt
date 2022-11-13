package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle7_8Test {
    // Circle Tangent to Three Lines

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 6)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val base3: Point
    )

    data class Setup(
        val base4: Point,
        val base: Element.Line,
        val par1: Element.Line,
        val par2: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(1.0011, 0.61),
                base3 = Point(1.1233, 0.88)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.043, 0.012),
                base2 = Point(1.011, 0.0601),
                base3 = Point(1.1235, 0.86)
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
                val base = namer.set("base", lineTool(base1, base2)!!)
                val par1 = namer.set("par1", lineTool(base1, base3)!!)
                val base4 = namer.set("base4", base2.plus(base3.minus(base1)))
                val par2 = namer.set("par2", lineTool(base2, base4)!!)
                return Setup(base4, base, par1, par2) to EuclideaContext(
                    config = EuclideaConfig(maxSqDistance = sq(8.0)),
                    points = listOf(base1, base2),
                    elements = listOf(base, par1, par2)
                )
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            with(setup) {
                return { context ->
                    when (val last = context.elements.lastOrNull()) {
                        is Element.Circle -> listOf(base, par1, par2).all { line ->
                            intersect(line, last) is Intersection.OnePoint
                        }
                        else -> false
                    }
                }
            }
        }

        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
            val referenceElements = ElementSet()
            referenceElements += referenceSolution(params, Namer()).second.elements

            val solutionElements = ElementSet()
            solutionElements += constructSolution(params)

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                solutionScore * 10 + referenceScore * 4
            }
        }

        private fun constructSolution(params: Params): List<Element> {
            // cheekily use reference solution
            val solution = referenceSolution(params, Namer()).second.elements.last()
            return listOf(solution)
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
                    // Optimal 6E solution
                    val startC1 = namer.set("startC1", circleTool(base1, base2)!!)
                    val startP1 = namer.set("startP1", intersectTwoPoints(startC1, par1).second)
                    val startL1 = namer.set("startL1", lineTool(startP1, base2)!!)
                    val expand = namer.set("expand", intersectTwoPointsOther(startC1, base, base2))
                    val perpC1 = namer.set("perpC1", circleTool(expand, base2)!!)
                    val perpC2 = namer.set("perpC2", circleTool(base2, startP1)!!)
                    val (perpP1, perpP2) = namer.setAll("perpP1", "perpP2", intersectTwoPoints(perpC2, perpC1))
                    val perp = namer.set("perp", lineTool(perpP1, perpP2)!!)
                    val center = namer.set("center", intersectOnePoint(perp, startL1))
                    val tangentP = namer.set("tangentP", intersectOnePoint(perp, base))
                    val solution = namer.set("solution", circleTool(center, tangentP)!!)

                    return setup to initialContext.withElements(
                        listOf(startC1, startL1, perpC1, perpC2, perp, solution)
                    )
                }
            }
        }
    }
}
