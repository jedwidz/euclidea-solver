package euclidea

import euclidea.EuclideaTools.circleTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle15_3Test {

    @Test
    fun puzzle15_3_check_solution() {
        // Line-Circle Intersection
        // (circles only)
        // Reproduce and check my best solution so far
        val namer = Namer()
        val base1 = namer.set("base1", Point(-1.0, 0.0))
        val base2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.02, -0.1234))
        val radius = 0.3123
        val (circle, solutionContext) =
            puzzle15_3_solution4E(base1, base2, center, radius, namer)

        dumpSolution(solutionContext, namer)
        assertTrue { puzzle15_3_isSolution(base1, base2, circle).invoke(solutionContext) }
    }

    @Test
    fun puzzle15_3_improve_solution() {
        val namer = Namer()
        val base1 = namer.set("base1", Point(-1.0, 0.0))
        val base2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.02, -0.1234))
        val radius = 0.3123
        val (circle, startingContext) =
            puzzle15_3_initialContext(base1, base2, center, radius, namer)

        val (sampleSolutionCircle, sampleSolutionContext) =
            puzzle15_3_solution4E(base1, base2, center, radius, namer)

        val isSolution = puzzle15_3_isSolution(base1, base2, sampleSolutionCircle)

        val replayNamer = Namer()
        val replayBase1 = replayNamer.set("base1", Point(-1.0, 0.0))
        val replayBase2 = replayNamer.set("base2", Point(1.0, 0.0))
        val replayCenter = namer.set("center", Point(0.01983, -0.1258))
        val replayRadius = 0.3078
        val (replayCircle, replayInitialContext) = puzzle15_3_initialContext(
            replayBase1,
            replayBase2,
            replayCenter,
            replayRadius,
            replayNamer
        )

        val isReplaySolution = puzzle15_3_isSolution(replayBase1, replayBase2, replayCircle)

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

        val maxExtraElements = 0
        val solutionContext = solve(startingContext, 4, prune = { next ->
            next.elements.count { !sampleSolutionContext.hasElement(it) } > maxExtraElements
        }) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    private fun puzzle15_3_isSolution(
        base1: Point,
        base2: Point,
        circle: Element.Circle
    ): (EuclideaContext) -> Boolean {
        val solutionLine = Element.Line(base1, base2)
        val (solutionPoint1, solutionPoint2) = intersectTwoPoints(solutionLine, circle)
        return { context ->
            context.hasPoint(solutionPoint1) && context.hasPoint(solutionPoint2)
        }
    }

    private fun puzzle15_3_solution4E(
        base1: Point,
        base2: Point,
        center: Point,
        radius: Double,
        namer: Namer
    ): Pair<Element.Circle, EuclideaContext> {
        val (circle, initialContext) = puzzle15_3_initialContext(
            base1,
            base2,
            center,
            radius,
            namer
        )
        val start1 = namer.set("start1", circleTool(base1, center)!!)
        val start2 = namer.set("start2", circleTool(base2, center)!!)
        val upper = namer.set("upper", intersectAnyPoint(start1, circle))
        val tweak = namer.set("tweak", circleTool(base2, upper)!!)
        val lower = namer.set("lower", intersectTwoPointsOther(start1, tweak, upper))
        val up = namer.set("up", intersectTwoPointsOther(start1, start2, center))
        val dupe = namer.set("dupe", circleTool(up, lower)!!)
        namer.setAll("solution1", "solution2", intersectTwoPoints(dupe, circle))

        val solutionContext = initialContext.withElements(
            listOf(
                start1, start2, tweak, dupe
            )
        )
        return Pair(circle, solutionContext)
    }

    private fun puzzle15_3_initialContext(
        base1: Point,
        base2: Point,
        center: Point,
        radius: Double,
        namer: Namer
    ): Pair<Element.Circle, EuclideaContext> {
        val circle = namer.set("circle", Element.Circle(center, radius))
        val baseContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(100.0)),
            points = listOf(base1, base2, center),
            elements = listOf(circle)
        )
        return Pair(circle, baseContext)
    }
}
