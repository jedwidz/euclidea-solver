package euclidea

object EuclideaTools {

    fun lineTool(point1: Point, point2: Point): Element.Line? {
        return makeLine(point1, point2)
    }

    fun circleTool(center: Point, sample: Point): Element.Circle? {
        return makeCircle(center, distance(center, sample), sample)
    }

    fun perpendicularTool(line: Element.Line, point: Point): Element.Line? {
        val direction = line.point2.minus(line.point1)
        val point2 = Point(point.x + direction.y, point.y - direction.x)
        return makeLine(point, point2)
    }

    private fun makeLine(point1: Point, point2: Point): Element.Line? {
        return if (coincides(point1, point2)) null else Element.Line(point1, point2)
    }

    private fun makeCircle(center: Point, distance: Double, sample: Point): Element.Circle? {
        return if (distance <= 0.0) null else Element.Circle(center, distance, sample)
    }
}