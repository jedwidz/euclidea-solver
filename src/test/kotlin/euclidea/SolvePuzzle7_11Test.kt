package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test
import kotlin.math.max

class SolvePuzzle7_11Test {
    // Excircle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(0, 8, 4)
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
                base1 = Point(0.03, 0.04),
                base2 = Point(0.3011, 0.61),
                base3 = Point(0.6233, -0.12)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                base1 = Point(0.0, 0.0),
                base2 = Point(0.2031, 0.612),
                base3 = Point(0.5123, -0.121)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base12 = lineTool(base1, base2)
                    val base23 = lineTool(base2, base3)
                    val base31 = lineTool(base3, base1)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base12, base23, base31) to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(8.0)),
                        points = listOf(base1, base2, base3),
                        elements = listOf(base12, base23, base31)
                    )
                }
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

            val interestPoints = pointsOfInterest(params, setup)

            return { _, element ->
                val solutionScore = if (element in solutionElements) 1 else 0
                val referenceScore = if (element in referenceElements) 1 else 0
                val interestPointsScore = interestPoints.count { pointAndElementCoincide(it, element) }
                solutionScore * 10 + referenceScore * 4 + interestPointsScore
            }
        }

        private fun pointsOfInterest(params: Params, setup: Setup): List<Point> {
            return with(setup) {
                val solution = constructSolution(params)
                val tangents = listOf(base12, base23, base31).map { intersectOnePoint(it, solution) }
                val center = solution.center
                listOf(center) + tangents
            }
        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean)? {
            // Euclidea 8E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> element !is Element.Circle
                    1 -> element !is Element.Circle
                    2 -> element !is Element.Circle
                    3 -> element !is Element.Circle
                    4 -> element !is Element.Circle
                    5 -> element !is Element.Line
                    6 -> element !is Element.Line
                    7 -> element !is Element.Circle
                    else -> false
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solution = constructSolution(params)
            val center = solution.center
            return { context ->
                // Assumes that solution is the last element (no extraneous elements)
                if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
                    0
                else {
                    val onCenter = context.elements.count { pointAndElementCoincide(center, it) }
                    // Need two elements to locate center, then the solution circle itself
                    max(0, 2 - onCenter) + 1
                }
            }
        }

        private fun constructSolution(params: Params): Element.Circle {
            // cheekily use reference solution
            return referenceSolution(params, Namer()).second.elements.last() as Element.Circle
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
                    // Optimal 8E solution
                    @Suppress("unused") val context = object {
                        val startC1 = circleTool(base1, base2)
                        val outerP1 = intersectTwoPoints(startC1, base31).second
                        val outerC1 = circleTool(base3, outerP1)
                        val outerP2 = intersectTwoPoints(outerC1, base23).first
                        val perpC1 = circleTool(outerP2, base2)
                        val perpC2 = circleTool(base2, outerP2)
                        val all = intersectTwoPoints(perpC2, perpC1)
                        val perpP1 = all.first
                        val perpP2 = all.second
                        val sideP = intersectTwoPoints(perpC2, base12).first
                        val bisectC = circleTool(sideP, base2)
                        val perp = lineTool(perpP1, perpP2)
                        val bisectP = intersectTwoPointsOther(bisectC, perpC1, base2)
                        val bisectL = lineTool(bisectP, base2)
                        val center = intersectOnePoint(bisectL, perp)
                        val tangentP = intersectOnePoint(perp, base23)
                        val solution = circleTool(center, tangentP)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
