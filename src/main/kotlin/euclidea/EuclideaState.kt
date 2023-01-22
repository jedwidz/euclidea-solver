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

    fun restrictConfig(enabledToolSet: Set<EuclideaTool>): EuclideaConfig {
        return EuclideaConfig(
            lineToolEnabled = lineToolEnabled && EuclideaTool.LineTool in enabledToolSet,
            circleToolEnabled = circleToolEnabled && EuclideaTool.CircleTool in enabledToolSet,
            perpendicularToolEnabled = perpendicularToolEnabled && EuclideaTool.PerpendicularTool in enabledToolSet,
            perpendicularBisectorToolEnabled = perpendicularBisectorToolEnabled && EuclideaTool.PerpendicularBisectorTool in enabledToolSet,
            angleBisectorToolEnabled = angleBisectorToolEnabled && EuclideaTool.AngleBisectorTool in enabledToolSet,
            parallelToolEnabled = parallelToolEnabled && EuclideaTool.ParallelTool in enabledToolSet,
            nonCollapsingCompassToolEnabled = nonCollapsingCompassToolEnabled && EuclideaTool.NonCollapsingCompassTool in enabledToolSet,
            maxSqDistance = maxSqDistance
        )
    }
}

data class IntersectionSource(val element1: Element, val element2: Element, val intersection: Intersection)

// Should use `EuclideaContext.of` rather than primary constructor, in order to include intersection points of initial elements.
data class EuclideaContext private constructor(
    val config: EuclideaConfig = EuclideaConfig(),
    val elements: List<Element>,
    private val pointsInfo: PointsInfo
) {
    val points
        get() = pointsInfo.points
    val pointSource
        get() = pointsInfo.pointSource

    companion object {
        fun of(
            config: EuclideaConfig = EuclideaConfig(),
            points: List<Point>,
            elements: List<Element>
        ): EuclideaContext {
            return EuclideaContext(config, listOf(), PointsInfo.Strict(points)).withElements(elements)
        }

        private sealed class PointsInfo {
            abstract val points: List<Point>
            abstract val pointSource: Map<Point, IntersectionSource>

            data class Strict(
                override val points: List<Point>,
                override val pointSource: Map<Point, IntersectionSource> = mapOf()
            ) : PointsInfo()

            class Lazy(
                previousContext: EuclideaContext,
                newElement: Element
            ) : PointsInfo() {
                val delegate by lazy { previousContext.updatedPointsInfo(newElement) }
                override val points
                    get() = delegate.points
                override val pointSource
                    get() = delegate.pointSource
            }
        }
    }

    fun withElement(element: Element): EuclideaContext {
        return if (hasElement(element))
            this
        else
            EuclideaContext(config, elements + element, PointsInfo.Lazy(this, element))
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
        return PointsInfo.Strict(updatedPoints, updatedPointSource)
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
