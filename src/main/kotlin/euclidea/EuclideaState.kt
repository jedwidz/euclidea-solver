package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

data class EuclideaConfig(
    val lineToolEnabled: Boolean = true,
    val circleToolEnabled: Boolean = true,
    val maxSqDistance: Double = Double.MAX_VALUE
)

data class EuclideaContext(
    val config: EuclideaConfig = EuclideaConfig(),
    val points: List<Point>,
    val elements: List<Element>,
    val pointSource: Map<Point, Pair<Element, Element>> = mapOf(),
    val oldPoints: Set<Point> = setOf(),
    val pendingElements: Set<Element> = setOf()
) {
    fun nexts(): List<EuclideaContext> {
        val newPoints = points.filter { it !in oldPoints }
        val nextOldPoints = oldPoints + newPoints

        val newElements = mutableListOf<Element>()
        fun tryAdd(e: Element?) {
            if (e !== null && !hasElement(e))
                newElements.add(e)
        }

        fun visit(point1: Point, point2: Point) {
            if (config.lineToolEnabled)
                tryAdd(lineTool(point1, point2))
            if (config.circleToolEnabled) {
                tryAdd(circleTool(point1, point2))
                tryAdd(circleTool(point2, point1))
            }
        }
        newPoints.forEachIndexed { i, newPoint ->
            oldPoints.forEach { visit(newPoint, it) }
            for (j in i + 1 until newPoints.size)
                visit(newPoint, newPoints[j])
        }

        val newPendingElements = pendingElements + newElements

        val res = mutableListOf<EuclideaContext>()
        var nextPendingElements = newPendingElements
        newPendingElements.forEach { newElement ->
            nextPendingElements = nextPendingElements.minus(newElement)
            res.add(this.withElement(newElement).withSearchState(nextOldPoints, nextPendingElements))
        }
        return res.toList()
    }

    private fun withSearchState(nextOldPoints: Set<Point>, nextPendingElements: Set<Element>): EuclideaContext {
        return EuclideaContext(config, points, elements, pointSource, nextOldPoints, nextPendingElements)
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
                    if (point.sqDistance < config.maxSqDistance)
                        if (updatedPoints.none { p -> coincides(p, point) }) {
                            updatedPoints += point
                            updatedPointSource += point to Pair(e, element)
                        }
                }
            }
            EuclideaContext(config, updatedPoints, elements + element, updatedPointSource, oldPoints, pendingElements)
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
}

