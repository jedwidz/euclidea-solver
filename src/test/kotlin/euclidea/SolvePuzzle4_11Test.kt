package euclidea

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SolvePuzzle4_11Test {

    @Test
    fun puzzle4_11_check_solution() {
        // Square by Two Vertices
        // (circles only)
        // Reproduce and check my best solution so far
        val namer = Namer()
        val base1 = namer.set("base1", Point(0.0, 0.0))
        val base2 = namer.set("base2", Point(1.0, 0.0))
        val solutionContext =
            puzzle4_11_solution8E(base1, base2, namer)

        dumpSolution(solutionContext, namer)
        assertTrue { puzzle4_11_isSolution(base1, base2).invoke(solutionContext) }
    }

    @Test
    fun puzzle4_11_improve_solution() {
        val namer = Namer()
        val base1 = namer.set("base1", Point(0.0, 0.0))
        val base2 = namer.set("base2", Point(1.0, 0.0))
        val startingContext =
            puzzle4_11_initialContext(base1, base2, namer)

        val sampleSolutionContext =
            puzzle4_11_solution8E(base1, base2, namer)

        val isSolution = puzzle4_11_isSolution(base1, base2)

        val replayNamer = Namer()
        val replayBase1 = replayNamer.set("base1", Point(0.0, 0.0))
        val replayBase2 = namer.set("base2", Point(1.01983, 0.0011))
        val replayInitialContext = puzzle4_11_initialContext(
            replayBase1,
            replayBase2,
            replayNamer
        )

        val isReplaySolution = puzzle4_11_isSolution(replayBase1, replayBase2)

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

        val maxExtraElements = 2
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

    private fun puzzle4_11_isSolution(
        base1: Point,
        base2: Point
    ): (EuclideaContext) -> Boolean {
        val diff = base2 - base1
        // Arbitrary choice of one of two axis-aligned squares (the 'diagonal square' seems unlikely to be optimal)
        val solution1 = Point(base1.x + diff.y, base1.y - diff.x)
        val solution2 = solution1.plus(diff)
        return { context ->
            context.hasPoint(solution1) && context.hasPoint(solution2)
        }
    }

    private fun puzzle4_11_solution8E(
        base1: Point,
        base2: Point,
        namer: Namer
    ): EuclideaContext {
        val initialContext = puzzle4_11_initialContext(
            base1,
            base2,
            namer
        )
        val start1 = namer.set("start1", EuclideaTools.circleTool(base1, base2)!!)
        val start2 = namer.set("start2", EuclideaTools.circleTool(base2, base1)!!)
        val (adj2, adj1) = namer.setAll("adj2", "adj1", intersectTwoPoints(start1, start2))
        val big = namer.set("big", EuclideaTools.circleTool(adj1, adj2)!!)
        val triangle = namer.set("triangle", EuclideaTools.circleTool(adj1, base1)!!)
        val two = namer.set("two", intersectTwoPointsOther(start2, big, adj2))
        val bigAtTwo = namer.set("bigAtTwo", EuclideaTools.circleTool(two, adj1)!!)
        val big1P = namer.set("big1P", intersectTwoPointsOther(triangle, start2, base1))
        val big1 = namer.set("big1", EuclideaTools.circleTool(base1, big1P)!!)
        val focus = namer.set("focus", intersectAnyPoint(bigAtTwo, big1))
        val focusC = namer.set("focusC", EuclideaTools.circleTool(base2, focus)!!)
        val finalP = namer.set("finalP", intersectAnyPoint(start1, focusC))
        val finalC = namer.set("finalC", EuclideaTools.circleTool(adj2, finalP)!!)

        // namer.setAll("solution1", "solution2", intersectTwoPoints(finalC, circle))

        val solutionContext = initialContext.withElements(
            listOf(
                start1, start2, big, triangle, bigAtTwo, big1, focusC, finalC
            )
        )
        return solutionContext
    }

    private fun puzzle4_11_initialContext(
        base1: Point,
        base2: Point,
        namer: Namer
    ): EuclideaContext {
        val baseContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false, maxSqDistance = sq(5.0)),
            points = listOf(base1, base2),
            elements = listOf()
        )
        return baseContext
    }
}
