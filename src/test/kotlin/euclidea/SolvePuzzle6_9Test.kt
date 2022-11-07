package euclidea

import org.junit.jupiter.api.Test

class SolvePuzzle6_9Test {
    // Nine Point Circle
    // (circles only)

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // Checks for center of circle, not the circle itself
        // maxExtraElement = 2, maxDepth = 9 - 1 - success 1 hr 40 min
        Solver().improveSolution(2, 9 - 1)
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
            namer.set("base1", params.base1)
            namer.set("base2", params.base2)
            namer.set("base3", params.base3)
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val triangle12 = namer.set("triangle12", EuclideaTools.lineTool(base1, base2)!!)
                val triangle23 = namer.set("triangle23", EuclideaTools.lineTool(base2, base3)!!)
                val triangle31 = namer.set("triangle31", EuclideaTools.lineTool(base3, base1)!!)
                val setup = Setup(triangle12, triangle23, triangle31)
                return setup to EuclideaContext(
                    config = EuclideaConfig(maxSqDistance = sq(8.0)),
                    points = listOf(base1, base2, base3),
                    elements = listOf(triangle12, triangle23, triangle31)
                )
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
                    // Suboptimal 5L-13E solution
                    val (perp1, perp1Cons) = namer.setCons(
                        "perp1",
                        EuclideaTools.dropPerpendicular(base2, base3, base1)!!
                    )
                    val (perp3, perp3Cons) = namer.setCons(
                        "perp3",
                        EuclideaTools.dropPerpendicular(base1, base2, base3)!!
                    )
                    val foot1 = namer.set("foot1", intersectOnePoint(perp1, triangle23))
                    val foot2 = namer.set("foot2", intersectOnePoint(perp3, triangle12))
                    val (cross, crossCons) = namer.setCons("cross", EuclideaTools.bisect(foot1, foot2)!!)
                    val mid2 = namer.set("mid2", intersectOnePoint(cross, triangle31))
                    val (pincer, pincerCons) = namer.setCons("pincer", EuclideaTools.bisect(mid2, foot1)!!)
                    val center = namer.set("center", intersectOnePoint(pincer, cross))
                    val solution = namer.set("solution", EuclideaTools.circleTool(center, mid2)!!)

                    return setup to initialContext
                        .withElements(perp1Cons).withElement(perp1)
                        .withElements(perp3Cons).withElement(perp3)
                        .withElements(crossCons).withElement(cross)
                        .withElements(pincerCons).withElement(pincer)
                        .withElement(solution)
                }
            }
        }
    }
}
