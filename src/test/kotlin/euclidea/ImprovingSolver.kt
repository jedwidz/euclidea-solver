package euclidea

import kotlin.test.assertTrue

abstract class ImprovingSolver<Params, Setup> {

    fun checkReferenceSolution() {
        checkImpl(this::referenceSolution)
    }

    private fun checkImpl(solution: (Params, Namer) -> Pair<Setup, EuclideaContext>) {
        fun check(params: Params, dump: Boolean) {
            val namer = Namer()
            nameParams(params, namer)
            val (setup, solutionContext) =
                solution(params, namer)

            if (dump) dumpSolution(solutionContext, namer)
            assertTrue { isSolution(params, setup).invoke(solutionContext) }
        }
        check(makeParams(), true)
        check(makeReplayParams(), false)
    }

    fun improveSolution(maxExtraElements: Int, maxDepth: Int) {
        val namer = Namer()
        val params = makeParams()
        nameParams(params, namer)
        val (setup, startingContext) =
            initialContext(params, namer)

        val (sampleSolutionSetup, sampleSolutionContext) =
            referenceSolution(params, namer)

        val isSolution = isSolution(params, setup)
        val visitPriority = visitPriority(params, setup)
        val pass = pass(params, setup)
        val remainingStepsLowerBound = remainingStepsLowerBound(params, setup)

        val replayNamer = Namer()
        val replayParams = makeReplayParams()
        nameParams(replayParams, replayNamer)
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

        val prefixNamer = Namer()
        val prefixContext = solutionPrefix(params, prefixNamer)?.second

        fun checkPrefix(index: Int, element: Element): Boolean {
            if (prefixContext === null)
                return true
            return prefixContext.elements.getOrNull(index)?.let { prefixElement ->
                coincides(element, prefixElement)
            } ?: true
        }

        val targetElementSet = ElementSet()
        targetElementSet += sampleSolutionContext.elements
        prefixContext?.let { targetElementSet += it.elements }

        val passWithPrefix: ((SolveContext, Element) -> Boolean) = { solveContext, element ->
            !checkPrefix(solveContext.depth, element) || (pass !== null && pass(solveContext, element))
        }

        fun checkExtraElements(nextElements: List<Element>): Boolean {
            val extraElements = nextElements.count { it !in targetElementSet }
            return extraElements <= maxExtraElements
        }

        val solutionContext = solve(
            startingContext,
            maxDepth,
            prune = { nextSolveContext ->
                val nextElements = nextSolveContext.context.elements
                !checkExtraElements(nextElements)
            },
            visitPriority = visitPriority,
            pass = passWithPrefix,
            remainingStepsLowerBound = remainingStepsLowerBound
        ) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    protected abstract fun nameParams(params: Params, namer: Namer)

    protected abstract fun makeParams(): Params

    protected abstract fun makeReplayParams(): Params

    protected abstract fun isSolution(
        params: Params,
        setup: Setup
    ): (EuclideaContext) -> Boolean

    protected open fun visitPriority(
        params: Params,
        setup: Setup
    ): ((SolveContext, Element) -> Int)? {
        return null
    }

    protected open fun pass(
        params: Params,
        setup: Setup
    ): ((SolveContext, Element) -> Boolean)? {
        return null
    }

    protected open fun remainingStepsLowerBound(
        params: Params,
        setup: Setup
    ): ((EuclideaContext) -> Int)? {
        return null
    }

    protected abstract fun referenceSolution(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext>

    protected abstract fun initialContext(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext>

    protected open fun solutionPrefix(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext>? {
        return null
    }
}