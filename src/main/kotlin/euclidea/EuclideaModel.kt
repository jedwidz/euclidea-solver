package euclidea

import kotlin.math.*

data class Point(val x: Double, val y: Double) {
    val sqDistance: Double = sq(x) + sq(y)

    operator fun minus(point: Point): Point {
        return Point(x - point.x, y - point.y)
    }

    operator fun plus(point: Point): Point {
        return Point(x + point.x, y + point.y)
    }

    companion object {
        val Origin = Point(0.0, 0.0)
    }
}

sealed class Element {

    data class Line(val point1: Point, val point2: Point) : Element() {
        override fun minus(point: Point): Line {
            return Line(point1 - point, point2 - point)
        }

        override fun plus(point: Point): Line {
            return Line(point1 + point, point2 + point)
        }
    }

    data class Circle(
        val center: Point,
        val radius: Double,
        val sample: Point? = null
    ) : Element() {
        override fun minus(point: Point): Circle {
            return Circle(center - point, radius, sample?.let { it - point })
        }

        override fun plus(point: Point): Circle {
            return Circle(center + point, radius, sample?.let { it + point })
        }
    }

    abstract operator fun minus(point: Point): Element
    abstract operator fun plus(point: Point): Element
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

private fun linesCoincide(line1: Element.Line, line2: Element.Line): Boolean {
    // TODO ? optimize
    return linesIntersect(line1, line2) === Intersection.Coincide
}

private fun circlesCoincide(circle1: Element.Circle, circle2: Element.Circle): Boolean {
    return coincides(circle1.center, circle2.center) && coincides(circle1.radius, circle2.radius)
}

fun coincides(point1: Point, point2: Point): Boolean {
    return coincides(point1.x, point2.x) && coincides(point1.y, point2.y)
}

private const val Epsilon = 0.0000000001
private const val EpsilonRough = Epsilon * 100000

fun coincides(num1: Double, num2: Double): Boolean {
    return abs(num2 - num1) < Epsilon
}

fun coincidesRough(num1: Double, num2: Double): Boolean {
    return abs(num2 - num1) < EpsilonRough
}

fun pointAndLineCoincide(point: Point, line: Element.Line): Boolean {
    val d1 = line.point2.minus(line.point1)
    val d2 = line.point1.minus(point)
    val measure = (d1.x * d2.y - d2.x * d1.y) // / sqrt(d1.sqDistance)
    return coincides(measure, 0.0)
}

private fun linesIntersect(line1: Element.Line, line2: Element.Line): Intersection {
    // Help from: https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
    val p1 = line1.point1
    val p2 = line1.point2
    val p3 = line2.point1
    val p4 = line2.point2
    val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
    return if (coincides(d, 0.0)) {
        // Check if p1 is on line2
        if (coincides((p1.x - p4.x) * (p3.y - p4.y), (p1.y - p4.y) * (p3.x - p4.x)))
            Intersection.Coincide
        else
            Intersection.Disjoint
    } else {
        val a = p1.x * p2.y - p1.y * p2.x
        val b = p3.x * p4.y - p3.y * p4.x
        val x = a * (p3.x - p4.x) - (p1.x - p2.x) * b
        val y = a * (p3.y - p4.y) - (p1.y - p2.y) * b
        Intersection.OnePoint(Point(x / d, y / d))
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
        if (h2 < 0.0 || h2.isNaN())
            Intersection.Disjoint
        else {
            val lod = l / d
            if (coincides(h2, 0.0))
                Intersection.OnePoint(Point(lod * p.x, lod * p.y) + o)
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
    // Help from: https://mathworld.wolfram.com/Circle-LineIntersection.html
    val o = circle.center
    val lineO = line - o
    val dx = lineO.point2.x - lineO.point1.x
    val dy = lineO.point2.y - lineO.point1.y
    val dr2 = sq(dx) + sq(dy)
    val det = lineO.point1.x * lineO.point2.y - lineO.point2.x * lineO.point1.y
    val disc = sq(circle.radius * sqrt(dr2)) - sq(det)
    return if (disc
        < 0.0 || disc.isNaN()
    )
        Intersection.Disjoint
    else if (coincides(disc, 0.0)) {
        Intersection.OnePoint(Point(det * dy / dr2, -det * dx / dr2) + o)
    } else {
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