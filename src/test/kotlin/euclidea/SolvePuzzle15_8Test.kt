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
            puzzle15_8_solution7E(base1, center, radius, namer)

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
            puzzle15_8_solution7E(base1, center, radius, namer)

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

        // maxExtraElements = 4, maxDepth = 11, initialSameElements = 0 - chugging after ~12 hours (old)
        // maxExtraElements = 3, maxDepth = 7, initialSameElements = 0 - nothing after ~2.5 hours
        // maxExtraElements = 2, maxDepth = 7, initialSameElements = 0 - nothing after ~4 mins
        // maxExtraElements = 4, maxDepth = 7, initialSameElements = 0 - nothing after 25h 31m 11s
        // maxExtraElements = 5, maxDepth = 7, initialSameElements = 0 - found 2h 31m
        val maxExtraElements = 1
        val initialSameElements = 0
        val solutionContext = solve(startingContext, 7, prune = { next ->
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

    private fun puzzle15_8_solution7E(
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
        val start = namer.set("start", EuclideaTools.circleTool(base1, center)!!)
        val (adj1, adj2) = namer.set("shiftP", intersectTwoPoints(start, circle))
        val shift = namer.set("shift", EuclideaTools.circleTool(adj1, center)!!)
        val span = namer.set("span", EuclideaTools.circleTool(adj1, adj2)!!)
        val opp = namer.set("opp", intersectTwoPointsOther(shift, start, center))
        val eye = namer.set("eye", EuclideaTools.circleTool(center, opp)!!)
        val perpP = namer.set("perpP", intersectTwoPointsOther(eye, shift, opp))
        val perpC = namer.set("perpC", EuclideaTools.circleTool(perpP, adj1)!!)
        val focus = namer.set("focus", intersectAnyPoint(shift, perpC))
        val bigP = namer.set("bigP", intersectTwoPointsOther(eye, start, opp))
        val bigC = namer.set("bigC", EuclideaTools.circleTool(bigP, focus)!!)
        val finalP = namer.set("finalP", intersectAnyPoint(bigC, span))
        val finalC = namer.set("finalC", EuclideaTools.circleTool(perpP, finalP)!!)

        namer.setAll("solution1", "solution2", intersectTwoPoints(finalC, circle))

        val solutionContext = initialContext.withElements(
            listOf(
                start, shift, span, eye, perpC, bigC, finalC
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
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(4.0)),
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
