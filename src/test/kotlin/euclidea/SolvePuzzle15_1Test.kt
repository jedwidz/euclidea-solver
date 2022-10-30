package euclidea

import euclidea.EuclideaTools.circleTool
import org.junit.jupiter.api.Test
import solve
import kotlin.test.assertTrue

class SolvePuzzle15_1Test {

    @Test
    fun puzzle15_1_check_solution() {
        // Midpoint**
        // (circles only)
        // Reproduce and check my best solution so far
        val namer = Namer()
        val basePoint1 = namer.set("base1", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val solutionContext =
            puzzle15_1_solution7E(basePoint1, basePoint2, namer)

        dumpSolution(solutionContext, namer)
        assertTrue { puzzle15_1_isSolution(basePoint1, basePoint2).invoke(solutionContext) }
    }

    @Test
    fun puzzle15_1_improve_solution() {
        // Try to improve on my best solution so far
        val namer = Namer()
        val basePoint1 = namer.set("base1", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val startingContext =
            puzzle15_1_initialContext(basePoint1, basePoint2, namer)

        val sampleSolutionContext =
            puzzle15_1_solution7E(basePoint1, basePoint2, namer)

        val isSolution = puzzle15_1_isSolution(basePoint1, basePoint2)

        val replayNamer = Namer()
        val replayBasePoint1 = replayNamer.set("base1", Point(0.01, 0.0))
        val replayBasePoint2 = replayNamer.set("base2", Point(1.0, 0.1))
        val replayInitialContext = puzzle15_1_initialContext(
            replayBasePoint1,
            replayBasePoint2,
            replayNamer
        )

        val isReplaySolution = puzzle15_1_isSolution(replayBasePoint1, replayBasePoint2)

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
        val solutionContext = solve(startingContext, 7, prune = { next ->
            next.elements.count { !sampleSolutionContext.hasElement(it) } > maxExtraElements
        }) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    fun puzzle15_1_isSolution(basePoint1: Point, basePoint2: Point): (EuclideaContext) -> Boolean {
        val checkSolutionPoint = midpoint(basePoint1, basePoint2)
        return { context ->
            context.hasPoint(checkSolutionPoint)
        }
    }

    private fun puzzle15_1_solution7E(
        basePoint1: Point,
        basePoint2: Point,
        namer: Namer
    ): EuclideaContext {
        val initialContext = puzzle15_1_initialContext(
            basePoint1,
            basePoint2,
            namer
        )
        val start1 = namer.set("start1", circleTool(basePoint1, basePoint2)!!)
        val start2 = namer.set("start2", circleTool(basePoint2, basePoint1)!!)
        val start = namer.set("start", intersectAnyPoint(start1, start2))
        val top = namer.set("top", circleTool(start, basePoint1)!!)
        val next = namer.set("next", intersectTwoPointsOther(start2, top, basePoint1))
        val side = namer.set("side", circleTool(next, basePoint2)!!)
        val two = namer.set("two", intersectTwoPointsOther(start2, side, start))
        val big = namer.set("big", circleTool(two, basePoint1)!!)
        val (x1, x2) = namer.setAll("x1", "x2", intersectTwoPoints(big, start1))
        val target1 = namer.set("target1", circleTool(x1, basePoint1)!!)
        val target2 = namer.set("target2", circleTool(x2, basePoint1)!!)
        namer.set("solution", intersectTwoPointsOther(target1, target2, basePoint1))

        val solutionContext = initialContext.withElements(
            listOf(
                start1, start2, top, side, big, target1, target2
            )
        )
        return solutionContext
    }

    private fun puzzle15_1_initialContext(
        basePoint1: Point,
        basePoint2: Point,
        namer: Namer
    ): EuclideaContext {
        val baseContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(100.0)),
            points = listOf(basePoint1, basePoint2),
            elements = listOf()
        )
        return baseContext
    }
}
