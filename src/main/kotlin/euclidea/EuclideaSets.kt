package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line

interface EuclideaSet<T> {
    operator fun contains(item: T): Boolean
    fun removeOne(): T?

    fun add(item: T): Boolean
    fun remove(item: T): Boolean

    fun items(): List<T>

    fun <U : T> canonicalOrNull(item: U): U?
    fun <U : T> canonicalOrAdd(item: U): U

    operator fun plusAssign(item: T) {
        add(item)
    }

    operator fun plusAssign(items: Collection<T>) {
        items.forEach { add(it) }
    }

    operator fun minusAssign(items: Collection<T>) {
        items.forEach { remove(it) }
    }

    operator fun <U : T> plusAssign(from: EuclideaSet<U>) {
        from.items().forEach { add(it) }
    }

    val size: Int
}

abstract class IndexedSet<T : Primitive>(
    // must have equals consistent with T::equals
    private val comparator: Comparator<in T>
) : EuclideaSet<T> {
    protected abstract fun coincides(item1: T, item2: T): Boolean
    protected abstract fun bound(d: Double): T

    // compares on hashMetric first
    private val hashComparator = Comparator<T> { a, b ->
        when (val compare = a.hashMetric.compareTo(b.hashMetric)) {
            0 -> comparator.compare(a, b)
            else -> compare
        }
    }

    private val set = sortedSetOf(hashComparator)

    override operator fun contains(item: T): Boolean {
        return canonicalImpl(item) !== null
    }

    private fun <U : T> canonicalImpl(item: U): U? {
        // Optimize for already canonical
        // TODO fix this for lines with base points switched
        // if (item in set) return item

        val primary = item.hashMetric
        val range = coincidingRange(primary)
        val subSet = set.subSet(range.first, true, range.second, true)

        @Suppress("UNCHECKED_CAST", "UnnecessaryVariable")
        val res = subSet.firstOrNull { coincides(it, item) } as U?
        return res
    }

    private fun coincidingRange(primary: Double): Pair<T, T> {
        return bound(primary - Epsilon) to bound(primary + Epsilon)
    }

    override fun add(item: T): Boolean {
        return when (canonicalImpl(item)) {
            null -> set.add(item)
            else -> false
        }
    }

    override fun remove(item: T): Boolean {
        return when (canonicalImpl(item)) {
            null -> false
            else -> set.remove(item)
        }
    }

    override fun removeOne(): T? {
        val res = set.firstOrNull()
        res?.let { set.remove(it) }
        return res
    }

    override fun items(): List<T> {
        return set.toList()
    }

    override fun <U : T> canonicalOrNull(item: U): U? {
        return canonicalImpl(item)
    }

    override fun <U : T> canonicalOrAdd(item: U): U {
        return when (val existing = canonicalImpl(item)) {
            null -> {
                set.add(item)
                item
            }
            else -> existing
        }
    }

    override val size: Int
        get() = set.size
}

private val pointComparator = compareBy<Point>({ it.x }, { it.y })

class PointSet : IndexedSet<Point>(
    comparator = pointComparator
) {
    override fun bound(d: Double): Point {
        return Point(d, d)
    }

    override fun coincides(item1: Point, item2: Point): Boolean {
        return euclidea.coincides(item1, item2)
    }

    fun centroid(): Point? {
        val points = items()
        val count = points.size
        return if (count <= 0)
            null
        else {
            val x = points.sumOf { it.x } / size.toDouble()
            val y = points.sumOf { it.y } / size.toDouble()
            Point(x, y)
        }
    }

    companion object {
        fun of(points: Collection<Point>): PointSet {
            val set = PointSet()
            set += points
            return set
        }
    }
}

private val lineComparator = compareBy<Line>(
    { it.xDir },
    { it.yDir },
    { it.yIntercept },
    { it.xIntercept },
    { it.xMin },
    { it.xMax },
    { it.yMin },
    { it.yMax })

class LineSet : IndexedSet<Line>(
    comparator = lineComparator
) {
    override fun bound(d: Double): Line {
        val scaledForHashMetric = d * 2.0
        return Line(Point(scaledForHashMetric, 0.0), Point(scaledForHashMetric, 1.0))
    }

    override fun coincides(item1: Line, item2: Line): Boolean {
        return linesCoincide(item1, item2)
    }

    companion object {
        fun of(lines: Collection<Line>): LineSet {
            val lineSet = LineSet()
            lineSet += lines
            return lineSet
        }
    }
}

private val circleSetComparator = compareBy<Circle>({ it.center.x }, { it.center.y }, { it.radius })

class CircleSet : IndexedSet<Circle>(
    comparator = circleSetComparator
) {
    override fun bound(d: Double): Circle {
        return Circle(Point(d, d), d)
    }

    override fun coincides(item1: Circle, item2: Circle): Boolean {
        return circlesCoincide(item1, item2)
    }

}

class ElementSet : EuclideaSet<Element> {

    private val lineSet = LineSet()
    private val circleSet = CircleSet()

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

    override fun items(): List<Element> {
        return lineSet.items() + circleSet.items()
    }

    override fun <U : Element> canonicalOrNull(item: U): U? {
        return when (item) {
            is Line -> lineSet.canonicalOrNull(item)
            is Circle -> circleSet.canonicalOrNull(item)
            else -> unreachable() // Kotlin compiler can't figure out this is unneeded
        }
    }

    override fun <U : Element> canonicalOrAdd(item: U): U {
        return when (item) {
            is Line -> lineSet.canonicalOrAdd(item)
            is Circle -> circleSet.canonicalOrAdd(item)
            else -> unreachable() // Kotlin compiler can't figure out this is unneeded
        }
    }

    private fun unreachable(): Nothing {
        throw UnsupportedOperationException("Unreachable code reached")
    }

    override val size: Int
        get() = lineSet.size + circleSet.size

    companion object {
        fun of(elements: Collection<Element>): ElementSet {
            val set = ElementSet()
            set += elements
            return set
        }
    }
}

class PrimitiveSet : EuclideaSet<Primitive> {

    private val pointSet = PointSet()
    private val elementSet = ElementSet()

    override fun contains(item: Primitive): Boolean {
        return when (item) {
            is Point -> pointSet.contains(item)
            is Element -> elementSet.contains(item)
            else -> unsupported(item)
        }
    }

    private fun unsupported(item: Primitive): Nothing {
        throw IllegalArgumentException("Unsupported Primitive type: ${item::class}")
    }

    override fun add(item: Primitive): Boolean {
        return when (item) {
            is Point -> pointSet.add(item)
            is Element -> elementSet.add(item)
            else -> unsupported(item)
        }
    }

    override fun remove(item: Primitive): Boolean {
        return when (item) {
            is Point -> pointSet.remove(item)
            is Element -> elementSet.remove(item)
            else -> unsupported(item)
        }
    }

    override fun removeOne(): Primitive? {
        return pointSet.removeOne() ?: elementSet.removeOne()
    }

    override fun items(): List<Primitive> {
        return pointSet.items() + elementSet.items()
    }

    override fun <U : Primitive> canonicalOrNull(item: U): U? {
        return when (item) {
            is Point -> pointSet.canonicalOrNull(item)
            is Element -> elementSet.canonicalOrNull(item)
            else -> unsupported(item)
        }
    }

    override fun <U : Primitive> canonicalOrAdd(item: U): U {
        return when (item) {
            is Point -> pointSet.canonicalOrAdd(item)
            is Element -> elementSet.canonicalOrAdd(item)
            else -> unsupported(item)
        }
    }

    override val size: Int
        get() = elementSet.size + pointSet.size
}
