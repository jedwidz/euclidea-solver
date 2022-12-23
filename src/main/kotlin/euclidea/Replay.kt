package euclidea

fun replaySteps(referenceContext: EuclideaContext, replayInitialContext: EuclideaContext): EuclideaContext {
    val fromReferencePoint = mutableMapOf<Point, Point>()
    val fromReferenceElement = mutableMapOf<Element, Element>()

    fun replayFail(message: String): Nothing {
        throw IllegalStateException(message)
    }

    fun unifyPoint(referencePoint: Point, replayPoint: Point) {
        val existing = fromReferencePoint.putIfAbsent(referencePoint, replayPoint)
        if (existing !== null) {
            if (!coincides(existing, replayPoint))
                replayFail("Failed to unify reference point $referencePoint with replay point $replayPoint: reference point already unified with $existing")
        }
    }

    fun unifyElement(referenceElement: Element, replayElement: Element) {
        when {
            referenceElement is Element.Circle && replayElement is Element.Circle -> {
                unifyPoint(referenceElement.center, replayElement.center)
            }
            referenceElement is Element.Line && replayElement is Element.Line -> {
                unifyPoint(referenceElement.point1, replayElement.point1)
                unifyPoint(referenceElement.point2, replayElement.point2)
            }
            else -> replayFail("Failed to unify reference point $referenceElement with replay point $replayElement: mismatched element type")
        }
        val existing = fromReferenceElement.putIfAbsent(referenceElement, replayElement)
        if (existing !== null) {
            if (!coincides(existing, replayElement))
                replayFail("Failed to unify reference element $referenceElement with replay element $replayElement: reference element already unified with $existing")
        }
    }

    fun unifyIntersection(referenceIntersection: Intersection, replayIntersection: Intersection) {
        when {
            referenceIntersection is Intersection.Disjoint && replayIntersection is Intersection.Disjoint -> {
                // OK, but not really expected in practice
            }
            referenceIntersection is Intersection.OnePoint && replayIntersection is Intersection.OnePoint -> {
                unifyPoint(referenceIntersection.point, replayIntersection.point)
            }
            referenceIntersection is Intersection.TwoPoints && replayIntersection is Intersection.TwoPoints -> {
                unifyPoint(referenceIntersection.point1, replayIntersection.point1)
                unifyPoint(referenceIntersection.point2, replayIntersection.point2)
            }
        }
    }

    fun replayPointFor(referencePoint: Point): Point {
        return fromReferencePoint.getOrElse(referencePoint) {
            replayFail("No replay point unified with reference point $referencePoint")
        }
    }

    fun replayElementFor(referenceElement: Element): Element {
        return fromReferenceElement.getOrElse(referenceElement) {
            replayFail("No replay element unified with reference element $referenceElement")
        }
    }

    fun replayLineFor(referenceLine: Element.Line): Element.Line {
        return replayElementFor(referenceLine) as Element.Line
    }

    fun findReplayPoint(referencePoint: Point): Point {
        val canonicalReferencePoint = referenceContext.canonicalPoint(referencePoint)
            ?: replayFail("Failed to find replay point for $referencePoint: no canonical reference point")
        return fromReferencePoint.getOrElse(canonicalReferencePoint) {
            when (val intersectionSource = referenceContext.pointSource[canonicalReferencePoint]) {
                null -> replayFail("Failed to find replay point for $canonicalReferencePoint: no reference point source")
                else -> with(intersectionSource) {
                    val replayElement1 = replayElementFor(element1)
                    val replayElement2 = replayElementFor(element2)
                    val replayIntersection = intersect(replayElement1, replayElement2)
                    unifyIntersection(intersection, replayIntersection)
                    replayPointFor(canonicalReferencePoint)
                }
            }
        }
    }

    fun generateReplayElement(referenceElement: Element): Element {
        return when (referenceElement) {
            is Element.Circle -> {
                val sample = referenceElement.sample
                if (sample === null)
                    replayFail("Failed to generate replay element for $referenceElement: no sample point on circle")
                val replayCenter = findReplayPoint(referenceElement.center)
                val replaySample = findReplayPoint(sample)
                val replayCircle = EuclideaTools.circleTool(replayCenter, replaySample)
                replayCircle
            }
            is Element.Line -> {
                when (val source = referenceElement.source) {
                    is LineSource.Perpendicular -> {
                        val replaySourceLine = replayLineFor(source.line)
                        val replaySourcePoint = findReplayPoint(source.point)
                        val replayLine = EuclideaTools.perpendicularTool(replaySourceLine, replaySourcePoint)
                        replayLine
                    }
                    is LineSource.PerpendicularBisect -> {
                        val replayPoint1 = findReplayPoint(source.point1)
                        val replayPoint2 = findReplayPoint(source.point2)
                        val replayLine = EuclideaTools.perpendicularBisectorTool(replayPoint1, replayPoint2)
                        replayLine
                    }
                    is LineSource.AngleBisect -> {
                        val replayPointA = findReplayPoint(source.pointA)
                        val replayPointO = findReplayPoint(source.pointO)
                        val replayPointB = findReplayPoint(source.pointB)
                        val replayLine = EuclideaTools.angleBisectorTool(replayPointA, replayPointO, replayPointB)
                        replayLine
                    }
                    null -> {
                        val replayPoint1 = findReplayPoint(referenceElement.point1)
                        val replayPoint2 = findReplayPoint(referenceElement.point2)
                        val replayLine = EuclideaTools.lineTool(replayPoint1, replayPoint2)
                        replayLine
                    }
                }
            }
        }
    }

    // Unify initial context
    val initialPairedPoints = referenceContext.points.zip(replayInitialContext.points)
    initialPairedPoints.forEach { (referencePoint, replayPoint): Pair<Point, Point> ->
        unifyPoint(referencePoint, replayPoint)
    }
    // val initialPairedReferencePoints = initialPairedPoints.map { it.first }.toSet()

    val initialPairedElements = referenceContext.elements.zip(replayInitialContext.elements)
    initialPairedElements.forEach { (referenceElement, replayElement): Pair<Element, Element> ->
        unifyElement(referenceElement, replayElement)
    }
    val initialPairedReferenceElements = initialPairedElements.map { it.first }.toSet()

    // Unify remaining steps
    var replayContext = replayInitialContext
    referenceContext.elements.forEach { referenceElement ->
        if (!initialPairedReferenceElements.contains(referenceElement)) {
            val existingReplayElement = fromReferenceElement[referenceElement]
            if (existingReplayElement !== null)
                replayFail("Reference element $referenceElement from new step already unified with $existingReplayElement")
            val newReplayElement = generateReplayElement(referenceElement)
            unifyElement(referenceElement, newReplayElement)
            replayContext = replayContext.withElement(newReplayElement)
        }
    }

    // TODO ? check all point sources are consistent?

    return replayContext
}
