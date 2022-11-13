package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle7_11Test {
    // Excircle

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

    data class Setup(
        val base12: Element.Line,
        val base23: Element.Line,
        val base31: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(0.3011, 0.61),
                base3 = Point(0.6233, -0.12)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(0.3021, 0.612),
                base3 = Point(0.6223, -0.121)
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
                val base12 = namer.set("base12", lineTool(base1, base2)!!)
                val base23 = namer.set("base23", lineTool(base2, base3)!!)
                val base31 = namer.set("base31", lineTool(base3, base1)!!)
                return Setup(base12, base23, base31) to EuclideaContext(
                    config = EuclideaConfig(maxSqDistance = sq(8.0)),
                    points = listOf(base1, base2, base3),
                    elements = listOf(base12, base23, base31)
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
                        is Element.Circle -> listOf(base12, base23, base31).all { line ->
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
                    // Suboptimal 10E solution
                    val startC1 = namer.set("startC1", circleTool(base1, base2)!!)
                    val startC2 = namer.set("startC2", circleTool(base2, base1)!!)
                    val startP1 = namer.set("startP1", intersectTwoPoints(startC1, base31).first)
                    val bisectC1 = namer.set("bisectC1", circleTool(startP1, base1)!!)
                    val bisectP1 = namer.set("bisectP1", intersectTwoPointsOther(bisectC1, startC2, base1))
                    val bisectL1 = namer.set("bisectL1", lineTool(bisectP1, base1)!!)
                    val startP2 = namer.set("startP2", intersectTwoPoints(startC2, base23).second)
                    val bisectC2 = namer.set("bisectC2", circleTool(startP2, base2)!!)
                    val bisectP2 = namer.set("bisectP2", intersectTwoPointsOther(bisectC2, startC1, base2))
                    val bisectL2 = namer.set("bisectL2", lineTool(bisectP2, base2)!!)
                    val center = namer.set("center", intersectOnePoint(bisectL2, bisectL1))
                    val perpC1 = namer.set("perpC1", circleTool(base1, center)!!)
                    val perpC2 = namer.set("perpC2", circleTool(base2, center)!!)
                    val (perpP1, perpP2) = namer.setAll("perpP1", "perpP2", intersectTwoPoints(perpC2, perpC1))
                    val perp = namer.set("perp", lineTool(perpP1, perpP2)!!)
                    val tangentP = namer.set("tangentP", intersectOnePoint(perp, base12))
                    val solution = namer.set("solution", circleTool(center, tangentP)!!)

                    return setup to initialContext.withElements(
                        listOf(startC1, startC2, bisectC1, bisectL1, bisectC2, bisectL2, perpC1, perpC2, perp, solution)
                    )
                }
            }
        }
    }
}