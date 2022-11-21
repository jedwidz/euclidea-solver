package euclidea

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

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

fun <T : Any> Any.reflectProperties(kClass: KClass<T>): Map<String, T> {
    val unordered = this::class.declaredMemberProperties.mapNotNull { property ->
        if (property.parameters.size == 1 && property.returnType.isSubtypeOf(kClass.starProjectedType)) {
            @Suppress("UNCHECKED_CAST")
            val typedProperty = property as KProperty1<Any, T>
            val primitive = typedProperty.get(this)
            val name = property.name
            name to primitive
        } else null
    }.toMap()
    // Maybe not guaranteed to give the declaration order, but fingers crossed
    // See: https://stackoverflow.com/a/5004929/2396171
    val order = this::class.java.declaredFields.map { it.name }
    return order.mapNotNull { name -> unordered[name]?.let { name to it } }.toMap()
}
