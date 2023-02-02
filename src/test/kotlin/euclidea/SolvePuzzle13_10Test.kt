package euclidea

import euclidea.EuclideaTools.angleBisectorTool
import euclidea.EuclideaTools.lineTool
import euclidea.EuclideaTools.nonCollapsingCompassTool
import euclidea.EuclideaTools.perpendicularTool
import org.junit.jupiter.api.Test

class SolvePuzzle13_10Test {
    // Billiards on Round Table

    @Test
    fun checkSolution() {
        Solver().checkReferenceSolution()
    }

    @Test
    fun improveSolution() {
        Solver().improveSolution(
            // solution found 3 sec
            maxExtraElements = 5,
            maxDepth = 6,
            maxUnfamiliarElements = 2,
//            maxNonNewElements = 4,
//            maxConsecutiveNonNewElements = 2,
            useTargetConstruction = true,
            fillKnownElements = true
        )
    }

    data class Params(
        val baseO: Point,
        val dir: Point,
        val baseAScale: Double,
        val baseBScale: Double,
        val radiusScale: Double,
        val probeDir: Point
    ) {
        val baseA = baseO + (dir - baseO) * baseAScale
        val baseB = baseO + (dir - baseO) * baseBScale
        val radius = dir.distance * radiusScale

        // Probe point on circle
        val probe = baseO + (probeDir - baseO) * (radius / probeDir.distance)
    }

    data class Setup(
        val circle: Element.Circle
    )

    class Solver : ImprovingSolver<Params, Setup>() {

        override fun makeParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                dir = Point(-0.5, 0.4),
                baseAScale = 0.6,
                baseBScale = -0.35,
                radiusScale = 1.0,
                probeDir = Point(0.2, 0.6)
            )
        }

        override fun makeReplayParams(): Params {
            return Params(
                baseO = Point(0.0, 0.0),
                dir = Point(-0.501, 0.402),
                baseAScale = 0.603,
                baseBScale = -0.3544,
                radiusScale = 1.0013,
                probeDir = Point(0.206, 0.607)
            )
        }

        override fun initialContext(
            params: Params,
            namer: Namer
        ): Pair<Setup, EuclideaContext> {
            with(params) {
                val context = object {
                    val circle = Element.Circle(baseO, radius)
                }
                namer.nameReflected(context)
                with(context) {
                    return Setup(circle) to EuclideaContext.of(
                        config = EuclideaConfig(
//                            perpendicularBisectorToolEnabled = true,
//                            perpendicularToolEnabled = true,
//                            angleBisectorToolEnabled = true,
//                            nonCollapsingCompassToolEnabled = true,
//                            parallelToolEnabled = true,
                            maxSqDistance = sq(8.0)
                        ),
                        points = listOf(baseO, baseA, baseB /*, probe*/),
                        elements = listOf(circle)
                    )
                }
            }
        }

        override fun isSolution(
            params: Params,
            setup: Setup
        ): (EuclideaContext) -> Boolean {
            val solution = constructSolution(params)
            // check condition
            with(params) {
                with(setup) {
                    solution.all { pointC ->
                        if (pointAndCircleCoincide(pointC, circle)) {
                            val bisect = angleBisectorTool(baseA, pointC, baseB)
                            val lineOC = lineTool(baseO, pointC)
                            coincides(bisect, lineOC)
                        } else false
                    }
                }
            }
            return { context ->
                solution.any { context.hasPoint(it) }
            }
        }

        private fun constructSolution(params: Params): List<Point> {
            val namer = Namer()
            val (setup, _) = initialContext(
                params, namer
            )
            with(params) {
                with(setup) {
                    // Sub-optimal 11L solution
                    val diameter = lineTool(baseO, baseA)
                    val perpO = perpendicularTool(diameter, baseO, probe = probe)
                    val perpB = perpendicularTool(diameter, baseB, probe = probe)
                    val pointD = intersectTwoPoints(perpO, circle).second
                    val parD = perpendicularTool(perpO, pointD, probe = baseA)
                    val pointE = intersectOnePoint(parD, perpB)
                    val lineAE = lineTool(pointE, baseA)
                    val lineAD = lineTool(pointD, baseA)
                    val lineOE = lineTool(pointE, baseO)
                    val pointF = intersectOnePoint(lineOE, lineAD)
                    val parF = perpendicularTool(perpO, pointF, probe = baseO)
                    val pointG = intersectOnePoint(parF, lineAE)
                    val pointH = intersectOnePoint(parF, perpO)
                    val measure = nonCollapsingCompassTool(pointH, pointG, baseO)
                    val pointI = intersectTwoPoints(diameter, circle).second
                    val perpI = perpendicularTool(diameter, pointI, probe = probe)
                    val aim = intersectTwoPoints(perpI, measure).second
                    val bisect = lineTool(baseO, aim)
                    val solution = intersectTwoPoints(bisect, circle)
                    return solution.toList()
                }
            }
        }

