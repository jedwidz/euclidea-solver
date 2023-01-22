package euclidea

data class EuclideaConfig(
    val lineToolEnabled: Boolean = true,
    val circleToolEnabled: Boolean = true,
    // TODO be consistent about default enablement
    val perpendicularToolEnabled: Boolean = false,
    val perpendicularBisectorToolEnabled: Boolean = false,
    val angleBisectorToolEnabled: Boolean = false,
    val parallelToolEnabled: Boolean = false,
    val nonCollapsingCompassToolEnabled: Boolean = false,
    val maxSqDistance: Double = Double.MAX_VALUE
) {
    val anyTwoPointToolEnabled: Boolean = lineToolEnabled || circleToolEnabled || perpendicularBisectorToolEnabled
    val anyLinePointToolEnabled: Boolean = perpendicularToolEnabled || parallelToolEnabled
    val anyThreePointToolEnabled: Boolean = angleBisectorToolEnabled || nonCollapsingCompassToolEnabled
}

data class IntersectionSource(val element1: Element, val element2: Element, val intersection: Intersection)

private data class PointsInfo(
    val points: List<Point>,
    val pointSource: Map<Point, IntersectionSource> = mapOf()
)

// Should use `EuclideaContext.of` rather than primary constructor, in order to include intersection points of initial elements.
data class EuclideaContext private constructor(
    val config: EuclideaConfig = EuclideaConfig(),
    val elements: List<Element>,
    private val pointsInfo: PointsInfo
) {
    val points: List<Point> = pointsInfo.points
    val pointSource: Map<Point, IntersectionSource> = pointsInfo.pointSource

    companion object {
        fun of(
            config: EuclideaConfig = EuclideaConfig(),
            points: List<Point>,
            elements: List<Element>
        ): EuclideaContext {
            return EuclideaContext(config, listOf(), PointsInfo(points)).withElements(elements)
        }
    }

    fun withElement(element: Element): EuclideaContext {
        return if (hasElement(element))
            this
        else
            EuclideaContext(config, elements + element, updatedPointsInfo(element))
    }

    private fun updatedPointsInfo(element: Element): PointsInfo {
        val updatedPoints = points.toMutableList()
        val updatedPointSource = pointSource.toMutableMap()
        for (e in elements) {
            val intersection = intersect(e, element)
            for (point in intersection.points()) {
                if (point.sqDistance < config.maxSqDistance)
                    if (updatedPoints.none { p -> coincides(p, point) }) {
                        updatedPoints += point
                        updatedPointSource += point to IntersectionSource(e, element, intersection)
                    }
            }
        }
        return PointsInfo(updatedPoints, updatedPointSource)
    }

    fun withElements(elements: List<Element>): EuclideaContext {
        return elements.fold(this) { acc, element -> acc.withElement(element) }
    }

    fun hasElements(otherElements: List<Element>): Boolean {
        return otherElements.all { hasElement(it) }
    }

    fun hasElement(element: Element): Boolean {
        return elements.any { e -> coincides(e, element) }
    }

    fun hasPoint(point: Point): Boolean {
        return points.any { p -> coincides(p, point) }
    }

    fun hasPoints(otherPoints: List<Point>): Boolean {
        return otherPoints.all { hasPoint(it) }
    }

    fun canonicalPoint(point: Point): Point? {
        // Linear lookup, maybe should be more efficient
        return points.firstOrNull() { coincides(it, point) }
    }

    fun constructionPointSet(): PointSet {
        val res = PointSet()
        res += elements.flatMap { element -> element.constructionPoints() }
        return res
    }

    fun constructionElementSet(): ElementSet {
        val res = ElementSet()
        res += elements.flatMap { element -> element.constructionElements() }
        return res
    }
}
