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

    fun dropPerpendicular(
        linePoint1: Point,
        linePoint2: Point,
        point: Point
    ): Pair<Element.Line, List<Element.Circle>>? {
        val circle1 = circleTool(linePoint1, point) ?: return null
        val circle2 = circleTool(linePoint2, point) ?: return null
        val other = intersectTwoPointsOther(circle1, circle2, point)
        return lineTool(point, other)?.let { it to listOf(circle1, circle2) }
    }

    fun bisect(point1: Point, point2: Point): Pair<Element.Line, List<Element.Circle>>? {
        val circle1 = circleTool(point1, point2) ?: return null
        val circle2 = circleTool(point2, point1) ?: return null
        val (cross1, cross2) = intersectTwoPoints(circle1, circle2)
        return lineTool(cross1, cross2)?.let { it to listOf(circle1, circle2) }
    }

    private fun makeLine(point1: Point, point2: Point): Element.Line? {
        return if (coincides(point1, point2)) null else Element.Line(point1, point2)
    }

    private fun makeCircle(center: Point, distance: Double, sample: Point): Element.Circle? {
        return if (distance <= 0.0) null else Element.Circle(center, distance, sample)
    }
}