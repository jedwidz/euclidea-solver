package euclidea

import euclidea.EuclideaTools.angleBisectConstruction
import euclidea.EuclideaTools.nonCollapsingCompassConstruction
import euclidea.EuclideaTools.parallelConstruction
import euclidea.EuclideaTools.perpendicularBisectConstruction
import euclidea.EuclideaTools.perpendicularConstruction
import kotlin.math.*

interface Primitive

data class Point(val x: Double, val y: Double) : Primitive, Comparable<Point> {

    override fun compareTo(other: Point): Int {
        return when (val compare = x.compareTo(other.x)) {
            0 -> y.compareTo(other.y)
            else -> compare
        }
    }

    val sqDistance: Double = sq(x) + sq(y)
    val distance = sqrt(sqDistance)

    operator fun minus(point: Point): Point {
        return Point(x - point.x, y - point.y)
    }

    operator fun plus(point: Point): Point {
        return Point(x + point.x, y + point.y)
    }

    operator fun times(scale: Double): Point {
        return Point(x * scale, y * scale)
    }

    companion object {
        val Origin = Point(0.0, 0.0)
    }
}

sealed class LineSource {
    data class Perpendicular(val line: Element.Line, val point: Point, val probe: Point? = null) : LineSource()
    data class PerpendicularBisect(val point1: Point, val point2: Point) : LineSource()
    data class AngleBisect(val pointA: Point, val pointO: Point, val pointB: Point) : LineSource()
    data class Parallel(val line: Element.Line, val point: Point, val probe: Point? = null) : LineSource()

    val tool: EuclideaTool
        get() = when (this) {
            is AngleBisect -> EuclideaTool.AngleBisectorTool
            is Parallel -> EuclideaTool.ParallelTool
            is Perpendicular -> EuclideaTool.PerpendicularTool
            is PerpendicularBisect -> EuclideaTool.PerpendicularBisectorTool
        }
}

sealed class CircleSource {
    data class NonCollapsingCompass(val pointA: Point, val pointB: Point) : CircleSource()

    val tool: EuclideaTool
        get() = when (this) {
            is NonCollapsingCompass -> EuclideaTool.NonCollapsingCompassTool
        }
}

sealed class Element : Primitive {

    abstract val sourceTool: EuclideaTool

    data class Line(
        val point1: Point,
        val point2: Point,
        val limit1: Boolean = false,
        val limit2: Boolean = false,
        val source: LineSource? = null
    ) : Element(), Comparable<Line> {

        val xIntercept: Double?
        val yIntercept: Double?
        val xDir: Double
        val yDir: Double
        val xMin: Double?
        val xMax: Double?
        val yMin: Double?
        val yMax: Double?

        // Heading from point1 to point2, in radians in range [0,2*PI)
        val heading: Double

        init {
            val dx = point2.x - point1.x
            val dy = point2.y - point1.y
            fun intercept(da: Double, db: Double, a: Double, b: Double) =
                if (abs(db) < Epsilon) null else a - (da / db) * b
            xIntercept = intercept(dx, dy, point1.x, point1.y)
            yIntercept = intercept(dy, dx, point1.y, point1.x)

            val len = sqrt(sq(dx) + sq(dy))
            val x = dx / len
            val y = dy / len
            val sign = if (abs(x) < Epsilon) sign(y) else sign(x)
            xDir = x * sign
            yDir = y * sign
            val xBounds = limits(point1.x, point2.x)
            xMin = xBounds.first
            xMax = xBounds.second
            val yBounds = limits(point1.y, point2.y)
            yMin = yBounds.first
            yMax = yBounds.second

            heading = normalizeLineHeading(atan2(dy, dx))
        }

        override fun compareTo(other: Line): Int {
            // Not using `compareBy` for perf
            @Suppress("NAME_SHADOWING")
            return when (val compare = xDir.compareTo(other.xDir)) {
                0 -> when (val compare = yDir.compareTo(other.yDir)) {
                    0 -> when (val compare = compareToNullable(yIntercept, other.yIntercept)) {
                        0 -> when (val compare = compareToNullable(xIntercept, other.xIntercept)) {
                            0 -> when (val compare = compareToNullable(xMin, other.xMin)) {
                                0 -> when (val compare = compareToNullable(xMax, other.xMax)) {
                                    0 -> when (val compare = compareToNullable(yMin, other.yMin)) {
                                        0 -> when (val compare = compareToNullable(yMax, other.yMax)) {
                                            0 -> 0
                                            else -> compare
                                        }
                                        else -> compare
                                    }
                                    else -> compare
                                }
                                else -> compare
                            }
                            else -> compare
                        }
                        else -> compare
                    }
                    else -> compare
                }
                else -> compare
            }
        }

        private fun limits(v1: Double, v2: Double): Pair<Double?, Double?> {
            return if (v1 <= v2) {
                (if (limit1) v1 - Epsilon else null) to (if (limit2) v2 + Epsilon else null)
            } else {
                (if (limit2) v2 - Epsilon else null) to (if (limit1) v1 + Epsilon else null)
            }
        }

        override fun minus(point: Point): Line {
            return Line(point1 - point, point2 - point)
        }

        override fun plus(point: Point): Line {
            return Line(point1 + point, point2 + point)
        }

        fun filterLimits(intersection: Intersection): Intersection {
            if (!hasLimit())
                return intersection
            val points = intersection.points()
            val limitedPoints = points.filter { point -> withinLimits(point) }
            return if (limitedPoints.size == points.size)
                intersection
            else Intersection.of(limitedPoints)
        }

        fun hasLimit() = limit1 || limit2

        fun withinLimits(point: Point): Boolean {
            return (xMin == null || point.x >= xMin)
                    && (xMax == null || point.x <= xMax)
                    && (yMin == null || point.y >= yMin)
                    && (yMax == null || point.y <= yMax)
        }

        fun extended(): Line {
            return if (hasLimit())
                copy(limit1 = false, limit2 = false)
            else this
        }

        override fun distance(): Double {
            return distance(point1, point2)
        }

        override val sourceTool: EuclideaTool
            get() = source?.tool ?: EuclideaTool.LineTool
    }

