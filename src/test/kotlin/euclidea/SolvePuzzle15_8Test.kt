package euclidea

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle15_8Test {

    @Test
    fun puzzle15_8_check_solution() {
        // Line-Circle Intersection*
        // (circles only)
        // Reproduce and check my best solution so far
        val namer = Namer()
        val base1 = namer.set("base1", Point(-1.0, 0.0))
        val center = namer.set("center", Point(0.02, 0.0))
        val radius = 0.3123
        val (circle, solutionContext) =
            puzzle15_8_solution12E(base1, center, radius, namer)

        dumpSolution(solutionContext, namer)
        assertTrue { puzzle15_8_isSolution(base1, circle).invoke(solutionContext) }
    }

    @Test
    fun puzzle15_8_improve_solution() {
        val namer = Namer()
        val base1 = namer.set("base1", Point(-1.0, 0.0))
        val center = namer.set("center", Point(0.02, 0.0))
        val radius = 0.3123
        val (circle, startingContext) =
            puzzle15_8_initialContext(base1, center, radius, namer)

        val (sampleSolutionCircle, sampleSolutionContext) =
            puzzle15_8_solution12E(base1, center, radius, namer)

        val isSolution = puzzle15_8_isSolution(base1, sampleSolutionCircle)

        val replayNamer = Namer()
        val replayBase1 = replayNamer.set("base1", Point(-1.0, 0.0))
        val replayCenter = namer.set("center", Point(0.01983, 0.0))
        val replayRadius = 0.3078
        val (replayCircle, replayInitialContext) = puzzle15_8_initialContext(
            replayBase1,
            replayCenter,
            replayRadius,
            replayNamer
        )

        val isReplaySolution = puzzle15_8_isSolution(replayBase1, replayCircle)

        fun checkSolution(context: EuclideaContext): Boolean {
            return try {
                val replaySolutionContext =
                    replaySteps(context, replayInitialContext)
                isReplaySolution(replaySolutionContext)
            } catch (e: IllegalStateException) {
                // Failed replay
                false
            }
        }

        assertTrue(isSolution(sampleSolutionContext))

        // maxExtraElements = 4, maxDepth = 11, initialSameElements = 0 - chugging after ~12 hours
        // maxExtraElements = 3, maxDepth = 7, initialSameElements = 0 - chugging after a few hours
        val maxExtraElements = 1
        val initialSameElements = 11
        val solutionContext = solve(startingContext, 12, prune = { next ->
            val extraElements = next.elements.count { !sampleSolutionContext.hasElement(it) }
            extraElements > maxExtraElements || next.elements.size <= initialSameElements && extraElements > 0
        }) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    private fun puzzle15_8_isSolution(
        base1: Point,
        circle: Element.Circle
    ): (EuclideaContext) -> Boolean {
        val solutionLine = Element.Line(base1, circle.center)
        val (solutionPoint1, solutionPoint2) = intersectTwoPoints(solutionLine, circle)
        return { context ->
            context.hasPoint(solutionPoint1) && context.hasPoint(solutionPoint2)
        }
    }

    private fun puzzle15_8_solution12E(
        base1: Point,
        center: Point,
        radius: Double,
        namer: Namer
    ): Pair<Element.Circle, EuclideaContext> {
        val (circle, initialContext) = puzzle15_8_initialContext(
            base1,
            center,
            radius,
            namer
        )
        val start1 = namer.set("start1", EuclideaTools.circleTool(base1, center)!!)
        val start2 = namer.set("start2", EuclideaTools.circleTool(center, base1)!!)
        val triangleP = namer.set("triangleP", intersectAnyPoint(start1, start2))
        val triangleC = namer.set("triangleC", EuclideaTools.circleTool(triangleP, base1)!!)
        val triangleP2 = namer.set("triangleP2", intersectAnyPoint(triangleC, start2))
        val (next1, next2) = namer.setAll("next1", "next2", intersectTwoPoints(start1, circle))
        val nextCircle1 = namer.set("nextCircle1", EuclideaTools.circleTool(next1, base1)!!)
        val nextCircle2 = namer.set("nextCircle2", EuclideaTools.circleTool(next2, base1)!!)
        val eye = namer.set("eye", intersectTwoPointsOther(nextCircle1, nextCircle2, base1))
        val start1Opp = namer.set("start1Opp", EuclideaTools.circleTool(eye, triangleP2)!!)
        val downP = namer.set("downP", intersectAnyPoint(start1Opp, circle))
        val downC = namer.set("downC", EuclideaTools.circleTool(downP, center)!!)
        val (leftP, rightP) = namer.setAll("leftP", "rightP", intersectTwoPoints(downC, circle))
        val rightC = namer.set("rightC", EuclideaTools.circleTool(rightP, downP)!!)
        val cross = namer.set("cross", EuclideaTools.circleTool(rightP, leftP)!!)
        val midP = namer.set("midP", intersectTwoPointsOther(downC, rightC, center))
        val midC = namer.set("midC", EuclideaTools.circleTool(center, midP)!!)
        val midP2 = namer.set("midP2", intersectTwoPointsOther(downC, cross, leftP))
        val midC2 = namer.set("midC2", EuclideaTools.circleTool(midP2, leftP)!!)
        val eye2 = namer.set("eye2", intersectAnyPoint(midC, midC2))
        val cut = namer.set("cut", EuclideaTools.circleTool(downP, eye2)!!)

        namer.setAll("solution1", "solution2", intersectTwoPoints(cut, circle))

        val solutionContext = initialContext.withElements(
            listOf(
                start1, start2, triangleC, nextCircle1, nextCircle2, start1Opp, downC, rightC, cross, midC, midC2, cut
            )
        )
        return Pair(circle, solutionContext)
    }

    private fun puzzle15_8_initialContext(
        base1: Point,
        center: Point,
        radius: Double,
        namer: Namer
    ): Pair<Element.Circle, EuclideaContext> {
        val circle = namer.set("circle", Element.Circle(center, radius))
        val baseContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(100.0)),
            points = listOf(base1, center),
            elements = listOf(circle)
        )
        return Pair(circle, baseContext)
    }

    @Test
    fun puzzle15_8_old() {
        // Line-Circle Intersection*
        // (circles only)
        val center = Point(0.0, 0.0)
        val circle = Element.Circle(center, 1.0)
        val point = Point(-2.542897, 0.0)
        val initialContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = 100.0),
            points = listOf(center, point),
            elements = listOf(circle)
        )
        // Check for either solution point, or a useful waypoint
        val solutionKnots = listOf(-1.0, 1.0, -2.0, 2.0)
        fun checkSolution(a: Double, b: Double): Boolean {
            return coincides(a, 0.0) && solutionKnots.any { coincides(it, b) }
        }
        // 6 - nothing
        // 7 - coincidence? (~3 hours)
        val solutionContext = solve(initialContext, 6) { context ->
            context.points.any { checkSolution(it.y, it.x) }
        }
        dumpSolution(solutionContext)
    }
}
