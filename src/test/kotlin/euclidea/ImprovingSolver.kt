package euclidea

import kotlin.test.assertTrue

abstract class ImprovingSolver<Params, Setup> {

    fun checkReferenceSolution() {
        val namer = Namer()
        val params = makeParams(namer)
        val (setup, solutionContext) =
            referenceSolution(params, namer)

        dumpSolution(solutionContext, namer)
        assertTrue { isSolution(params, setup).invoke(solutionContext) }
    }

    fun improveSolution(maxExtraElements: Int, maxDepth: Int) {
        val namer = Namer()
        val params = makeParams(namer)
        val (setup, startingContext) =
            initialContext(params, namer)

        val (sampleSolutionSetup, sampleSolutionContext) =
            referenceSolution(params, namer)

        val isSolution = isSolution(params, setup)

        val replayNamer = Namer()
        val replayParams = makeReplayParams(replayNamer)
        val (replaySetup, replayInitialContext) = initialContext(
            replayParams,
            replayNamer
        )

        val isReplaySolution = isSolution(replayParams, replaySetup)

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

        val solutionContext = solve(startingContext, maxDepth, prune = { next ->
            val extraElements = next.elements.count { !sampleSolutionContext.hasElement(it) }
            extraElements > maxExtraElements
        }) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    protected abstract fun makeReplayParams(namer: Namer): Params

    protected abstract fun makeParams(namer: Namer): Params

    protected abstract fun isSolution(
        params: Params,
        setup: Setup
    ): (EuclideaContext) -> Boolean

    protected abstract fun referenceSolution(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext>

    protected abstract fun initialContext(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext>
}