    data class Circle(
        val center: Point,
        val radius: Double,
        val sample: Point? = null,
        val source: CircleSource? = null
    ) : Element(), Comparable<Circle> {

        override fun compareTo(other: Circle): Int {
            return when (val compare = center.compareTo(other.center)) {
                0 -> radius.compareTo(other.radius)
                else -> compare
            }
        }

        override fun minus(point: Point): Circle {
            return Circle(center - point, radius, sample?.let { it - point })
        }

        override fun plus(point: Point): Circle {
            return Circle(center + point, radius, sample?.let { it + point })
        }

        override fun distance(): Double {
            return radius
        }

        override val sourceTool: EuclideaTool
            get() = source?.tool ?: EuclideaTool.CircleTool
    }

    abstract operator fun minus(point: Point): Element
    abstract operator fun plus(point: Point): Element
    abstract fun distance(): Double
}

sealed class Intersection {
    object Disjoint : Intersection()
    data class OnePoint(val point: Point) : Intersection()
    data class TwoPoints(val point1: Point, val point2: Point) : Intersection()

    // Coincide at an infinite number of points
    object Coincide : Intersection()

    fun points(): List<Point> {
        return when (this) {
            Coincide -> emptyList()
            Disjoint -> emptyList()
            is OnePoint -> listOf(point)
            is TwoPoints -> listOf(point1, point2)
        }
    }

    companion object {
        fun of(points: List<Point>): Intersection {
            return when (points.size) {
                0 -> Disjoint
                1 -> OnePoint(points.first())
                2 -> TwoPoints(points.first(), points.last())
                else -> throw IllegalArgumentException("Too many points for intersection: $points")
            }
        }
    }
}

fun intersect(element1: Element, element2: Element): Intersection {
    return when (element1) {
        is Element.Circle -> when (element2) {
            is Element.Circle -> circlesIntersect(element1, element2)
            is Element.Line -> circleLineIntersect(element1, element2)
        }
        is Element.Line -> when (element2) {
            is Element.Circle -> circleLineIntersect(element2, element1)
            is Element.Line -> linesIntersect(element1, element2)
        }
    }
}

fun normalizeLineHeading(heading: Double): Double {
    return heading.mod(2 * PI)
}

fun distance(point1: Point, point2: Point): Double {
    return sqrt(sq(point1.x - point2.x) + sq(point1.y - point2.y))
}

// Heading in degrees, in range (-90, 90) from positive y-axis
fun heading(point1: Point, point2: Point): Double {
    val d = if (point2.y > point1.y) point2.minus(point1) else point1.minus(point2)
    return atan2(d.x, d.y) * 180.0 / PI
}

