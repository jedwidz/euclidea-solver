package euclidea

data class EuclideaConfig(
    val lineToolEnabled: Boolean = true,
    val circleToolEnabled: Boolean = true,
    val maxSqDistance: Double = Double.MAX_VALUE
)

data class IntersectionSource(val element1: Element, val element2: Element, val intersection: Intersection)

data class EuclideaContext(
    val config: EuclideaConfig = EuclideaConfig(),
    val points: List<Point>,
    val elements: List<Element>,
    val pointSource: Map<Point, IntersectionSource> = mapOf()
) {

    @Suppress("SuspiciousCollectionReassignment")
    fun withElement(element: Element): EuclideaContext {
        return if (hasElement(element))
            this
        else {
            var updatedPoints = points
            var updatedPointSource = pointSource
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
            EuclideaContext(config, updatedPoints, elements + element, updatedPointSource)
        }
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
}

