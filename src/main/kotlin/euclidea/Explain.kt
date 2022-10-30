package euclidea

class Namer {
    private val names: MutableMap<Any, String> = mutableMapOf()

    fun <T : Any> set(name: String, named: T): T {
        names[named] = name
        return named
    }

    fun <T : Pair<Any, Any>> setAll(name1: String, name2: String, namedPair: T): T {
        names[namedPair.first] = name1
        names[namedPair.second] = name2
        return namedPair
    }

    fun get(named: Any): String? {
        return names[named]
    }
}

fun dumpSolution(solutionContext: EuclideaContext?, namer: Namer = Namer()) {
    println(solutionContext)
    solutionContext?.let { printSteps(it, namer) }
}

private class Labeler<T : Any>(val prefix: String, val namer: Namer) {
    private val tags = mutableMapOf<T, Int>()
    private var nextTag = 1

    fun label(item: T, newAction: ((String) -> Unit)? = null): String {
        return when (val tag = tags[item]) {
            null -> {
                val newTag = nextTag++
                tags[item] = newTag
                val label = labelFor(newTag, nameOf(item))
                newAction?.let { it(label) }
                label
            }
            else -> labelFor(tag, nameOf(item))
        }
    }

    private fun nameOf(item: T) = namer.get(item)

    private fun labelFor(tag: Int, name: String?): String {
        return "${prefix}${tag}${name?.let { "_${it}" } ?: ""}"
    }
}

private fun printSteps(context: EuclideaContext, namer: Namer) {
    with(context) {
        object {
            val pointLabeler = Labeler<Point>("point", namer)
            val circleLabeler = Labeler<Element.Circle>("circle", namer)
            val lineLabeler = Labeler<Element.Line>("line", namer)

            fun elementLabel(element: Element): String {
                return when (element) {
                    is Element.Circle -> explainCircle(element)
                    is Element.Line -> explainLine(element)
                }
            }

            fun explainPoint(point: Point): String {
                return pointLabeler.label(point) { pointLabel ->
                    val coordinates = "(${point.x}, ${point.y})"
                    when (val intersectionSource = pointSource[point]) {
                        null -> println("$pointLabel at $coordinates")
                        else -> {
                            val elementLabel1 = elementLabel(intersectionSource.element1)
                            val elementLabel2 = elementLabel(intersectionSource.element2)
                            val points = intersectionSource.intersection.points()
                            val num = points.indexOf(point) + 1
                            val count = points.size
                            println("$pointLabel at intersection ($num/$count) of $elementLabel1 and $elementLabel2 $coordinates")
                        }
                    }
                }
            }

            fun explainCircle(circle: Element.Circle): String {
                val centerLabel = explainPoint(circle.center)
                return circleLabeler.label(circle) { circleLabel ->
                    when (val sample = circle.sample) {
                        null -> println("$circleLabel with center $centerLabel and radius ${circle.radius}")
                        else -> {
                            val sampleLabel = explainPoint(sample)
                            println("$circleLabel with center $centerLabel extending to $sampleLabel")
                        }
                    }
                }
            }

            fun explainLine(line: Element.Line): String {
                val point1Label = explainPoint(line.point1)
                val point2Label = explainPoint(line.point2)
                return lineLabeler.label(line) { lineLabel ->
                    println("$lineLabel from $point1Label to $point2Label")
                }
            }

            private fun explainSteps() {
                for (element in elements) {
                    when (element) {
                        is Element.Circle -> explainCircle(element)
                        is Element.Line -> explainLine(element)
                    }
                }
            }

            private fun explainSegment(segment: Segment): String {
                val point1Label = explainPoint(segment.first)
                val point2Label = explainPoint(segment.second)
                return "$point1Label to $point2Label"
            }

            private fun explainSegmentWithLine(segmentWithLine: SegmentWithLine): String {
                val segment = explainSegment(segmentWithLine.segment)
                return when (val line = segmentWithLine.line) {
                    null -> segment
                    else -> "${explainLine(line)} ($segment)"
                }
            }

            private fun explainSegmentOrCircle(segmentOrCircle: SegmentOrCircle): String {
                return when (segmentOrCircle) {
                    is SegmentOrCircle.Circle -> explainCircle(segmentOrCircle.circle)
                    is SegmentOrCircle.Segment -> explainSegment(segmentOrCircle.segment)
                }
            }

            private fun reportCoincidences() {
                val coincidences = context.coincidences()
                for ((distance, segmentOrCircles) in coincidences.distances) {
                    println("Segments with distance and circles with radius $distance:")
                    for (segmentOrCircle in segmentOrCircles) {
                        println(explainSegmentOrCircle(segmentOrCircle))
                    }
                    println()
                }
                for ((heading, segmentsWithLine) in coincidences.headings) {
                    println("Segment or lines with heading $heading:")
                    for (segmentWithLine in segmentsWithLine) {
                        println(explainSegmentWithLine(segmentWithLine))
                    }
                    println()
                }
                for ((angles, triangles) in coincidences.triangles) {
                    println("Triangles with angles $angles:")
                    for (triangle in triangles.toList()) {
                        println(triangle.toList().map { explainSegmentWithLine(it) })
                    }
                    println()
                }
            }

            fun explain() {
                explainSteps()
                println()
                reportCoincidences()
            }
        }.explain()
    }
}