fun midpoint(point1: Point, point2: Point): Point {
    return Point((point1.x + point2.x) * 0.5, (point1.y + point2.y) * 0.5)
}

fun rotatePoint(center: Point, point: Point, deg: Double): Point {
    val d = point.minus(center)
    val rad = deg * PI / 180.0
    val x = d.x * cos(rad) - d.y * sin(rad)
    val y = d.x * sin(rad) + d.y * cos(rad)
    return Point(center.x + x, center.y + y)
}

// Angle aob in degrees, in range (0, 180)
fun angle(a: Point, o: Point, b: Point): Double {
    val va = a.minus(o)
    val vb = b.minus(o)
    val cosTh = (va.x * vb.x + va.y * vb.y) / sqrt(va.sqDistance * vb.sqDistance)
    return acos(cosTh) * 180.0 / PI
}

fun coincides(element1: Element, element2: Element): Boolean {
    return when (element1) {
        is Element.Circle -> when (element2) {
            is Element.Circle -> circlesCoincide(element1, element2)
            else -> false
        }
        is Element.Line -> when (element2) {
            is Element.Line -> linesCoincide(element1, element2)
            else -> false
        }
    }
}

private fun coincidesNullable(num1: Double?, num2: Double?): Boolean {
    return num1 === null && num2 === null || (num1 !== null && num2 !== null && coincides(num1, num2))
}

fun linesCoincide(line1: Element.Line, line2: Element.Line): Boolean {
    return linesCoincideNoLimits(line1, line2)
            && coincidesNullable(line1.xMin, line2.xMin)
            && coincidesNullable(line1.xMax, line2.xMax)
            && coincidesNullable(line1.yMin, line2.yMin)
            && coincidesNullable(line1.yMax, line2.yMax)
}

private fun linesCoincideNoLimits(line1: Element.Line, line2: Element.Line): Boolean {
    // More consistent with point/line tests
//    return pointAndLineCoincideNoLimits(line1.point1, line2) && pointAndLineCoincideNoLimits(line1.point2, line2) &&
//            pointAndLineCoincideNoLimits(line2.point1, line2) && pointAndLineCoincideNoLimits(line2.point2, line2)
    return coincidesNullable(line1.xIntercept, line2.xIntercept) && coincidesNullable(
        line1.yIntercept,
        line2.yIntercept
    ) && linesParallel(line1, line2)
}

fun linesParallel(line1: Element.Line, line2: Element.Line): Boolean {
    return coincides(line1.xDir, line2.xDir) && coincides(line1.yDir, line2.yDir)
}

fun linesPerpendicular(line1: Element.Line, line2: Element.Line): Boolean {
    return coincides(line1.xDir, line2.yDir) && coincides(-line1.yDir, line2.xDir) ||
            coincides(-line1.xDir, line2.yDir) && coincides(line1.yDir, line2.xDir)
}

fun circlesCoincide(circle1: Element.Circle, circle2: Element.Circle): Boolean {
    return coincides(circle1.center, circle2.center) && coincides(circle1.radius, circle2.radius)
}

fun coincides(point1: Point, point2: Point): Boolean {
    return coincides(point1.x, point2.x) && coincides(point1.y, point2.y)
}

const val Epsilon = 0.0000000001
private const val EpsilonRough = Epsilon * 100000

fun coincides(num1: Double, num2: Double): Boolean {
    return abs(num2 - num1) < Epsilon
}

fun coincidesRough(num1: Double, num2: Double): Boolean {
    return abs(num2 - num1) < EpsilonRough
}

fun pointAndLineCoincide(point: Point, line: Element.Line): Boolean {
    return pointAndLineCoincideNoLimits(point, line) && line.withinLimits(point)
}

fun pointAndLineCoincideNoLimits(point: Point, line: Element.Line): Boolean {
    val d1 = line.point2.minus(line.point1)
    val d2 = line.point1.minus(point)
    val measure = (d1.x * d2.y - d2.x * d1.y) // / sqrt(d1.sqDistance)
    return coincides(measure, 0.0)
}

fun pointAndCircleCoincide(point: Point, circle: Element.Circle): Boolean {
    // check for coinciding square of distance
    // TODO- maybe should use distance rather than its square?
    val d = point.minus(circle.center).sqDistance
    return coincides(d, sq(circle.radius))
}

fun pointAndElementCoincide(point: Point, element: Element): Boolean {
    return when (element) {
        is Element.Line -> pointAndLineCoincide(point, element)
        is Element.Circle -> pointAndCircleCoincide(point, element)
    }
}

