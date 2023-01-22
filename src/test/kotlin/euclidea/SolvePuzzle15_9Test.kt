package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertTrue

class SolvePuzzle15_9Test {
    // Circle with Center on Line

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        // ?
        Solver().improveSolution(
            maxExtraElements = 7,
            maxDepth = 7,
            nonNewElementLimit = 2,
//            consecutiveNonNewElementLimit = 2,
            useTargetConstruction = true
        )
    }

    data class Params(
        val center: Point,
        val radius: Double,
        val base: Point,
        val dir: Point,
        val sample: Point
    )

    data class Setup(
        val circle: Element.Circle,
        val line: Element.Line,
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                center = Point(0.0, 0.7),
                radius = 0.4,
                base = Point(0.1, 0.0),
                dir = Point(0.9, 0.0),
                sample = Point(1.0, 0.55),
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                center = Point(0.0, 0.701),
                radius = 0.412,
                base = Point(0.1011, 0.0),
                dir = Point(0.9011, 0.0),
                sample = Point(1.203, 0.564),
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = Element.Circle(center, radius)
                    val line = Element.Line(base, dir)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle, line) to EuclideaContext.of(
                        config = EuclideaConfig(
                            maxSqDistance = sq(5.0),
//                            parallelToolEnabled = true,
//                            perpendicularBisectorToolEnabled = true,
                            nonCollapsingCompassToolEnabled = true,
                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
                        ),
                        // base and dir act as probes
                        points = listOf(center, sample /* , base, dir */),
                        elements = listOf(circle, line)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // Validate solution
            assertTrue(pointAndLineCoincide(solution.center, setup.line))
            assertTrue(pointAndCircleCoincide(params.sample, solution))
            assertTrue(meetAtOnePoint(setup.circle, solution))
//            // Look for partial solution
//            val diameter1 = lineTool(params.center, solution.center)
//            val diameter2 = lineTool(params.sample, solution.center)
//            val elementsOfInterest = listOf(
//                solution,
//                perpendicularTool(setup.line, solution.center),
//                diameter1,
//                diameter2
//            )
//            val givenPoints = PointSet.of(listOf(params.center, params.sample, mirrorAcross(setup.line, params.sample)))
//            val givenElements = ElementSet.of(listOf(setup.line, setup.circle))
//            return { context ->
//                context.points.any { point ->
//                    point !in givenPoints && elementsOfInterest.any { element ->
//                        if (pointAndElementCoincide(
//                                point,
//                                element
//                            )
//                        ) {
//                            println("Partial solution: $point coincides with $element")
//                            true
//                        } else false
//                    }
//                } ||
//                        context.elements.any { element ->
//                            element !in givenElements &&
//                                    when (element) {
//                                        is Element.Circle -> if (coincides(element.radius, solution.radius)) {
//                                            println("Partial solution: $element has radius of $solution")
//                                            true
//                                        } else false
//                                        is Element.Line -> if (linesParallel(element, diameter1) || linesParallel(
//                                                element,
//                                                diameter2
//                                            )
//                                        ) {
//                                            println("Partial solution: $element is parallel with line of interest")
//                                            true
//                                        } else false
//                                    }
//                        }
//            }
            return { context ->
                context.hasElement(solution)
            }
        }

        private fun constructSolution(params: Params): Element.Circle {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    val bracket1 = perpendicularTool(line, center, probe = base)
                    val bracket2 = perpendicularTool(line, sample, probe = base)
                    val p1 = intersectOnePoint(line, bracket1)
                    val p2 = intersectOnePoint(line, bracket2)
                    val d = p2 - p1
                    val param = solveByBisection(0.0, 1.0) { x ->
                        val p = p1 + d * x
                        val d1 = (p - center).distance - radius
                        val d2 = (p - sample).distance
                        d1 - d2
                    }
                    val solutionCenter = p1 + d * param
                    val solution = circleTool(solutionCenter, sample)
                    return solution
                }
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Assumed partial solution, agreeing with hints
//                    @Suppress("unused") val context = object {
//                        val half = EuclideaTools.angleBisectorTool(baseA, baseO, baseB)
//                        // val perp = perpendicularTool(line2, sample, probe = baseO)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

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

        override fun toolSequence(): List<EuclideaTool> {
            // Euclidea 7E E-star moves hint
            return listOf(
                EuclideaTool.PerpendicularTool,
                EuclideaTool.CircleTool,
                EuclideaTool.LineTool,
                EuclideaTool.NonCollapsingCompassTool,
                EuclideaTool.NonCollapsingCompassTool,
                EuclideaTool.LineTool,
                EuclideaTool.CircleTool
            )
        }

//        override fun referenceSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Sub-optimal 5L solution
//                        val perpCenter = perpendicularTool(line, center, probe = base)
//                        val aim1 = intersectTwoPoints(perpCenter, circle).second
//                        val cross1 = lineTool(aim1, base)
//                        val touch = intersectTwoPoints(cross1, circle).second
//                        val cross2 = lineTool(touch, center)
//                        val perpBase = perpendicularTool(line, base, probe = touch)
//                        val solutionCenter = intersectOnePoint(perpBase, cross2)
//                        val solution = circleTool(solutionCenter, touch)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::alternateSolution)
//        }
//
//        fun alternateSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 5L solution
//                        val lineA = lineTool(center, baseA)
//                        val solutionA = perpendicularTool(lineA, baseA, probe = baseB)
//                        val bisectAB = perpendicularBisectorTool(baseA, baseB)
//
//                        // Needs to be the 'further away' intersection
//                        val pointC = intersectTwoPoints(bisectAB, circle).second
//                        val solutionC = perpendicularTool(bisectAB, pointC, probe = baseA)
//                        val vertex = intersectOnePoint(bisectAB, solutionA)
//                        val solutionB = lineTool(vertex, baseB)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
