import euclidea.Element
import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import euclidea.Point
import euclidea.forEachPair
import euclidea.intersect

data class EuclideaContext(val points: List<Point>, val elements: List<Element>) {
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

    private fun withElement(element: Element): EuclideaContext {
        return if (elements.contains(element))
            this
        else {
            val newPoints =
                elements.flatMap { e -> intersect(e, element).points() }.toSet().filter { !points.contains(it) }
            EuclideaContext(points + newPoints, elements + element)
        }
    }

    fun containsAll(otherElements: List<Element>): Boolean {
        return otherElements.all { elements.contains(it) }
    }
}

