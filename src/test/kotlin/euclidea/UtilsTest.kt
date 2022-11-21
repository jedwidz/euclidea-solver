package euclidea

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun forEachPair_basic() {
        forEachPair_impl(listOf(1, 2, 3), listOf(12, 13, 23))
    }

    @Test
    fun forEachPair_pair() {
        forEachPair_impl(listOf(1, 2), listOf(12))
    }

    @Test
    fun forEachPair_single() {
        forEachPair_impl(listOf(1), listOf())
    }

    @Test
    fun forEachPair_empty() {
        forEachPair_impl(listOf(), listOf())
    }

    private fun forEachPair_impl(list: List<Int>, expected: List<Int>) {
        val res = mutableListOf<Int>()
        list.forEachPair { a, b -> res.add(a * 10 + b) }
        assertEquals(expected, res.toList())
    }

    @Suppress("unused")
    private val obj = object {
        val intA = 1
        val stringZ = "stringZ value"
        val stringA = "stringA value"
        val intZ = 100
    }

    @Test
    fun reflectProperties_value() {
        assertEquals(
            mapOf("stringZ" to "stringZ value", "stringA" to "stringA value"),
            obj.reflectProperties(String::class)
        )
        assertEquals(mapOf("intA" to 1, "intZ" to 100), obj.reflectProperties(Int::class))
        assertEquals(mapOf(), obj.reflectProperties(Double::class))
    }

    @Test
    fun reflectProperties_order() {
        assertEquals(
            listOf("stringZ", "stringA"),
            obj.reflectProperties(String::class).keys.toList()
        )
        assertEquals(
            listOf("intA", "intZ"),
            obj.reflectProperties(Int::class).keys.toList()
        )
        assertEquals(
            listOf(),
            obj.reflectProperties(Double::class).keys.toList()
        )
    }
}