fun intersectAnyPoint(element1: Element, element2: Element): Point =
    when (val i = intersect(element1, element2)) {
        is Intersection.OnePoint -> i.point
        is Intersection.TwoPoints -> i.point1
        else -> error("At least one intersection point expected: $i")
    }

fun intersectOnePoint(element1: Element, element2: Element): Point =
    when (val i = intersect(element1, element2)) {
        is Intersection.OnePoint -> i.point
        else -> error("One intersection point expected: $i")
    }

fun intersectTwoPoints(
    element1: Element,
    element2: Element,
    swap: Boolean = false
): Pair<Point, Point> =
    when (val i = intersect(element1, element2)) {
        is Intersection.TwoPoints -> if (swap) Pair(i.point2, i.point1) else Pair(i.point1, i.point2)
        else -> error("Two intersection points expected: $i")
    }

fun intersectTwoPointsOther(
    element1: Element,
    element2: Element,
    point1: Point
): Point {
    val intersection = intersect(element1, element2)
    val points = intersection.points().filter { point2 -> !coincides(point1, point2) }
    return when (points.size) {
        1 -> points.first()
        else -> error("Expected one point other than $point1: $intersection")
    }
}

private fun linesIntersect(line1: Element.Line, line2: Element.Line): Intersection {
    return line1.filterLimits(line2.filterLimits(linesIntersectNoLimits(line1, line2)))
}

private fun linesIntersectNoLimits(line1: Element.Line, line2: Element.Line): Intersection {
    // Help from: https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
    return if (linesCoincide(line1, line2))
        Intersection.Coincide
    else {
        val p1 = line1.point1
        val p2 = line1.point2
        val p3 = line2.point1
        val p4 = line2.point2
        val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
        return if (coincides(d, 0.0))
            Intersection.Disjoint
        else {
            val a = p1.x * p2.y - p1.y * p2.x
            val b = p3.x * p4.y - p3.y * p4.x
            val x = a * (p3.x - p4.x) - (p1.x - p2.x) * b
            val y = a * (p3.y - p4.y) - (p1.y - p2.y) * b
            Intersection.OnePoint(Point(x / d, y / d))
        }
    }
}

private fun circlesIntersect(circle1: Element.Circle, circle2: Element.Circle): Intersection {
    // Help from: https://math.stackexchange.com/a/1033561
    val o = circle1.center
    val p = circle2.center - o
    val d2 = sq(p.x) + sq(p.y)
    val d = sqrt(d2)
    return if (coincides(d, 0.0)) {
        if (coincides(circle1.radius, circle2.radius))
            Intersection.Coincide
        else Intersection.Disjoint
    } else {
        val l = (sq(circle1.radius) - sq(circle2.radius) + d2) / (2.0 * d)
        val h2 = sq(circle1.radius) - sq(l)
        if (h2.isNaN())
            Intersection.Disjoint
        else {
            val lod = l / d
            if (coincides(h2, 0.0))
                Intersection.OnePoint(Point(lod * p.x, lod * p.y) + o)
            else if (h2 < 0.0)
                Intersection.Disjoint
            else {
                val hod = sqrt(h2) / d
                Intersection.TwoPoints(
                    Point(lod * p.x + hod * p.y, lod * p.y - hod * p.x) + o,
                    Point(lod * p.x - hod * p.y, lod * p.y + hod * p.x) + o
                )
            }
        }
    }
}

private fun circleLineIntersect(circle: Element.Circle, line: Element.Line): Intersection {
    return line.filterLimits(circleLineIntersectNoLimits(circle, line))
}

private fun circleLineIntersectNoLimits(circle: Element.Circle, line: Element.Line): Intersection {
    // Help from: https://mathworld.wolfram.com/Circle-LineIntersection.html
    val o = circle.center
    val pointO1 = line.point1 - o
    val pointO2 = line.point2 - o
    val dx = pointO2.x - pointO1.x
    val dy = pointO2.y - pointO1.y
    val dr2 = sq(dx) + sq(dy)
    val det = pointO1.x * pointO2.y - pointO2.x * pointO1.y
    val disc = sq(circle.radius * sqrt(dr2)) - sq(det)
    return if (disc.isNaN())
        Intersection.Disjoint
    else if (coincides(disc, 0.0)) {
        Intersection.OnePoint(Point(det * dy / dr2, -det * dx / dr2) + o)
    } else if (disc < 0.0)
        Intersection.Disjoint
    else {
        val f = sqrt(disc)
        val xf = dx * f
        val yf = dy * f
        Intersection.TwoPoints(
            Point((det * dy - xf) / dr2, (-det * dx - yf) / dr2) + o,
            Point((det * dy + xf) / dr2, (-det * dx + yf) / dr2) + o
        )
    }
}

