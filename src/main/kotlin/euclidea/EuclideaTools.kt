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

    fun perpendicularTool(line: Element.Line, point: Point, probe: Point? = null): Element.Line {
        val direction = line.point2.minus(line.point1)
        val point2 = Point(point.x + direction.y, point.y - direction.x)
        return makeLine(point, point2, LineSource.Perpendicular(line, point, probe))
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

    fun parallelTool(line: Element.Line, point: Point, probe: Point? = null): Element.Line {
        val direction = line.point2.minus(line.point1)
        val point2 = Point(point.x + direction.x, point.y + direction.y)
        return makeLine(point, point2, LineSource.Parallel(line, point, probe))
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

    fun perpendicular(line: Element.Line, point: Point, probe: Point?): Pair<Element.Line, List<Element>> {
        // Requires a probe point
        if (probe === null)
            invalid()
        val extended = line.extended()
        if (pointAndLineCoincide(point, extended)) {
            // Erect perpendicular
            if (pointAndLineCoincide(probe, extended)) {
                // 4E construction
                // TODO consider just disallowing this case, it's likely unintended
                val equal = circleTool(point, probe)
                val (point1, point2) = intersectTwoPoints(equal, extended)
                val circle1 = circleTool(point1, point2)
                val circle2 = circleTool(point2, point1)
                val aim = intersectTwoPoints(circle1, circle2).first
                return lineTool(point, aim) to listOf(extended, equal, circle1, circle2)
            } else {
                // 3E construction
                val equal = circleTool(probe, point)
                val other = intersectTwoPointsOther(equal, extended, point)
                val cross = lineTool(other, probe)
                val aim = intersectTwoPointsOther(cross, equal, other)
                return lineTool(point, aim) to listOf(extended, equal, cross)
            }
        } else {
            // Drop perpendicular
            if (pointAndLineCoincide(probe, extended)) {
                // 3E construction
                val circle1 = circleTool(probe, point)
                // Arbitrary other point on the line
                val probe2 = intersectTwoPoints(circle1, extended).first
                val circle2 = circleTool(probe2, point)
                val aim = intersectTwoPointsOther(circle1, circle2, point)
                return lineTool(point, aim) to listOf(extended, circle1, circle2)
            } else {
                // Could do a 4E construction here, but that's probably not the intention
                invalid()
            }
        }
    }

    enum class ParallelOption { AllCircles, WithCircleThenLine, WithCircleThenLineVariant, WithLineThenCircle }

    fun parallel(
        line: Element.Line,
        point: Point,
        probe: Point?,
        option: ParallelOption = ParallelOption.WithCircleThenLine,
        useSecondDir: Boolean = false
    ): Pair<Element.Line, List<Element>> {
        val extended = line.extended()
        if (pointAndLineCoincide(point, extended))
            return extended to listOf()
        else {
            if (probe === null || !pointAndLineCoincide(probe, extended))
                invalid()
            fun withCircleThenLine(variant: Boolean): Pair<Element.Line, List<Element>> {
                // 4E construction, with one construction line (not through `point`)
                val circle1 = circleTool(probe, point)
                val dirs = intersectTwoPoints(circle1, extended)
                val dir = if (useSecondDir) dirs.second else dirs.first
                val circle2 = circleTool(dir, point)
                val other = intersectTwoPointsOther(circle2, circle1, point)
                val cross = lineTool(other, if (variant) probe else dir)
                val aim = intersectTwoPointsOther(cross, if (variant) circle1 else circle2, other)
                return lineTool(point, aim) to listOf(extended, circle1, circle2, cross)
            }
            when (option) {
                ParallelOption.AllCircles -> {
                    // 4E construction, with all circles
                    val circle1 = circleTool(probe, point)
                    val dirs = intersectTwoPoints(circle1, extended)
                    val dir = if (useSecondDir) dirs.second else dirs.first
                    val circle2 = circleTool(dir, probe)
                    val circle3 = circleTool(point, probe)
                    val aim = intersectTwoPointsOther(circle2, circle3, probe)
                    return lineTool(point, aim) to listOf(extended, circle1, circle2, circle3)
                }
                ParallelOption.WithCircleThenLine -> return withCircleThenLine(false)
                ParallelOption.WithCircleThenLineVariant -> return withCircleThenLine(true)
                ParallelOption.WithLineThenCircle -> {
                    // 4E construction, with one construction line through `point`
                    val cross = lineTool(point, probe)
                    val circle1 = circleTool(probe, point)
                    val other = intersectTwoPointsOther(circle1, cross, point)
                    val dirs = intersectTwoPoints(circle1, extended)
                    val dir = if (useSecondDir) dirs.second else dirs.first
                    val circle2 = circleTool(dir, other)
                    val aim = intersectTwoPointsOther(circle2, circle1, other)
                    return lineTool(point, aim) to listOf(extended, circle1, circle2, cross)
                }
            }
        }
    }

    fun perpendicularBisect(point1: Point, point2: Point): Pair<Element.Line, List<Element.Circle>> {
        val circle1 = circleTool(point1, point2)
        val circle2 = circleTool(point2, point1)
        val (cross1, cross2) = intersectTwoPoints(circle1, circle2)
        return lineTool(cross1, cross2) to listOf(circle1, circle2)
    }

    fun angleBisect(
        pointA: Point,
        pointO: Point,
        pointB: Point,
        toO: Boolean = true
    ): Pair<Element.Line, List<Element>> {
        // TODO tool consumes 4E, but this construction is 5E
        val lineB = Element.Line(pointO, pointB)
        val rayB = Element.Line(pointO, pointB, limit1 = true)
        val equalA = circleTool(pointO, pointA)
        val aimB = intersectOnePoint(equalA, rayB)
        val circleA = circleTool(pointA, if (toO) pointO else aimB)
        val circleB = circleTool(aimB, if (toO) pointO else pointA)
        val (cross1, cross2) = intersectTwoPoints(equalA, circleB)
        return lineTool(cross1, cross2) to listOf(lineB, equalA, circleA, circleB)
    }

    fun nonCollapsingCompass(pointA: Point, pointB: Point, center: Point): Pair<Element.Circle, List<Element.Circle>> {
        // Optimal 5E construction
        val circle1 = circleTool(pointA, center)
        val circle2 = circleTool(center, pointA)
        val (cross1, cross2) = intersectTwoPoints(circle1, circle2)
        val circle3 = circleTool(cross1, pointB)
        val circle4 = circleTool(cross2, pointB)
        val aim = intersectTwoPointsOther(circle3, circle4, pointB)
        return circleTool(center, aim) to listOf(circle1, circle2, circle3, circle4)
    }

    fun nonCollapsingCompassConstruction(pointA: Point, pointB: Point, center: Point): ElementSet {
        val res = ElementSet()
        res += nonCollapsingCompass(pointA, pointB, center).second
        res += nonCollapsingCompass(pointB, pointA, center).second
        // TODO ? include 4L parallelogram construction
        return res
    }


    fun angleBisectConstruction(pointA: Point, pointO: Point, pointB: Point): ElementSet {
        val res = ElementSet()
        for (toO in listOf(true, false)) {
            res += angleBisect(pointA, pointO, pointB, toO = toO).second
            res += angleBisect(pointB, pointO, pointA, toO = toO).second
        }
        return res
    }

    fun perpendicularConstruction(line: Element.Line, point: Point, probe: Point?): ElementSet {
        val res = ElementSet()
        res += perpendicular(line, point, probe).second
        return res
    }

    fun perpendicularBisectConstruction(point1: Point, point2: Point): ElementSet {
        val res = ElementSet()
        res += perpendicularBisect(point1, point2).second
        return res
    }

    fun parallelConstruction(line: Element.Line, point: Point, probe: Point?): ElementSet {
        val res = ElementSet()
        for (option in ParallelOption.values()) {
            for (useSecondDir in listOf(false, true))
                res += parallel(line, point, probe, option, useSecondDir = useSecondDir).second
        }
        return res
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
