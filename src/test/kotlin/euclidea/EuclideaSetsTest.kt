package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EuclideaSetsTest {

    private val d = Epsilon / 10

    @Test
    fun pointSet() {
        euclideaSetTest(
            set = PointSet(),
            item = Point(0.0, 0.0),
            sameItems = listOf(Point(0.0 - d, 0.0), Point(0.0 + d, 0.0), Point(0.0, 0.0 - d), Point(0.0, 0.0 + d)),
            differentItems = listOf(Point(1.0, 0.0), Point(0.0, 1.0))
        )
    }

    @Test
    fun lineSet() {
        euclideaSetTest(
            set = LineSet(),
            item = Line(Point(0.0, 0.0), Point(1.0, 2.0)),
            sameItems = listOf(
                Line(Point(0.0 - d, 0.0), Point(1.0, 2.0)),
                Line(Point(0.0 + d, 0.0), Point(1.0, 2.0)),
                Line(Point(0.0 - d, 0.0 - d), Point(1.0 - d, 2.0 - d)),
                Line(Point(0.0 + d, 0.0 + d), Point(1.0 + d, 2.0 + d)),
                Line(Point(0.0, 0.0 - d), Point(1.0 + d, 2.0 - d))
            ),
            differentItems = listOf(
                Line(Point(0.0, 1.0), Point(1.0, 2.0)),
                Line(Point(0.0, 1.0), Point(0.0, 2.0)),
                Line(Point(0.0, 1.0), Point(1.0, 0.0)),
                Line(Point(1.0, 0.0), Point(1.0, 2.0))
            )
        )
    }

    @Test
    fun circleSet() {
        euclideaSetTest(
            set = CircleSet(),
            item = Circle(Point(0.0, 0.0), 1.0),
            sameItems = listOf(
                Circle(Point(0.0 - d, 0.0), 1.0),
                Circle(Point(0.0 + d, 0.0), 1.0),
                Circle(Point(0.0 - d, 0.0 - d), 1.0 - d),
                Circle(Point(0.0 + d, 0.0 + d), 1.0 + d)
            ),
            differentItems = listOf(
                Circle(Point(0.0, 0.0), 2.0),
                Circle(Point(0.0, 1.0), 1.0),
                Circle(Point(1.0, 0.0), 1.0)
            )
        )
    }

    private fun <T, S : EuclideaSet<T>> euclideaSetTest(
        set: S,
        item: T,
        sameItems: List<T>,
        differentItems: List<T>
    ) {
        val allSameItems = sameItems + item
        val items = allSameItems + differentItems

        items.forEach { assertTrue(it !in set) }

        set += item

        allSameItems.forEach { assertTrue(it in set) }
        differentItems.forEach { assertTrue(it !in set) }

        set += differentItems

        items.forEach { assertTrue(it in set) }
    }

}
