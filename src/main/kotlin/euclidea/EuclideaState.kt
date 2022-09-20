package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

data class EuclideaConfig(
    val lineToolEnabled: Boolean = true,
    val circleToolEnabled: Boolean = true
)

data class EuclideaSignature(
    val pointCount: Int,
    val points: Set<PointSignature>,
    val elementCount: Int,
    val elements: Set<ElementSignature>
)

data class EuclideaContext(
    val config: EuclideaConfig = EuclideaConfig(),
    val points: List<Point>,
    val elements: List<Element>,
    val pointSource: Map<Point, Pair<Element, Element>> = mapOf()
) {
    val signature = EuclideaSignature(
        points.size,
        points.map { it.signature }.toSet(),
        elements.size,
        elements.map { it.signature }.toSet()
    )

    fun nexts(): List<EuclideaContext> {
        val res = mutableListOf<EuclideaContext>()
        fun tryAdd(e: Element?) {
            when (val next = e?.let { this.withElement(it) }) {
                this, null -> {; }
                else -> res.add(next)
            }
        }
        forEachPair(points) { point1, point2 ->
            if (config.lineToolEnabled)
                tryAdd(lineTool(point1, point2))
            if (config.circleToolEnabled) {
                tryAdd(circleTool(point1, point2))
                tryAdd(circleTool(point2, point1))
            }
        }
        return res.toList()
    }

    @Suppress("SuspiciousCollectionReassignment")
    fun withElement(element: Element): EuclideaContext {
        return if (hasElement(element))
            this
        else {
            var updatedPoints = points
            var updatedPointSource = pointSource
            for (e in elements) {
                for (point in intersect(e, element).points()) {
                    if (updatedPoints.none { p -> coincides(p, point) }) {
                        updatedPoints += point
                        updatedPointSource += point to Pair(e, element)
                    }
                }
            }
            EuclideaContext(config, updatedPoints, elements + element, updatedPointSource)
        }
    }

    fun hasElements(otherElements: List<Element>): Boolean {
        return otherElements.all { hasElement(it) }
    }

    fun hasElement(element: Element): Boolean {
        return elements.any { e -> coincides(e, element) }
    }
}

