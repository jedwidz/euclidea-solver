package euclidea

typealias Segment = Pair<Point, Point>

fun EuclideaContext.coincidences(): Coincidences {
    val distances = distanceCoincidences()
    return Coincidences(distances)
}

private fun EuclideaContext.distanceCoincidences(): List<Pair<Double, List<Segment>>> {
    val segmentToDistance =
        points.pairs().map { pair -> pair to distance(pair.first, pair.second) }.sortedBy { e -> e.second }
    val res = mutableMapOf<Double, List<Segment>>()
    val acc = mutableListOf<Pair<Segment, Double>>()
    fun cut() {
        if (acc.isNotEmpty()) {
            val size = acc.size
            if (size > 1) {
                val middleDistance = acc[size / 2].second
                val segments = acc.map { it.first }
                res[middleDistance] = segments
            }
            acc.clear()
        }
    }

    var prevDistance: Double? = null
    for ((pair, distance) in segmentToDistance) {
        if (prevDistance?.let { coincides(it, distance) } == true)
            acc.add(pair to distance)
        else cut()
        prevDistance = distance
    }
    cut()
    val distances = res.toList()
    return distances
}

data class Coincidences(val distances: List<Pair<Double, List<Segment>>>)