//        override fun solutionPrefix(params: Params, namer: Namer): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    // Find the first point on the solution line
//                    @Suppress("unused") val context = object {
//                        val extend1 = lineTool(baseO, baseAScale)
//                        val extend2 = lineTool(dir, baseB)
//                        val apex = intersectOnePoint(extend1, extend2)
//                        val circle1 = circleTool(baseO, apex)
//                        val low = intersectTwoPointsOther(circle1, extend1, apex)
//                        val circle2 = circleTool(low, baseAScale)
//                        val side = intersectTwoPoints(circle2, circle1).second
//                        val cross = lineTool(side, baseAScale)
//                        val aim = intersectTwoPoints(cross, circle1).second
//                        val circle3 = circleTool(apex, aim)
//                        val solutionP = intersectOnePoint(circle3, side1)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }

//        override fun remainingStepsLowerBound(params: Params, setup: Setup): (EuclideaContext) -> Int {
//            with(setup) {
//                val solution = constructSolution(params)
//                val point1 = intersectOnePoint(solution, side1)
//                val point2 = intersectOnePoint(solution, side2)
//                return { context ->
//                    // Assumes that solution is the last element (no extraneous elements)
//                    if (context.elements.lastOrNull()?.let { coincides(it, solution) } == true)
//                        0
//                    else {
//                        val onPoint1 = context.elements.count { pointAndElementCoincide(point1, it) }
//                        val onPoint2 = context.elements.count { pointAndElementCoincide(point2, it) }
//                        // Assume solution uses at least one of the highlighted points
//                        max(0, min(2 - onPoint1, 2 - onPoint2)) + 1
//                    }
//                }
//            }
//        }

        override fun pass(params: Params, setup: Setup): ((SolveContext, Element) -> Boolean) {
            // Euclidea 6E E-star moves hint
            return { solveContext, element ->
                when (solveContext.depth) {
                    0 -> !element.isLineFromLine
                    1 -> !element.isCircleFromCircle
                    2 -> !element.isCircleFromCircle
                    3 -> !element.isLineFromLine
                    4 -> !element.isLineFromLine
                    5 -> !element.isCircleFromCircle
                    else -> false
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
                    @Suppress("unused") val context = object {
                        // Sub-optimal 11L solution
                        val diameter = lineTool(baseO, baseA)
                        val perpO = perpendicularTool(diameter, baseO, probe = probe)
                        val perpB = perpendicularTool(diameter, baseB, probe = probe)
                        val pointD = intersectTwoPoints(perpO, circle).second
                        val parD = perpendicularTool(perpO, pointD, probe = baseA)
                        val pointE = intersectOnePoint(parD, perpB)
                        val lineAE = lineTool(pointE, baseA)
                        val lineAD = lineTool(pointD, baseA)
                        val lineOE = lineTool(pointE, baseO)
                        val pointF = intersectOnePoint(lineOE, lineAD)
                        val parF = perpendicularTool(perpO, pointF, probe = baseO)
                        val pointG = intersectOnePoint(parF, lineAE)
                        val pointH = intersectOnePoint(parF, perpO)
                        val measure = nonCollapsingCompassTool(pointH, pointG, baseO)
                        val pointI = intersectTwoPoints(diameter, circle).second
                        val perpI = perpendicularTool(diameter, pointI, probe = probe)
                        val aim = intersectTwoPoints(perpI, measure).second
                        val bisect = lineTool(baseO, aim)
                        val solution = intersectTwoPoints(bisect, circle).second
                    }
                    namer.nameReflected(context)
                    return setup to initialContext.withElements(elementsReflected(context))
                }
            }
        }

//        override fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
//            return listOf(this::optimal6LSolution)
//        }
//
//        private fun optimal6LSolution(
//            params: Params,
//            namer: Namer
//        ): Pair<Setup, EuclideaContext> {
//            val (setup, initialContext) = initialContext(
//                params, namer
//            )
//            with(params) {
//                with(setup) {
//                    @Suppress("unused") val context = object {
//                        // Optimal 6L solution
//                        val circle1 = nonCollapsingCompassTool(baseAScale, baseB, baseO)
//                        val right = intersectOnePoint(circle1, circle)
//                        val circle2 = circleTool(right, baseO)
//                        val bisect = perpendicularBisectorTool(baseO, dir)
//                        val center3 = intersectTwoPoints(bisect, circle2).second
//                        val circle3 = circleTool(baseO, center3)
//                        val point = intersectTwoPointsOther(circle3, circle, baseO)
//                        val parallel = parallelTool(side1, point, probe = baseO)
//                        val solutionP2 = intersectOnePoint(parallel, side2)
//                        val solution = parallelTool(circle, solutionP2, probe = dir)
//                    }
//                    namer.nameReflected(context)
//                    return setup to initialContext.withElements(elementsReflected(context))
//                }
//            }
//        }
    }
}
