import euclidea.*
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool

data class EuclideaContext(
    val points: List<Point>,
    val elements: List<Element>,
    val pointSource: Map<Point, Pair<Element, Element>> = mapOf()
) {
    fun nexts(): List<EuclideaContext> {
        val res = mutableListOf<EuclideaContext>()
        forEachPair(points) { point1, point2 ->
            listOf(lineTool(point1, point2), circleTool(point1, point2), circleTool(point2, point1)).forEach { e ->
                when (val next = e?.let { this.withElement(it) }) {
                    this, null -> {; }
                    else -> res.add(next)
                }
            }
        }
        return res.toList()
    }

    @Suppress("SuspiciousCollectionReassignment")
    private fun withElement(element: Element): EuclideaContext {
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
            EuclideaContext(updatedPoints, elements + element, updatedPointSource)
        }
    }

    fun hasElements(otherElements: List<Element>): Boolean {
        return otherElements.all { hasElement(it) }
    }

    private fun hasElement(element: Element): Boolean {
        return elements.any { e -> coincides(e, element) }
    }
}

