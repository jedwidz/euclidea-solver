package euclidea

fun <E> forEachPair(list: List<E>, action: (E, E) -> Unit) {
    val count = list.count()
    list.forEachIndexed { i, a ->
        list.subList(i + 1, count).forEach { b -> action(a, b) }
    }
}