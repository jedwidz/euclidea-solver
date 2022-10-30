package euclidea

typealias Segment = Pair<Point, Point>

fun EuclideaContext.coincidences(): Coincidences {
    return Coincidences(distances = distanceCoincidences(), headings = headingCoincidences())
}

private fun EuclideaContext.distanceCoincidences(): List<Pair<Double, List<Segment>>> {
    return segmentCoincidences { segment -> distance(segment.first, segment.second) }
}

private fun EuclideaContext.headingCoincidences(): List<Pair<Double, List<Segment>>> {
    val res = mutableListOf<Pair<Double, List<Segment>>>()
    for ((heading, segments) in segmentCoincidences { segment -> heading(segment.first, segment.second) }) {
        val filteredSegments = mutableListOf<Segment>()
        var remainingLines = segments.map { lineFor(it) }
        while (remainingLines.isNotEmpty()) {
            val line = remainingLines.first()
            remainingLines = remainingLines.filter { !coincides(line, it) }
            filteredSegments.add(segmentFor(line))
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

data class Coincidences(
    val distances: List<Pair<Double, List<Segment>>>,
    val headings: List<Pair<Double, List<Segment>>>
)
