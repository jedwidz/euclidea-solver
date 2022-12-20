package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test

class SolvePuzzle6_9Test {
    // Nine Point Circle

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Checks for center of circle, not the circle itself
        // maxExtraElement = 2, maxDepth = 9 - 1 - success 1 hr 40 min
        Solver().improveSolution(0, 9 - 1, 4)
    }

    data class Params(
        val base1: Point,
        val base2: Point,
        val base3: Point
    )

    data class Setup(
        val triangle12: Element.Line,
        val triangle23: Element.Line,
        val triangle31: Element.Line
    )

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
            namer.nameReflected(params)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val triangle12 = lineTool(base1, base2)
                    val triangle23 = lineTool(base2, base3)
                    val triangle31 = lineTool(base3, base1)
                }
                namer.nameReflected(context)
                with(context) {
                    val setup = Setup(triangle12, triangle23, triangle31)
                    return setup to EuclideaContext(
                        config = EuclideaConfig(maxSqDistance = sq(8.0)),
                        points = listOf(base1, base2, base3),
                        elements = listOf(triangle12, triangle23, triangle31)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val namer = Namer()
            val (_, referenceSolution) = referenceSolution(params, namer)
            val solutionCircle = referenceSolution.elements.last() as Element.Circle
            val solutionCenter = solutionCircle.center
            return { context ->
                context.hasPoint(solutionCenter)
                // context.hasElement(solutionCircle)
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
                    // Optimal 9E solution
                    @Suppress("unused") val context = object {
                        val start1 = circleTool(base1, base2)
                        val start2 = circleTool(base2, base1)
                        val adj = intersectTwoPoints(start1, start2)
                        val adj1 = adj.first
                        val adj2 = adj.second
                        val bisect = lineTool(adj1, adj2)
                        val cross1 = intersectTwoPointsOther(start1, triangle23, base2)
                        val cross2 = intersectTwoPointsOther(start2, triangle31, base1)
                        val cross = lineTool(cross1, cross2)
                        val half = intersectOnePoint(bisect, triangle12)
                        val pupil = circleTool(half, base1)
                        val narrow = intersectTwoPointsOther(pupil, triangle23, base2)
                        val back = circleTool(narrow, half)

                        // Maybe needs to be the intersection inside the triangle?
                        val inner = intersectAnyPoint(back, cross)
                        val cut = lineTool(inner, half)
                        val adjb = intersectTwoPoints(pupil, back)
                        val adjb1 = adjb.first
                        val adjb2 = adjb.second
                        val bisect2 = lineTool(adjb1, adjb2)
                        val center = intersectOnePoint(bisect2, cut)
                        val solution = circleTool(center, half)
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }
    }
}
