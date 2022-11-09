package euclidea

import euclidea.Point.Companion.Origin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.sqrt
import kotlin.test.assertEquals

class EuclideaModelTest {

    @Test
    fun lineIntersect_onePoint() {
        val line1 = Element.Line(Point(0.0, 0.0), Point(10.0, 10.0))
        val line2 = Element.Line(Point(2.0, 5.0), Point(3.0, 5.0))
        val intersection = intersect(line1, line2)
        Assertions.assertEquals(Intersection.OnePoint(Point(5.0, 5.0)), intersection)
    }

    @Test
    fun lineIntersect_onePoint_axes() {
        val xAxis = Element.Line(Point(-5.0, 0.0), Point(10.0, 0.0))
        val yAxis = Element.Line(Point(0.0, 1.0), Point(0.0, 2.0))
        val intersection = intersect(xAxis, yAxis)
        Assertions.assertEquals(Intersection.OnePoint(Origin), intersection)
    }

    @Test
    fun lineIntersect_disjoint() {
        val line1 = Element.Line(Point(-5.0, 0.0), Point(10.0, 1.0))
        val line2 = Element.Line(Point(0.0, 1.0), Point(30.0, 3.0))
        val intersection = intersect(line1, line2)
        Assertions.assertEquals(Intersection.Disjoint, intersection)
    }

    @Test
    fun lineIntersect_coincide() {
        val line1 = Element.Line(Point(-5.0, 0.0), Point(10.0, 1.0))
        val line2 = Element.Line(Point(-5.0, 0.0), Point(25.0, 2.0))
        val intersection = intersect(line1, line2)
        Assertions.assertEquals(Intersection.Coincide, intersection)
    }

    @Test
    fun lineIntersect_coincide_identical() {
        val line = Element.Line(Point(-5.0, 0.0), Point(10.0, 1.0))
        val intersection = intersect(line, line)
        Assertions.assertEquals(Intersection.Coincide, intersection)
    }

    @Test
    fun circleCircleIntersect_twoPoints() {
        val circle1 = Element.Circle(Point(-1.0, 2.0), 2.0)
        val circle2 = Element.Circle(Point(1.0, 2.0), 2.0)
        val intersection = intersect(circle1, circle2)
        val s = sqrt(3.0)
        Assertions.assertEquals(Intersection.TwoPoints(Point(0.0, 2.0 - s), Point(0.0, 2.0 + s)), intersection)
    }

    @Test
    fun circleCircleIntersect_bugFix() {
        val circle1 = Element.Circle(center = Point(x = 0.0, y = 0.0), radius = 1.0, sample = null)
        val circle2 = Element.Circle(center = Point(x = -2.0, y = 0.0), radius = 2.0, sample = Point(x = 0.0, y = 0.0))
        val intersection = intersect(circle1, circle2)
        val x = -0.25
        val s = sqrt(15.0 / 16.0)
        Assertions.assertEquals(Intersection.TwoPoints(Point(x, s), Point(x, -s)), intersection)
    }

    @Test
    fun circleCircleIntersect_onePoint() {
        val circle1 = Element.Circle(Point(-1.0, 1.0), 1.0)
        val circle2 = Element.Circle(Point(1.0, 1.0), 1.0)
        val intersection = intersect(circle1, circle2)
        Assertions.assertEquals(Intersection.OnePoint(Point(0.0, 1.0)), intersection)
    }

    @Test
    fun circleCircleIntersect_disjoint() {
        val circle1 = Element.Circle(Point(-1.0, 1.0), 0.9)
        val circle2 = Element.Circle(Point(1.0, 1.0), 0.9)
        val intersection = intersect(circle1, circle2)
        Assertions.assertEquals(Intersection.Disjoint, intersection)
    }

    @Test
    fun circleCircleIntersect_disjoint_concentric() {
        val circle1 = Element.Circle(Point(1.0, 2.0), 1.0)
        val circle2 = Element.Circle(Point(1.0, 2.0), 2.0)
        val intersection = intersect(circle1, circle2)
        Assertions.assertEquals(Intersection.Disjoint, intersection)
    }

    @Test
    fun circleCircleIntersect_coincide() {
        val circle = Element.Circle(Point(1.0, 2.0), 1.0)
        val intersection = intersect(circle, circle)
        Assertions.assertEquals(Intersection.Coincide, intersection)
    }

    @Test
    fun circleLineIntersect() {
        val circle = Element.Circle(Point(1.0, 2.0), 3.0)
        val point1 = Point(-1.0, 2.0)
        val point2 = Point(3.0, 2.0)

        val expectedPoint1 = Point(-2.0, 2.0)
        val expectedPoint2 = Point(4.0, 2.0)

        // Forwards
        val line1 = Element.Line(point1, point2)
        val intersection1 = intersect(line1, circle)
        Assertions.assertEquals(Intersection.TwoPoints(expectedPoint1, expectedPoint2), intersection1)

        // Backwards - intersection points should swap
        val line2 = Element.Line(point2, point1)
        val intersection2 = intersect(line2, circle)
        Assertions.assertEquals(Intersection.TwoPoints(expectedPoint2, expectedPoint1), intersection2)
    }

    @Test
    fun linePointCoincideTest() {
        val basePoint = Point(0.01, 0.0)
        val basePoint2 = Point(1.0, 0.1)
        val center = Point(0.01, 2.000)
        val base = EuclideaTools.lineTool(basePoint, basePoint2)!!
        val perpendicularLine = EuclideaTools.perpendicularTool(base, center)!!

        fun test(point: Point, line: Element.Line, coincides: Boolean) {
            assertEquals(coincides, pointAndLineCoincide(point, line))
            assertEquals(coincides, pointAndElementCoincide(point, line))
        }

        test(basePoint, perpendicularLine, false)
        test(basePoint2, perpendicularLine, false)

        test(basePoint, base, true)
        test(basePoint2, base, true)
        test(center, perpendicularLine, true)

        val intersectionPoint = intersect(base, perpendicularLine).points().first()
        test(intersectionPoint, perpendicularLine, true)
    }

    @Test
    fun circlePointCoincideTest() {
        val center = Point(0.01, 2.000)
        val radius = 1.0
        val x1 = center.plus(Point(radius, 0.0))
        val x2 = center.plus(Point(-radius, 0.0))
        val y1 = center.plus(Point(0.0, radius))
        val y2 = center.plus(Point(0.0, -radius))

        val circle = EuclideaTools.circleTool(center, x1)!!

        fun test(point: Point, circle: Element.Circle, coincides: Boolean) {
            assertEquals(coincides, pointAndCircleCoincide(point, circle))
            assertEquals(coincides, pointAndElementCoincide(point, circle))
        }

        test(center, circle, false)
        test(x1, circle, true)
        test(x2, circle, true)
        test(y1, circle, true)
        test(y2, circle, true)

        test(x2.plus(Point(0.01, 0.0)), circle, false)
        test(x2.plus(Point(0.0, 0.01)), circle, false)
    }
}