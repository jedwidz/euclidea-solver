package euclidea

import euclidea.Element.Circle
import euclidea.Element.Line
import java.util.*

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

abstract class IndexedSet<T> private constructor(
    // All entries must have non-null item
    private val set: TreeSet<IndexedSet<T>.Entry>
) : EuclideaSet<T> {

    constructor() : this(sortedSetOf())

    protected constructor(copyFrom: IndexedSet<T>) : this(TreeSet(copyFrom.set))

    private inner class Entry(val item: T?, val hashMetric: Double) : Comparable<Entry> {
        override fun compareTo(other: Entry): Int {
            return when (val compare = hashMetric.compareTo(other.hashMetric)) {
                0 -> {
                    // Only compare items if both present
                    when (val otherItem = other.item) {
                        null -> 0
                        else -> item?.let { compareItems(it, otherItem) } ?: 0
                    }
                }
                else -> compare
            }
        }
    }

    private fun entryForHashMetric(d: Double): Entry {
        return Entry(null, d)
    }

    private fun entryForItem(item: T): Entry {
        return Entry(item, hashMetric(item))
    }

    // 'Hash metric', which must differ by less than Epsilon for coinciding items
    // Used as a dimension for indexing
    protected abstract fun hashMetric(item: T): Double

    protected abstract fun coincides(item1: T, item2: T): Boolean

    protected abstract fun compareItems(a: T, b: T): Int

    override operator fun contains(item: T): Boolean {
        return canonicalImpl(item) !== null
    }

    private fun <U : T> canonicalImpl(item: U): U? {
        // Optimize for already canonical
        // TODO fix this for lines with base points switched
        // if (item in set) return item

        val primary = hashMetric(item)
        val range = coincidingRange(primary)
        val subSet = set.subSet(range.first, true, range.second, true)

        @Suppress("UNCHECKED_CAST", "UnnecessaryVariable")
        val res = subSet.firstOrNull { coincides(it.item!!, item) }?.item as U?
        return res
    }

    private fun coincidingRange(primary: Double): Pair<Entry, Entry> {
        return entryForHashMetric(primary - Epsilon) to entryForHashMetric(primary + Epsilon)
    }

    override fun add(item: T): Boolean {
        return when (canonicalImpl(item)) {
            null -> set.add(entryForItem(item))
            else -> false
        }
    }

    override fun remove(item: T): Boolean {
        return when (canonicalImpl(item)) {
            null -> false
            else -> set.remove(entryForItem(item))
        }
    }

    override fun removeOne(): T? {
        val res = set.firstOrNull()
        res?.let { set.remove(it) }
        return res?.item
    }

    override fun items(): List<T> {
        return set.map { it.item!! }
    }

    override fun <U : T> canonicalOrNull(item: U): U? {
        return canonicalImpl(item)
    }

    override fun <U : T> canonicalOrAdd(item: U): U {
        return when (val existing = canonicalImpl(item)) {
            null -> {
                set.add(entryForItem(item))
                item
            }
            else -> existing
        }
    }

    override val size: Int
        get() = set.size
}

class PointSet : IndexedSet<Point>() {

    override fun hashMetric(item: Point): Double {
        return (item.x + item.y) / 2.0
    }

    override fun compareItems(a: Point, b: Point): Int {
        return a.compareTo(b)
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

class LineSet : IndexedSet<Line> {

    constructor() : super()

    constructor(copyFrom: LineSet) : super(copyFrom)

    override fun hashMetric(item: Line): Double {
        with(item) {
            val intercept = smallerNullable(xIntercept, yIntercept)
            return ((intercept ?: 0.0) + xDir) / 2.0
        }
    }

    override fun compareItems(a: Line, b: Line): Int {
        return a.compareTo(b)
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

class CircleSet : IndexedSet<Circle> {

    constructor() : super()

    constructor(copyFrom: CircleSet) : super(copyFrom)

    override fun hashMetric(item: Circle): Double {
        with(item) {
            return (center.x + center.y + radius) / 3.0
        }
    }

    override fun compareItems(a: Circle, b: Circle): Int {
        return a.compareTo(b)
    }

    override fun coincides(item1: Circle, item2: Circle): Boolean {
        return circlesCoincide(item1, item2)
    }
}

class ElementSet private constructor(
    private val lineSet: LineSet,
    private val circleSet: CircleSet
) : EuclideaSet<Element> {

    constructor() : this(LineSet(), CircleSet())

    constructor(set: ElementSet) : this(LineSet(set.lineSet), CircleSet(set.circleSet))

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

class DoubleSet : IndexedSet<Double>() {

    override fun hashMetric(item: Double): Double {
        return item
    }

    override fun compareItems(a: Double, b: Double): Int {
        return a.compareTo(b)
    }

    override fun coincides(item1: Double, item2: Double): Boolean {
        return euclidea.coincides(item1, item2)
    }

    companion object {
        fun of(items: Collection<Double>): DoubleSet {
            val set = DoubleSet()
            set += items
            return set
        }
    }
}

class ElementsByTool : EuclideaSet<Element> {
    // TODO- circular dependency between tools source file and this one
    private val delegate = EnumMap<EuclideaTool, ElementSet>(EuclideaTool::class.java)

    constructor()

    constructor(elementsByTool: ElementsByTool) {
        elementsByTool.delegate.mapValuesTo(delegate) { ElementSet(it.value) }
    }

    override fun contains(item: Element): Boolean {
        return maybeSetFor(item)?.contains(item) ?: false
    }

    private fun maybeSetFor(item: Element) = delegate[item.sourceTool]

    private fun setFor(item: Element) = delegate.getOrPut(item.sourceTool) { ElementSet() }

    override fun removeOne(): Element? {
        return delegate.values.firstNotNullOfOrNull { set -> set.removeOne() }
    }

    override fun add(item: Element): Boolean {
        return setFor(item).add(item)
    }

    override fun remove(item: Element): Boolean {
        return maybeSetFor(item)?.remove(item) ?: false
    }

    override fun items(): List<Element> {
        // TODO could be inefficient
        return delegate.values.flatMap { it.items() }
    }

    override fun <U : Element> canonicalOrNull(item: U): U? {
        return maybeSetFor(item)?.canonicalOrNull(item)
    }

    override fun <U : Element> canonicalOrAdd(item: U): U {
        return setFor(item).canonicalOrAdd(item)
    }

    fun itemsForTool(nextTool: EuclideaTool?): List<Element> {
        return when (nextTool) {
            null -> items()
            else -> delegate[nextTool]?.items() ?: listOf()
        }
    }

    override val size: Int
        get() = delegate.values.sumOf { it.size }
}
