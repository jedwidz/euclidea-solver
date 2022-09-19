package euclidea

import kotlin.math.abs
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double) {
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

    data class Circle(val center: Point, val radius: Double) : Element() {
        override fun minus(point: Point): Circle {
            return Circle(center - point, radius)
        }

        override fun plus(point: Point): Circle {
            return Circle(center + point, radius)
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

private fun linesIntersect(line1: Element.Line, line2: Element.Line): Intersection {
    // Help from: https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
    val p1 = line1.point1
    val p2 = line1.point2
    val p3 = line2.point1
    val p4 = line2.point2
    val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
    return if (d == 0.0) {
        // Check if p1 is on line2
        if ((p1.x - p4.x) * (p3.y - p4.y) - (p1.y - p4.y) * (p3.x - p4.x) == 0.0)
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
    val o = circle2.center
    val p = circle1.center - o
    val d2 = sq(p.x) + sq(p.y)
    val d = sqrt(d2)
    return if (d == 0.0) {
        if (circle1.radius == circle2.radius)
            Intersection.Coincide
        else Intersection.Disjoint
    } else {
        val l = (sq(circle1.radius) - sq(circle2.radius) + d2) / (2.0 * d)
        val h2 = sq(circle1.radius) - sq(l)
        if (h2 < 0.0 || h2.isNaN())
            Intersection.Disjoint
        else {
            if (h2 == 0.0)
                Intersection.OnePoint(Point(l / d * p.x, l / d * p.y) + o)
            else {
                val hod = sqrt(h2) / d
                Intersection.TwoPoints(
                    Point(l / d * p.x + hod * p.y, l / d * p.y - hod * p.x) + o,
                    Point(l / d * p.x - hod * p.y, l / d * p.y + hod * p.x) + o
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
    else if (disc == 0.0) {
        Intersection.OnePoint(Point(det * dy / dr2, -det * dx / dr2) + o)
    } else {
        val f = sqrt(disc)
        val sgn = if (dy < 0.0) -1.0 else 1.0
        val xf = sgn * dx * f
        val yf = abs(dy) * f
        Intersection.TwoPoints(
            Point((det * dy - xf) / dr2, (-det * dx - yf) / dr2) + o,
            Point((det * dy + xf) / dr2, (-det * dx + yf) / dr2) + o
        )
    }
}

private fun sq(v: Double): Double {
    return v * v
}