fun sq(v: Double): Double {
    return v * v
}

fun elementsReflected(obj: Any): List<Element> {
    return obj.reflectProperties(Element::class).map { it.value }
}

val Element.isLine: Boolean
    get() {
        return this is Element.Line
    }

val Element.isLineFromLine: Boolean
    get() {
        return this is Element.Line && source === null
    }

val Element.isLineFromPerpendicularBisector: Boolean
    get() {
        return this is Element.Line && source is LineSource.PerpendicularBisect
    }

val Element.isLineFromPerpendicular: Boolean
    get() {
        return this is Element.Line && source is LineSource.Perpendicular
    }

val Element.isLineFromParallel: Boolean
    get() {
        return this is Element.Line && source is LineSource.Parallel
    }

val Element.isLineFromAngleBisector: Boolean
    get() {
        return this is Element.Line && source is LineSource.AngleBisect
    }

val Element.isCircle: Boolean
    get() {
        return this is Element.Circle
    }

val Element.isCircleFromCircle: Boolean
    get() {
        return this is Element.Circle && source === null
    }

val Element.isCircleFromNonCollapsingCompass: Boolean
    get() {
        return this is Element.Circle && source is CircleSource.NonCollapsingCompass
    }

fun Element.constructionPoints(): List<Point> {
    return when (this) {
        is Element.Circle -> when (source) {
            is CircleSource.NonCollapsingCompass -> listOf(center, source.pointA, source.pointB)
            null -> listOfNotNull(center, sample)
        }
        is Element.Line -> when (source) {
            is LineSource.AngleBisect -> listOf(source.pointA, source.pointO, source.pointB)
            is LineSource.Parallel -> listOf(source.line.point1, source.line.point2, source.point)
            is LineSource.Perpendicular -> listOf(source.line.point1, source.line.point2, source.point)
            is LineSource.PerpendicularBisect -> listOf(source.point1, source.point2)
            null -> listOf(point1, point2)
        }
    }
}

fun Element.constructionElements(): List<Element> {
    return when (this) {
        is Element.Circle -> when (source) {
            is CircleSource.NonCollapsingCompass -> nonCollapsingCompassConstruction(
                source.pointA,
                source.pointB,
                center
            )
            null -> ElementSet()
        }
        is Element.Line -> when (source) {
            is LineSource.AngleBisect -> angleBisectConstruction(source.pointA, source.pointO, source.pointB)
            is LineSource.Parallel -> parallelConstruction(source.line, source.point, source.probe)
            is LineSource.Perpendicular -> perpendicularConstruction(source.line, source.point, source.probe)
            is LineSource.PerpendicularBisect -> perpendicularBisectConstruction(source.point1, source.point2)
            null -> ElementSet()
        }
    }.items()
}

fun meetAtOnePoint(element1: Element, element2: Element): Boolean {
    return intersect(element1, element2) is Intersection.OnePoint
}

fun onePointIntersection(element1: Element, element2: Element): Point? {
    return when (val intersection = intersect(element1, element2)) {
        is Intersection.OnePoint -> intersection.point
        else -> null
    }
}

fun formsSquare(elements: LineSet): Boolean {
    if (elements.size != 4)
        return false
    val points = PointSet()
    points += elements.items().pairs().mapNotNull { (line1, line2) -> onePointIntersection(line1, line2) }
    if (points.size != 4)
        return false
    val center = points.centroid()!!
    val sampleVertex = points.items().minBy { point -> distance(center, point) }
    val angle = 360.0 / 4.toDouble()
    return (1 until 4).map { i -> rotatePoint(center, sampleVertex, angle * i.toDouble()) }
        .all { vertex -> points.contains(vertex) }
}

fun mirrorAcross(line: Element.Line, point: Point): Point {
    val proj = projection(line, point)
    return proj - (point - proj)
}

fun projection(line: Element.Line, point: Point): Point {
    val d = point - line.point1
    val dir = line.point2 - line.point1
    val e = dir * (1.0 / dir.distance)
    val dot = d.x * e.x + d.y * e.y
    return line.point1 + e * dot
}