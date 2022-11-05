package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line

interface EuclideaSet<T> {
    operator fun contains(item: T): Boolean
    fun removeOne(): T?

    fun add(item: T): Boolean
    fun remove(item: T): Boolean

    operator fun plusAssign(item: T) {
        add(item)
    }

    operator fun plusAssign(items: Collection<T>) {
        items.forEach { add(it) }
    }

    operator fun minusAssign(items: Collection<T>) {
        items.forEach { remove(it) }
    }
}

abstract class IndexedSet<T>(
    // must compare on primaryDim first, and have equals consist with T::equals
    comparator: Comparator<in T>
) : EuclideaSet<T> {
    protected abstract fun coincides(item1: T, item2: T): Boolean
    protected abstract fun bound(d: Double): T
    protected abstract fun primaryDim(item: T): Double

    private val set = sortedSetOf(comparator)

    override operator fun contains(item: T): Boolean {
        val primary = primaryDim(item)
        val range = coincidingRange(primary)
        // Take some liberty here at the edge points of the range
        val subSet = set.subSet(range.first, false, range.second, false)
        return subSet.any { coincides(it, item) }
    }

    private fun coincidingRange(primary: Double): Pair<T, T> {
        return bound(primary - Epsilon) to bound(primary + Epsilon)
    }

    override fun add(item: T): Boolean {
        return if (contains(item)) false else {
            val added = set.add(item)
            assert(added)
            added
        }
    }

    override fun remove(item: T): Boolean {
        return if (!contains(item)) false else {
            val removed = set.remove(item)
            assert(removed)
            removed
        }
    }

    override fun removeOne(): T? {
        val res = set.firstOrNull()
        res?.let { set.remove(it) }
        return res
    }
}

class PointSet : IndexedSet<Point>(compareBy({ it.x }, { it.y })) {

    override fun primaryDim(item: Point): Double {
        return item.x
    }

    override fun coincides(item1: Point, item2: Point): Boolean {
        return euclidea.coincides(item1, item2)
    }

    override fun bound(d: Double): Point {
        return Point(d, 0.0)
    }
}

private fun linePrimaryDim(line: Line) = line.xIntercept ?: line.yIntercept ?: 0.0

class LineSet : IndexedSet<Line>(compareBy({ linePrimaryDim(it) }, { it.yIntercept }, { it.xIntercept })) {

    override fun primaryDim(item: Line): Double {
        return linePrimaryDim(item)
    }

    override fun coincides(item1: Line, item2: Line): Boolean {
        return euclidea.coincides(item1, item2)
    }

    override fun bound(d: Double): Line {
        return Line(Point(d, 0.0), Point(d, 1.0))
    }
}

class CircleSet : IndexedSet<Circle>(compareBy({ it.center.x }, { it.center.y }, { it.radius })) {

    override fun primaryDim(item: Circle): Double {
        return item.center.x
    }

    override fun coincides(item1: Circle, item2: Circle): Boolean {
        return euclidea.coincides(item1, item2)
    }

    override fun bound(d: Double): Circle {
        return Circle(Point(d, 0.0), 0.0)
    }
}

class ElementSet : EuclideaSet<Element> {

    val lineSet = LineSet()
    val circleSet = CircleSet()

    override fun contains(item: Element): Boolean {
        return when (item) {
            is Line -> lineSet.contains(item)
            is Circle -> circleSet.contains(item)
        }
    }

    override fun add(item: Element): Boolean {
        return when (item) {
            is Line -> lineSet.add(item)
            is Circle -> circleSet.add(item)
        }
    }

    override fun remove(item: Element): Boolean {
        return when (item) {
            is Line -> lineSet.remove(item)
            is Circle -> circleSet.remove(item)
        }
    }

    override fun removeOne(): Element? {
        return lineSet.removeOne() ?: circleSet.removeOne()
    }
}

