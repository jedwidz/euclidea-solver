package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EuclideaSetsTest {

    private val d = Epsilon / 10

    private val point = Point(0.0, 0.0)

    private val samePoints = listOf(Point(0.0 - d, 0.0), Point(0.0 + d, 0.0), Point(0.0, 0.0 - d), Point(0.0, 0.0 + d))

    private val differentPoints = listOf(Point(1.0, 0.0), Point(0.0, 1.0))

    @Test
    fun pointSet() {
        euclideaSetTest(
            set = PointSet(),
            item = point,
            sameItems = samePoints,
            differentItems = differentPoints
        )
    }

    private val line = Line(point, Point(1.0, 2.0))

    private val sameLines = listOf(
        Line(Point(1.0, 2.0), point),
        Line(Point(0.0 - d, 0.0), Point(1.0, 2.0)),
        Line(Point(0.0 + d, 0.0), Point(1.0, 2.0)),
        Line(Point(0.0 - d, 0.0 - d), Point(1.0 - d, 2.0 - d)),
        Line(Point(0.0 + d, 0.0 + d), Point(1.0 + d, 2.0 + d)),
        Line(Point(0.0, 0.0 - d), Point(1.0 + d, 2.0 - d))
    )

    private val differentLines = listOf(
        Line(Point(0.0, 1.0), Point(1.0, 2.0)),
        Line(Point(0.0, 1.0), Point(0.0, 2.0)),
        Line(Point(0.0, 1.0), Point(1.0, 0.0)),
        Line(Point(1.0, 0.0), Point(1.0, 2.0))
    )

    @Test
    fun lineSet() {
        euclideaSetTest(
            set = LineSet(),
            item = line,
            sameItems = sameLines,
            differentItems = differentLines
        )
    }

    private val circle = Circle(point, 1.0)

    private val sameCircles = listOf(
        Circle(Point(0.0 - d, 0.0), 1.0),
        Circle(Point(0.0 + d, 0.0), 1.0),
        Circle(Point(0.0 - d, 0.0 - d), 1.0 - d),
        Circle(Point(0.0 + d, 0.0 + d), 1.0 + d)
    )

    private val differentCircles = listOf(
        Circle(point, 2.0),
        Circle(Point(0.0, 1.0), 1.0),
        Circle(Point(1.0, 0.0), 1.0)
    )

    @Test
    fun circleSet() {
        euclideaSetTest(
            set = CircleSet(),
            item = circle,
            sameItems = sameCircles,
            differentItems = differentCircles
        )
    }

    @Test
    fun elementSet_circle() {
        euclideaSetTest(
            set = ElementSet(),
            item = circle,
            sameItems = sameCircles,
            differentItems = listOf(line) + differentLines + differentCircles
        )
    }

    @Test
    fun elementSet_line() {
        euclideaSetTest(
            set = ElementSet(),
            item = line,
            sameItems = sameLines,
            differentItems = listOf(circle) + differentLines + differentCircles
        )
    }

    @Test
    fun primitiveSet_point() {
        euclideaSetTest(
            set = PrimitiveSet(),
            item = point,
            sameItems = samePoints,
            differentItems = listOf(
                line,
                circle
            ) + differentPoints + differentLines + differentCircles
        )
    }

    @Test
    fun primitiveSet_circle() {
        euclideaSetTest(
            set = PrimitiveSet(),
            item = circle,
            sameItems = sameCircles,
            differentItems = listOf(
                point,
                line
            ) + differentPoints + differentLines + differentCircles
        )
    }

    @Test
    fun primitiveSet_line() {
        euclideaSetTest(
            set = PrimitiveSet(),
            item = line,
            sameItems = sameLines,
            differentItems = listOf(
                point,
                circle
            ) + differentPoints + differentLines + differentCircles
        )
    }

    private val lineWithLimit = Line(point, Point(1.0, 2.0), limit1 = true)

    private val sameLinesWithLimit = listOf(
        Line(Point(1.0, 2.0), point, limit2 = true),
        Line(Point(0.0 - d, 0.0), Point(1.0, 2.0), limit1 = true),
        Line(Point(0.0 + d, 0.0), Point(1.0, 2.0), limit1 = true),
        Line(Point(0.0 - d, 0.0 - d), Point(1.0 - d, 2.0 - d), limit1 = true),
        Line(Point(0.0 + d, 0.0 + d), Point(1.0 + d, 2.0 + d), limit1 = true),
        Line(Point(0.0, 0.0 - d), Point(1.0 + d, 2.0 - d), limit1 = true)
    )

    private val differentLinesWithLimit = listOf(
        Line(point, Point(1.0, 2.0)),
        Line(point, Point(1.0, 2.0), limit1 = true, limit2 = true),
        Line(Point(1.0, 2.0), point, limit1 = true),
        Line(Point(0.0, 1.0), Point(1.0, 2.0)),
        Line(Point(0.0, 1.0), Point(0.0, 2.0)),
        Line(Point(0.0, 1.0), Point(1.0, 0.0)),
        Line(Point(1.0, 0.0), Point(1.0, 2.0))
    )

    @Test
    fun lineSetWithLimit() {
        euclideaSetTest(
            set = LineSet(),
            item = lineWithLimit,
            sameItems = sameLinesWithLimit,
            differentItems = differentLinesWithLimit
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
        items.forEach { assertNull(set.canonicalOrNull(it)) }

        set += item

        allSameItems.forEach { assertTrue(it in set) }
        differentItems.forEach { assertTrue(it !in set) }

        set += differentItems
        // TODO test using canonicalOrAdd to add

        items.forEach { assertTrue(it in set) }
        sameItems.forEach { assertEquals(item, set.canonicalOrNull(it)) }
        sameItems.forEach { assertEquals(item, set.canonicalOrAdd(it)) }
        differentItems.forEach { assertEquals(it, set.canonicalOrNull(it)) }
        differentItems.forEach { assertEquals(it, set.canonicalOrAdd(it)) }

        set += sameItems

        println("Removing ${set.items()}")
        set -= listOf(item)
        println("Removed ${set.items()}")
        allSameItems.forEach { assertFalse(it in set) }
        set.items().none { allSameItems.contains(it) }
    }

    @Test
    fun lineSet_largeInterceptBugFix() {
        lineSet_largeInterceptBugFix_impl(
            Line(
                point1 = Point(x = 0.2740876489451589, y = -0.612271149651619),
                point2 = Point(x = 0.9449083964302225, y = -0.6122709910755564)
            )
        )
        lineSet_largeInterceptBugFix_impl(
            Line(
                point1 = Point(x = -0.2740876489451589, y = -0.612271149651619),
                point2 = Point(x = -0.9449083964302225, y = -0.6122709910755564)
            )
        )
    }

    private fun lineSet_largeInterceptBugFix_impl(line: Line) {
        val set = LineSet()
        set.add(line)
        assertEquals(1, set.items().size)
        val removed = set.remove(line)
        assertTrue(removed)
        assertEquals(0, set.items().size)
    }
}
