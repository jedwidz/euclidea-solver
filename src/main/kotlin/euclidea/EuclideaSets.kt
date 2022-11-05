package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line

interface EuclideaSet<T> {
    operator fun contains(item: T): Boolean
    operator fun plusAssign(item: T)
    fun add(item: T): Boolean
    operator fun plusAssign(items: Collection<T>)
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
        val subSet = set.subSet(range.first, false, range.second, false)
        return subSet.any { coincides(it, item) }
    }

    private fun coincidingRange(primary: Double): Pair<T, T> {
        return bound(primary - Epsilon) to bound(primary + Epsilon)
    }

    override operator fun plusAssign(item: T) {
        add(item)
    }

    override fun add(item: T): Boolean {
        return if (contains(item)) false else {
            val added = set.add(item)
            assert(added)
            added
        }
    }

    override operator fun plusAssign(items: Collection<T>) {
        items.forEach { add(it) }
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

