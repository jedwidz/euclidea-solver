package euclidea

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object EuclideaTools {

    fun lineTool(point1: Point, point2: Point): Element.Line {
        return makeLine(point1, point2, null)
    }

    fun circleTool(center: Point, sample: Point): Element.Circle {
        return makeCircle(center, distance(center, sample), sample, source = null)
    }

    fun perpendicularTool(line: Element.Line, point: Point): Element.Line {
        val direction = line.point2.minus(line.point1)
        val point2 = Point(point.x + direction.y, point.y - direction.x)
        return makeLine(point, point2, LineSource.Perpendicular(line, point))
    }

    fun perpendicularBisectorTool(point1: Point, point2: Point): Element.Line {
        val direction = point2.minus(point1)
        val midpoint = midpoint(point1, point2)
        val point3 = Point(midpoint.x + direction.y, midpoint.y - direction.x)
        return makeLine(midpoint, point3, LineSource.PerpendicularBisect(point1, point2))
    }

    fun angleBisectorTool(pointA: Point, pointO: Point, pointB: Point): Element.Line {
        val dirA = pointA - pointO
        val headingA = atan2(dirA.y, dirA.x)
        val dirB = pointB - pointO
        val headingB = atan2(dirB.y, dirB.x)
        val heading = (headingA + headingB) * 0.5
        val aim = Point(pointO.x + cos(heading), pointO.y + sin(heading))
        return makeLine(pointO, aim, LineSource.AngleBisect(pointA, pointO, pointB))
    }

    fun parallelTool(line: Element.Line, point: Point): Element.Line {
        val direction = line.point2.minus(line.point1)
        val point2 = Point(point.x + direction.x, point.y + direction.y)
        return makeLine(point, point2, LineSource.Parallel(line, point))
    }

    fun nonCollapsingCompassTool(pointA: Point, pointB: Point, center: Point): Element.Circle {
        return makeCircle(
            center, distance(pointA, pointB),
            sample = null,
            source = CircleSource.NonCollapsingCompass(pointA, pointB)
        )
    }

    fun dropPerpendicular(
        linePoint1: Point,
        linePoint2: Point,
        point: Point
    ): Pair<Element.Line, List<Element.Circle>> {
        val circle1 = circleTool(linePoint1, point)
        val circle2 = circleTool(linePoint2, point)
        val other = intersectTwoPointsOther(circle1, circle2, point)
        return lineTool(point, other) to listOf(circle1, circle2)
    }

    fun bisect(point1: Point, point2: Point): Pair<Element.Line, List<Element.Circle>> {
        val circle1 = circleTool(point1, point2)
        val circle2 = circleTool(point2, point1)
        val (cross1, cross2) = intersectTwoPoints(circle1, circle2)
        return lineTool(cross1, cross2) to listOf(circle1, circle2)
    }

    private fun makeLine(point1: Point, point2: Point, source: LineSource?): Element.Line {
        return if (coincides(point1, point2)) invalid() else Element.Line(point1, point2, source = source)
    }

    private fun makeCircle(center: Point, distance: Double, sample: Point?, source: CircleSource?): Element.Circle {
        return if (distance <= 0.0) invalid() else Element.Circle(center, distance, sample, source = source)
    }

}

fun invalid(): Nothing {
    throw InvalidConstructionException()
}

class InvalidConstructionException : RuntimeException()
