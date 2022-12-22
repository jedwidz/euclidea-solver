package euclidea

typealias Segment = Two<Point>

data class SegmentWithLine(val segment: Segment, val line: Element.Line?)

sealed class SegmentOrCircle {
    data class Segment(val segment: euclidea.Segment) : SegmentOrCircle()
    data class Circle(val circle: Element.Circle) : SegmentOrCircle()
}

data class Coincidences(
    val distances: List<Pair<Double, List<SegmentOrCircle>>>,
    val headings: List<Pair<Double, List<SegmentWithLine>>>,
    val triangles: List<Pair<Three<Double>, List<Three<SegmentWithLine>>>>
)

fun EuclideaContext.coincidences(): Coincidences {
    return Coincidences(
        distances = distanceCoincidences(),
        headings = headingCoincidences(),
        triangles = triangleCoincidences()
    )
}

private fun EuclideaContext.triangleCoincidences(): List<Pair<Three<Double>, List<Three<SegmentWithLine>>>> {
    // Algorithm of complexity O(yikes!)
    val triangles = points.triples().map { pointsTriple ->
        val (a, b, c) = pointsTriple
        val angles = threeFrom(listOf(angle(a, b, c), angle(b, a, c), angle(a, c, b)).sorted())
        angles to pointsTriple
    }
        // Exclude degenerate triangles with three collinear points
        .filter { !coincidesRough(it.first.third, 180.0) }
    val contextLines = elements.filterIsInstance<Element.Line>()
    fun contextLineFor(point1: Point, point2: Point): SegmentWithLine {
        val contextLine = contextLines.firstOrNull { e ->
            val line = Element.Line(point1, point2)
            coincides(e, line)
        }
        return SegmentWithLine(point1 to point2, contextLine)
    }

    val res = mutableListOf<Pair<Three<Double>, List<Three<SegmentWithLine>>>>()
    for ((angle1, triangles2) in coalesceOnCoincide(triangles) { it to it.first.first }) {
        for ((angle2, triangles3) in coalesceOnCoincide(triangles2) { it to it.first.second }) {
            for ((angle3, triangles4) in coalesceOnCoincide(triangles3) { it to it.first.third }) {
                res.add(Triple(angle1, angle2, angle3) to triangles4.map { (_, points) ->
                    Triple(
                        contextLineFor(points.first, points.second),
                        contextLineFor(points.second, points.third),
                        contextLineFor(points.third, points.first),
                    )
                })
            }
        }
    }
    return res
}

private fun EuclideaContext.distanceCoincidences(): List<Pair<Double, List<SegmentOrCircle>>> {
    val contextCircles = elements.filterIsInstance<Element.Circle>()
    fun matchesContextCircle(segment: Segment): Boolean {
        fun matches(pointA: Point, pointB: Point): Boolean {
            val circle = EuclideaTools.circleTool(pointA, pointB)
            return contextCircles.any { coincides(it, circle) }
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

    return coalesceOnCoincide(segmentOrCircleToMeasure) { it }
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
    return coalesceOnCoincide(points.pairs()) { it to measureFor(it) }
}

private fun <T, R> coalesceOnCoincide(
    items: List<T>,
    resultAndMeasureFor: (T) -> Pair<R, Double>
): List<Pair<Double, List<R>>> {
    val itemToMeasure = items.map(resultAndMeasureFor).sortedBy { e -> e.second }
    val res = mutableMapOf<Double, List<R>>()
    val acc = mutableListOf<Pair<R, Double>>()
    fun cut() {
        if (acc.isNotEmpty()) {
            val size = acc.size
            if (size > 1) {
                val middleMeasure = acc[size / 2].second
                res[middleMeasure] = acc.map { it.first }
            }
            acc.clear()
        }
    }

    var prevMeasure: Double? = null
    for ((item, measure) in itemToMeasure) {
        if (prevMeasure?.let { coincidesRough(it, measure) } == true)
            acc.add(item to measure)
        else cut()
        prevMeasure = measure
    }
    cut()
    return res.toList()
}
