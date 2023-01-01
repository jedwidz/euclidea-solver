package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle8_4Test {
    // Regular Octagon

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // solution found ~26s
        Solver().improveSolution(3, 13)
    }

    data class Params(
        val pointA: Point,
        val pointB: Point
    )

    data class Setup(
        val base: Element.Line
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.0, 0.0)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                pointA = Point(0.0, 0.0),
                pointB = Point(1.011, 0.013)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val base = Element.Line(pointA, pointB, limit1 = true, limit2 = true)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(base) to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(10.0)),
                        points = listOf(pointA, pointB),
                        elements = listOf(base)
                    )
                }
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

//        override fun visitPriority(params: Params, setup: Setup): (SolveContext, Element) -> Int {
//            val referenceElements = ElementSet()
//            referenceElements += referenceSolution(params, Namer()).second.elements
//            referenceElements += solutionPrefix(params, Namer()).second.elements
//
//            val solutionElements = ElementSet()
//            solutionElements += constructSolution(params)
//
//            val interestPoints = pointsOfInterest(params)
//
//            return { _, element ->
//                val solutionScore = if (element in solutionElements) 1 else 0
//                val referenceScore = if (element in referenceElements) 1 else 0
//                val interestPointsScore = interestPoints.count { pointAndElementCoincide(it, element) }
//                solutionScore * 10 + referenceScore * 4 + interestPointsScore
//            }
//        }
//
//        private fun pointsOfInterest(params: Params): List<Point> {
//            return with(params) {
//                val center = midpoint(pointA, pointB)
//                val d = base3.minus(center)
//                listOf(
//                    center,
//                    pointA.plus(d),
//                    pointB.plus(d),
//                    pointB.minus(d),
//                    pointA.minus(d),
//                    center.minus(d)
//                )
//            }
//        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isCircleFromCircle
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isLineFromLine
                    3 -> !element.isCircleFromCircle
                    4 -> !element.isLineFromLine
                    5 -> !element.isLineFromLine
                    6 -> !element.isCircleFromCircle
                    7 -> !element.isCircleFromCircle
                    8 -> !element.isLineFromLine
                    9 -> !element.isLineFromLine
                    10 -> !element.isLineFromLine
                    11 -> !element.isLineFromLine
                    12 -> !element.isLineFromLine
                    else -> false
                }
            }
        }

        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
            val solutionElements = constructSolution(params)
            return { context ->
                solutionElements.count { !context.hasElement(it) }
            }
        }

        private fun constructSolution(params: Params): List<Element> {
            with(params) {
                // arbitrary choice of possible solutions
                var prev = pointA
                var curr = pointB
                val res = mutableListOf<Element.Line>()
                for (i in 1..7) {
                    val next = rotatePoint(curr, prev, -135.0)
                    prev = curr
                    curr = next
                    res.add(Element.Line(prev, curr))
                }
                return res.toList()
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Partial solution
//                    @Suppress("unused") val context = object {
//                        val line1 = lineTool(pointA, base3)
//                        val circle1 = circleTool(base3, pointA)
//                        val point1 = intersectTwoPointsOther(circle1, line1, pointA)
//                        val solution1 = lineTool(pointB, point1)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
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
                    // Suboptimal 14E solution
                    val bisectC1 = circleTool(pointB, pointA)
                    val bisectC2 = circleTool(pointA, pointB)
                    val bisectP = intersectTwoPoints(bisectC1, bisectC2)
                    val bisectP1 = bisectP.first
                    val bisectP2 = bisectP.second
                    val bisectL = lineTool(bisectP1, bisectP2)
                    val center = intersectOnePoint(bisectL, base)
                    val circle = circleTool(center, pointA)
                    val unit = intersectTwoPoints(circle, bisectL)
                    val up = unit.first
                    val down = unit.second
                    val vDown = lineTool(pointA, down)
                    val vUp = lineTool(down, pointB)
                    val bigP = intersectTwoPoints(vDown, bisectC2).first
                    val big = circleTool(pointB, bigP)
                    val solution6P = intersectTwoPointsOther(big, bisectC2, bigP)
                    val solution6 = lineTool(bigP, solution6P)
                    val solution2P1 = intersectTwoPoints(big, vDown).second
                    val solution2P2 = intersectTwoPoints(bisectC1, vUp).second
                    val solution2 = lineTool(solution2P1, solution2P2)
                    val hugeO = intersectTwoPoints(solution2, big).second
                    val huge = circleTool(hugeO, pointA)
                    val solution5A = intersectTwoPoints(huge, solution2).second
                    val solution5B = intersectOnePoint(huge, solution6)
                    val solution5 = lineTool(solution5A, solution5B)
                    val solution3P = intersectOnePoint(solution5, bisectL)
                    val solution3 = lineTool(solution3P, hugeO)
                    val big2 = circleTool(hugeO, pointB)
                    val solution4A = intersectTwoPoints(big2, solution5).second
                    val solution4B = intersectTwoPoints(big2, vUp).second
                    val solution4 = lineTool(solution4A, solution4B)
                    @Suppress("unused") val context = object {
                        val bisectC1 = circleTool(pointB, pointA)
                        val bisectC2 = circleTool(pointA, pointB)
                        val bisectP = intersectTwoPoints(bisectC1, bisectC2)
                        val bisectP1 = bisectP.first
                        val bisectP2 = bisectP.second
                        val bisectL = lineTool(bisectP1, bisectP2)
                        val center = intersectOnePoint(bisectL, base)
                        val circle = circleTool(center, pointA)
                        val unit = intersectTwoPoints(circle, bisectL)
                        val up = unit.first
                        val down = unit.second
                        val vDown = lineTool(pointA, down)
                        val vUp = lineTool(down, pointB)
                        val bigP = intersectTwoPoints(vDown, bisectC2).first
                        val big = circleTool(pointB, bigP)
                        val solution6P = intersectTwoPointsOther(big, bisectC2, bigP)
                        val solution6 = lineTool(bigP, solution6P)
                        val solution2P1 = intersectTwoPoints(big, vDown).second
                        val solution2P2 = intersectTwoPoints(bisectC1, vUp).second
                        val solution2 = lineTool(solution2P1, solution2P2)
                        val hugeO = intersectTwoPoints(solution2, big).second
                        val huge = circleTool(hugeO, pointA)
                        val solution5A = intersectTwoPoints(huge, solution2).second
                        val solution5B = intersectOnePoint(huge, solution6)
                        val solution5 = lineTool(solution5A, solution5B)
                        val solution3P = intersectOnePoint(solution5, bisectL)
                        val solution3 = lineTool(solution3P, hugeO)
                        val big2 = circleTool(hugeO, pointB)
                        val solution4A = intersectTwoPoints(big2, solution5).second
                        val solution4B = intersectTwoPoints(big2, vUp).second
                        val solution4 = lineTool(solution4A, solution4B)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
