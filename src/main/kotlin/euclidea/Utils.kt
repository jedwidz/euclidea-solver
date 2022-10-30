package euclidea

fun <E> List<E>.forEachPair(action: (E, E) -> Unit) {
    val count = count()
    forEachIndexed { i, a ->
        subList(i + 1, count).forEach { b -> action(a, b) }
    }
}

typealias Two<T> = Pair<T, T>

fun <E> List<E>.pairs(): List<Two<E>> {
    val res = mutableListOf<Two<E>>()
    forEachPair { a, b -> res.add(a to b) }
    return res
}

fun <E> List<E>.forEachTriple(action: (E, E, E) -> Unit) {
    val count = count()
    forEachIndexed { i, a ->
        subList(i + 1, count).forEachIndexed { j, b ->
            subList(i + j + 2, count).forEach { c -> action(a, b, c) }
        }
    }
}

typealias Three<T> = Triple<T, T, T>

fun <E> List<E>.triples(): List<Three<E>> {
    val res = mutableListOf<Three<E>>()
    forEachTriple { a, b, c -> res.add(Triple(a, b, c)) }
    return res
}

fun <T> threeFrom(list: List<T>): Three<T> {
    require(list.size == 3)
    return Triple(list[0], list[1], list[2])
}
