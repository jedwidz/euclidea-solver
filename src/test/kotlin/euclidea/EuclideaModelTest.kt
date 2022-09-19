package euclidea

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class EuclideaModelTest {

//    @Test
//    fun lineIntersectAxes() {
//        val xAxis = Element.Line(Origin, Point(10.0, 0.0))
//        intersect(xAxis, yAxis)
//    }

    @Test
    fun circleCircleIntersect_twoPoints() {
        val circle1 = Element.Circle(Point(-1.0, 2.0), 2.0)
        val circle2 = Element.Circle(Point(1.0, 2.0), 2.0)
        val intersection = intersect(circle1, circle2)
        val s = sqrt(3.0)
        Assertions.assertEquals(Intersection.TwoPoints(Point(0.0, 2.0 + s), Point(0.0, 2.0 - s)), intersection)
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