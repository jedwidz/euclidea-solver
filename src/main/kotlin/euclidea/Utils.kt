package euclidea

fun <E> List<E>.forEachPair(action: (E, E) -> Unit) {
    val count = count()
    forEachIndexed { i, a ->
        subList(i + 1, count).forEach { b -> action(a, b) }
    }
}

fun <E> List<E>.pairs(): List<Pair<E, E>> {
    val res = mutableListOf<Pair<E, E>>()
    forEachPair { a, b -> res.add(a to b) }
    return res
}
