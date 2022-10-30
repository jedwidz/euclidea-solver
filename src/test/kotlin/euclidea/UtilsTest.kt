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

}