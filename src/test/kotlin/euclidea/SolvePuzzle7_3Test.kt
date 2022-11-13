package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle7_3Test {
    // Angle of 75 degrees

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 5)
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

        override fun nameParams(params: Params, namer: Namer) {
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val base = namer.set("base", lineTool(base1, base2)!!)
                return Setup(base) to EuclideaContext(
                    config = EuclideaConfig(maxSqDistance = sq(8.0)),
                    points = listOf(base1, probe),
                    elements = listOf(base)
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

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                solutionScore * 10 + referenceScore * 4
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 5E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> element !is Element.Circle
                    1 -> element !is Element.Circle
                    2 -> element !is Element.Circle
                    3 -> element !is Element.Line
                    4 -> element !is Element.Line
                    else -> false
                }
            }
        }

        private fun constructSolution(params: Params): List<Element> {
            with(params) {
                // arbitrary choice of possible solutions
                val solP = rotatePoint(base1, base2, 75.0)
                val solution = lineTool(base1, solP)!!
                return listOf(solution)
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
                    // Suboptimal 6E solution
                    val start = namer.set("start", circleTool(probe, base1)!!)
                    val cut = namer.set("cut", intersectTwoPointsOther(start, base, base1))
                    val lens = namer.set("lens", circleTool(cut, probe)!!)
                    val (_, adj1) = namer.setAll("adj2", "adj1", intersectTwoPoints(lens, start))
                    val support = namer.set("support", circleTool(base1, adj1)!!)
                    // Which side?
                    val (_, bisectP) = namer.set("bisectP", intersectTwoPoints(support, base))
                    val bisect1 = namer.set("bisect1", circleTool(adj1, bisectP)!!)
                    val bisect2 = namer.set("bisect2", circleTool(bisectP, adj1)!!)
                    val (sol1, sol2) = namer.setAll("sol1", "sol2", intersectTwoPoints(bisect1, bisect2))
                    val solution = namer.set("solution", lineTool(sol1, sol2)!!)

                    return setup to initialContext.withElements(
                        listOf(start, lens, support, bisect1, bisect2, solution)
                    )
                }
            }
        }
    }
}
