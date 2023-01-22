package euclidea

import kotlin.test.assertTrue

abstract class ImprovingSolver<Params : Any, Setup> {

    fun checkReferenceSolution() {
        allReferenceSolutions().map(this::checkImpl)
    }

    private fun allReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
        return listOf(this::referenceSolution) + additionalReferenceSolutions()
    }

    private fun checkImpl(solution: (Params, Namer) -> Pair<Setup, EuclideaContext?>) {
        fun check(params: Params, replayParams: Params, dump: Boolean) {
            val namer = Namer()
            nameParams(params, namer)
            val (setup, solutionContext) =
                solution(params, namer)

            if (dump) dumpSolution(solutionContext, namer)
            if (solutionContext != null) {
                assertTrue { isSolution(params, setup).invoke(solutionContext) }

                val replayNamer = Namer()
                val (replaySetup, replayInitialContext) = initialContext(replayParams, replayNamer)
                val replaySolutionContext =
                    replaySteps(solutionContext, replayInitialContext)

                assertTrue { isSolution(replayParams, replaySetup).invoke(replaySolutionContext) }
            }
        }
        check(makeParams(), makeReplayParams(), true)
        check(makeReplayParams(), makeParams(), false)
    }

    fun improveSolution(
        maxExtraElements: Int,
        maxDepth: Int,
        nonNewElementLimit: Int? = null,
        consecutiveNonNewElementLimit: Int? = null,
        useTargetConstruction: Boolean = false
    ) {
        val namer = Namer()
        val params = makeParams()
        nameParams(params, namer)
        val (setup, startingContext) =
            initialContext(params, namer)
        val initialElementCount = startingContext.elements.size

        val sampleSolutionContexts = allReferenceSolutions().mapNotNull { it(params, namer).second }

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

        assertTrue(sampleSolutionContexts.all { isSolution(it) })

        val prefixNamer = Namer()
        val prefixContext = solutionPrefix(params, prefixNamer)?.second

        fun checkPrefix(index: Int, element: Element): Boolean {
            if (prefixContext === null)
                return true
            return prefixContext.elements.getOrNull(initialElementCount + index)?.let { prefixElement ->
                coincides(element, prefixElement)
            } ?: true
        }

        val targetElementSet = ElementSet()
        fun accumTargetElements(context: EuclideaContext) {
            targetElementSet += context.elements
            if (useTargetConstruction)
                targetElementSet += context.constructionElementSet()
        }
        accumTargetElements(startingContext)
        sampleSolutionContexts.forEach { accumTargetElements(it) }
        prefixContext?.let { accumTargetElements(it) }

        val passWithPrefix: ((SolveContext, Element) -> Boolean) = { solveContext, element ->
            !checkPrefix(solveContext.depth, element) || (pass !== null && pass(solveContext, element))
        }

        fun checkExtraElements(nextElements: List<Element>): Boolean {
            val extraElements = nextElements.count { it !in targetElementSet }
            return extraElements <= maxExtraElements
        }

        val solutionContext = solve(
            startingContext,
            maxDepth = maxDepth,
            nonNewElementLimit = nonNewElementLimit,
            consecutiveNonNewElementLimit = consecutiveNonNewElementLimit,
            prune = { nextSolveContext ->
                val nextElements = nextSolveContext.context.elements
                !checkExtraElements(nextElements)
            },
            visitPriority = visitPriority,
            pass = passWithPrefix,
            remainingStepsLowerBound = remainingStepsLowerBound,
            excludeElements = excludeElements(params, setup),
            toolSequence = toolSequence()
        ) { context ->
            isSolution(context) && checkSolution(context)
        }
        dumpSolution(solutionContext, namer)
        println("Count: ${solutionContext?.elements?.size}")
    }

    private fun nameParams(params: Params, namer: Namer) {
        namer.nameReflected(params)
    }

    protected abstract fun makeParams(): Params

    protected abstract fun makeReplayParams(): Params

    protected abstract fun isSolution(
        params: Params,
        setup: Setup
    ): (EuclideaContext) -> Boolean

    protected open fun excludeElements(
        params: Params,
        setup: Setup
    ): ElementSet? {
        return null
    }

    protected open fun visitPriority(
        params: Params,
        setup: Setup
    ): ((SolveContext, Element) -> Int)? {
        return null
    }

    protected open fun toolSequence(): List<EuclideaTool>? {
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

    protected open fun referenceSolution(
        params: Params,
        namer: Namer
    ): Pair<Setup, EuclideaContext?> {
        // No solution
        val (setup, _) = initialContext(params, namer)
        return setup to null
    }

    protected open fun additionalReferenceSolutions(): List<(Params, Namer) -> Pair<Setup, EuclideaContext?>> {
        // No solutions
        return listOf()
    }

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