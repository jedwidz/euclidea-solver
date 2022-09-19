package euclidea

import euclidea.Point.Companion.Origin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuclideaModelTest {

//    @Test
//    fun lineIntersectAxes() {
//        val xAxis = Element.Line(Origin, Point(10.0, 0.0))
//        intersect(xAxis, yAxis)
//    }

    @Test
    fun circleLineIntersect() {
        val circle = Element.Circle(Point(1.0, 2.0), 3.0)
        val line = Element.Line(Point(-1.0, 2.0), Point(3.0, 2.0))
        val intersection = intersect(line, circle)
        Assertions.assertEquals(Intersection.TwoPoints(Point(-2.0,2.0),Point(4.0,2.0)), intersection)
    }

}