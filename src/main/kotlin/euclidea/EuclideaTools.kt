package euclidea

object EuclideaTools {

    fun lineTool(point1: Point, point2: Point): Element.Line? {
        return makeLine(point1, point2)
    }

    fun circleTool(center: Point, sample: Point): Element.Circle? {
        return makeCircle(center, distance(center, sample))
    }

    private fun makeLine(point1: Point, point2: Point): Element.Line? {
        return if (coincides(point1, point2)) null else Element.Line(point1, point2)
    }

    private fun makeCircle(center: Point, distance: Double): Element.Circle? {
        return if (distance <= 0.0) null else Element.Circle(center, distance)
    }
}