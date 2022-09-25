package euclidea

import euclidea.Point.Companion.Origin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

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
        Assertions.assertEquals(Intersection.TwoPoints(Point(0.0, 2.0 + s), Point(0.0, 2.0 - s)), intersection)
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
        val line = Element.Line(Point(-1.0, 2.0), Point(3.0, 2.0))
        val intersection = intersect(line, circle)
        Assertions.assertEquals(Intersection.TwoPoints(Point(-2.0, 2.0), Point(4.0, 2.0)), intersection)
    }

}