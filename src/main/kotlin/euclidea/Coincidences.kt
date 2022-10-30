package euclidea

typealias Segment = Pair<Point, Point>

data class SegmentWithLine(val segment: Segment, val line: Element.Line?)

sealed class SegmentOrCircle {
    data class Segment(val segment: euclidea.Segment) : SegmentOrCircle()
    data class Circle(val circle: Element.Circle) : SegmentOrCircle()
}

data class Coincidences(
    val distances: List<Pair<Double, List<SegmentOrCircle>>>,
    val headings: List<Pair<Double, List<SegmentWithLine>>>
)

fun EuclideaContext.coincidences(): Coincidences {
    return Coincidences(distances = distanceCoincidences(), headings = headingCoincidences())
}

private fun EuclideaContext.distanceCoincidences(): List<Pair<Double, List<SegmentOrCircle>>> {
    val contextCircles = elements.filterIsInstance<Element.Circle>()
    fun matchesContextCircle(segment: Segment): Boolean {
        fun matches(pointA: Point, pointB: Point): Boolean {
            return EuclideaTools.circleTool(pointA, pointB)?.let { circle ->
                contextCircles.any { coincides(it, circle) }
            } ?: false
        }
        return matches(segment.first, segment.second) || matches(segment.second, segment.first) || matches(
            midpoint(
                segment.first,
                segment.second
            ), segment.first
        )
    }

    val segmentToMeasure =
        points.pairs().map { pair -> pair to distance(pair.first, pair.second) }
            .filter { !matchesContextCircle(it.first) }
    val segmentOrCircleToMeasure = (segmentToMeasure.map { SegmentOrCircle.Segment(it.first) to it.second }
            + contextCircles.map { SegmentOrCircle.Circle(it) to it.radius }
            + contextCircles.map { SegmentOrCircle.Circle(it) to it.radius * 2.0 })
        .sortedBy { e -> e.second }
    val res = mutableMapOf<Double, List<SegmentOrCircle>>()
    val acc = mutableListOf<Pair<SegmentOrCircle, Double>>()
    fun cut() {
        if (acc.isNotEmpty()) {
            val size = acc.size
            if (size > 1) {
                val middleMeasure = acc[size / 2].second
                val segments = acc.map { it.first }
                res[middleMeasure] = segments
            }
            acc.clear()
        }
    }

    var prevDistance: Double? = null
    for ((segmentOrCircle, distance) in segmentOrCircleToMeasure) {
        if (prevDistance?.let { coincides(it, distance) } == true)
            acc.add(segmentOrCircle to distance)
        else cut()
        prevDistance = distance
    }
    cut()
    return res.toList()
}

private fun EuclideaContext.headingCoincidences(): List<Pair<Double, List<SegmentWithLine>>> {
    val res = mutableListOf<Pair<Double, List<SegmentWithLine>>>()
    for ((heading, segments) in segmentCoincidences { segment -> heading(segment.first, segment.second) }) {
        val filteredSegments = mutableListOf<SegmentWithLine>()
        var remainingLines = segments.map { lineFor(it) }
        while (remainingLines.isNotEmpty()) {
            val line = remainingLines.first()
            val contextLine = elements.filterIsInstance<Element.Line>().firstOrNull { e -> coincides(e, line) }
            remainingLines = remainingLines.filter { !coincides(line, it) }
            filteredSegments.add(SegmentWithLine(segmentFor(line), contextLine))
        }
        if (filteredSegments.size > 1)
            res.add(heading to filteredSegments)
    }
    return res
}

fun lineFor(segment: Segment): Element.Line {
    return Element.Line(segment.first, segment.second)
}

fun segmentFor(line: Element.Line): Segment {
    return line.point1 to line.point2
}

private fun EuclideaContext.segmentCoincidences(measureFor: (Segment) -> Double): List<Pair<Double, List<Segment>>> {
    val segmentToMeasure =
        points.pairs().map { pair -> pair to measureFor(pair) }.sortedBy { e -> e.second }
    val res = mutableMapOf<Double, List<Segment>>()
    val acc = mutableListOf<Pair<Segment, Double>>()
    fun cut() {
        if (acc.isNotEmpty()) {
            val size = acc.size
            if (size > 1) {
                val middleMeasure = acc[size / 2].second
                val segments = acc.map { it.first }
                res[middleMeasure] = segments
            }
            acc.clear()
        }
    }

    var prevMeasure: Double? = null
    for ((pair, measure) in segmentToMeasure) {
        if (prevMeasure?.let { coincides(it, measure) } == true)
            acc.add(pair to measure)
        else cut()
        prevMeasure = measure
    }
    cut()
    return res.toList()
}
