import euclidea.EuclideaContext

fun solve(
    initialContext: EuclideaContext,
    maxDepth: Int,
    prune: ((EuclideaContext) -> Boolean)? = null,
    check: (EuclideaContext) -> Boolean
): EuclideaContext? {
    fun sub(context: EuclideaContext, depth: Int): EuclideaContext? {
        val nextDepth = depth + 1
        for (next in context.nexts())
            if (check(next))
                return next
            else if (nextDepth < maxDepth && (prune == null || !prune(next)))
                sub(next, nextDepth)?.let { return@sub it }
        return null
    }
    if (check(initialContext))
        return initialContext
    return sub(initialContext, 0)
}

