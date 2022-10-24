package euclidea

import euclidea.EuclideaTools.circleTool
import euclidea.EuclideaTools.lineTool
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class RoughSolveTest {
    @Test
    fun alphaTutorial1() {
        // Equilateral Triangle
        // TODO line segment rather than infinite line
        val point1 = Point.Origin
        val point2 = Point(1.0, 0.0)
        val line = Element.Line(point1, point2)
        val solutions = run {
            val circle1 = circleTool(point1, point2)!!
            val circle2 = circleTool(point2, point1)!!
            intersect(circle1, circle2).points().map { pointX ->
                listOf(point1, point2).map { pointA -> lineTool(pointA, pointX)!! }
            }
        }
        val solutionContext =
            solve(EuclideaContext(points = listOf(point1, point2), elements = listOf(line)), 4) { context ->
                solutions.any { context.hasElements(it) }
            }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7() {
        // Drop a Perpendicular**
        // (lines only)
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        val solution = Element.Line(Point(0.0, 0.0), Point(0.0, 1.0))
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 7) { context ->
            context.hasElement(solution)
        }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_point() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a point on the vertical line, rather than the vertical line itself, saving one ply
        // >> useful partial solution at depth 7 (~23 sec)
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        // Extra parallel line, not necessarily part of optimal solution but let's see...
        val bonusLine = Element.Line(Point(0.0, 2.0), Point(1.0, 2.0))
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(50.0)),
            points = listOf(center),
            elements = listOf(circle, line, bonusLine)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 7) { context ->
            context.points.any { point -> coincides(point.x, 0.0) && !coincides(point.y, 2.0) }
        }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_check_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Reproduce and check my best solution so far
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.0, 2.0))
        val radius = 1.0
        val probeLineIntercept = namer.set("probeIntercept", Point(-0.943215, 0.0))
        val probePoint = namer.set("probe", Point(-0.828934, 3.0))
        val solutionContext =
            puzzle15_7_solution11E(center, radius, basePoint, basePoint2, probeLineIntercept, probePoint, namer)

        dumpSolution(solutionContext, namer)
        val checkSolutionLine = Element.Line(center, basePoint)
        assertTrue(solutionContext.hasElement(checkSolutionLine))
    }

    @Test
    fun puzzle15_7_replay_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Test replaying a solution, with different starting points
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.0, 2.0))
        val radius = 1.0
        val probeLineIntercept = namer.set("probeIntercept", Point(-0.943215, 0.0))
        val probePoint = namer.set("probe", Point(-0.828934, 3.0))
        val solutionContext =
            puzzle15_7_solution11E(center, radius, basePoint, basePoint2, probeLineIntercept, probePoint, namer)

        // Double-check that solution works
        val checkSolutionLine = Element.Line(center, basePoint)
        assertTrue(solutionContext.hasElement(checkSolutionLine))

        val replayBasePoint = namer.set("base", Point(0.0, 0.0))
        val replayBasePoint2 = namer.set("base2", Point(1.0, 0.0))
        val replayCenter = namer.set("center", Point(0.0, 2.0))
        val replayRadius = 1.0
        val replayProbeLineIntercept = namer.set("probeIntercept", Point(-0.943215, 0.0))
        val replayProbePoint = namer.set("probe", Point(-0.828934, 3.0))
        val (_, replayInitialContext) = puzzle15_7_probeLineContext(
            replayCenter,
            replayRadius,
            replayBasePoint,
            replayBasePoint2,
            replayProbeLineIntercept,
            replayProbePoint,
            namer
        )

        val replaySolutionContext =
            replaySteps(solutionContext, replayInitialContext)

        dumpSolution(replaySolutionContext, namer)
        val replayCheckSolutionLine = Element.Line(replayCenter, replayBasePoint)
        assertTrue(replaySolutionContext.hasElement(replayCheckSolutionLine))
    }

    @Test
    fun puzzle15_7_improve_solution() {
        // Drop a Perpendicular**
        // (lines only)
        // Try to improve on my best solution so far
        val namer = Namer()
        val basePoint = namer.set("base", Point(0.0, 0.0))
        val basePoint2 = namer.set("base2", Point(1.0, 0.0))
        val center = namer.set("center", Point(0.0, 2.0))
        val radius = 1.0
        val (_, _, initialContext) = puzzle15_7_initialContext(center, radius, basePoint, basePoint2, namer)

        val probeLineIntercept = namer.set("probeIntercept", Point(-0.943215, 0.0))
        val probePoint = namer.set("probe", Point(-0.828934, 3.0))
        val sampleSolutionContext =
            puzzle15_7_solution11E(center, radius, basePoint, basePoint2, probeLineIntercept, probePoint, namer)

        val probeLine = namer.set("probe", Element.Line(probeLineIntercept, probePoint))
        val startingContext = initialContext.withElement(probeLine)

        // 4 - nothing (23 min)
        val maxExtraElements = 5
        val solutionContext = solve(startingContext, 10 - 1 - 1, prune = { next ->
            next.elements.count { !sampleSolutionContext.hasElement(it) } > maxExtraElements
        }) { context ->
            context.points.any { point -> coincides(point.x, 0.0) && !coincides(point.y, 2.0) }
        }
        dumpSolution(solutionContext, namer)
    }

    private fun puzzle15_7_solution11E(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        probeLineIntercept: Point,
        probePoint: Point,
        namer: Namer
    ): EuclideaContext {
        val (temp, probeLineContext) = puzzle15_7_probeLineContext(
            center,
            radius,
            basePoint,
            basePoint2,
            probeLineIntercept,
            probePoint,
            namer
        )
        val (circle, line, probeLine) = temp
        // Solution works regardless of point 'order' here
        val (xPoint1, xPoint2) = namer.setAll("x1", "x2", intersectTwoPoints(circle, probeLine))
        val xLine1 = namer.set("x1", Element.Line(center, xPoint1))
        val xLine2 = namer.set("x2", Element.Line(center, xPoint2))
        val xPoint3 = namer.set("x3", intersectTwoPointsOther(circle, xLine1, xPoint1))
        val xPoint4 = namer.set("x4", intersectTwoPointsOther(circle, xLine2, xPoint2))
        val probeLineOpp = namer.set("probeOpp", Element.Line(xPoint3, xPoint4))
        val pivotLine = namer.set("pivot", Element.Line(center, probeLineIntercept))
        val pivotOppPoint = namer.set("probeOpp", intersectOnePoint(pivotLine, probeLineOpp))
        val apexPoint = namer.set("apex", intersectOnePoint(xLine2, line))
        val adjacentLine = namer.set("adjacent", Element.Line(pivotOppPoint, apexPoint))
        val farPoint = namer.set("far", intersectOnePoint(adjacentLine, probeLine))
        val probeLineOppIntercept = namer.set("probeOppIntercept", intersectOnePoint(probeLineOpp, line))
        val crossLine = namer.set("cross", Element.Line(farPoint, probeLineOppIntercept))
        val middlePoint = namer.set("middle", intersectOnePoint(crossLine, pivotLine))
        val otherLine = namer.set("other", Element.Line(middlePoint, apexPoint))
        val nextPoint = namer.set("next", intersectOnePoint(otherLine, probeLineOpp))
        val parallelLine = namer.set("parallel", Element.Line(middlePoint, xPoint2))
        val symmetricalPoint = namer.set("symmetrical", intersectTwoPointsOther(circle, parallelLine, xPoint2))
        val symmetricalLine = namer.set("symmetrical", Element.Line(symmetricalPoint, nextPoint))
        val topPoint = namer.set("top", intersectOnePoint(symmetricalLine, probeLine))
        val solutionLine = namer.set("solution", Element.Line(topPoint, center))

        val solutionContext = probeLineContext.withElements(
            listOf(
                xLine1,
                xLine2,
                probeLineOpp,
                pivotLine,
                adjacentLine,
                crossLine,
                otherLine,
                parallelLine,
                symmetricalLine,
                solutionLine
            )
        )
        return solutionContext
    }

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

    private fun puzzle15_7_initialContext(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        namer: Namer
    ): Triple<Element.Circle, Element.Line, EuclideaContext> {
        val circle = namer.set("circle", Element.Circle(center, radius))
        val line = namer.set("base", Element.Line(basePoint, basePoint2))
        val baseContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false, maxSqDistance = sq(50.0)),
            points = listOf(center),
            elements = listOf(circle, line)
        )
        return Triple(circle, line, baseContext)
    }

    private fun puzzle15_7_probeLineContext(
        center: Point,
        radius: Double,
        basePoint: Point,
        basePoint2: Point,
        probeLineIntercept: Point,
        probePoint: Point,
        namer: Namer
    ): Pair<Triple<Element.Circle, Element.Line, Element.Line>, EuclideaContext> {
        val (circle, line, initialContext) = puzzle15_7_initialContext(center, radius, basePoint, basePoint2, namer)
        // 'probe' line to cut across the circle and line.
        val probeLine = namer.set("probe", Element.Line(probeLineIntercept, probePoint))
        val probeLineContext = initialContext.withElement(probeLine)
        return Pair(Triple(circle, line, probeLine), probeLineContext)
    }

    private fun intersectOnePoint(element1: Element, element2: Element): Point =
        when (val i = intersect(element1, element2)) {
            is Intersection.OnePoint -> i.point
            else -> error("One intersection point expected: $i")
        }

    private fun intersectTwoPoints(
        element1: Element,
        element2: Element
    ): Pair<Point, Point> =
        when (val i = intersect(element1, element2)) {
            is Intersection.TwoPoints -> Pair(i.point1, i.point2)
            else -> error("Two intersection points expected: $i")
        }

    private fun intersectTwoPointsOther(
        element1: Element,
        element2: Element,
        point1: Point
    ): Point {
        val intersection = intersect(element1, element2)
        val points = intersection.points().filter { point2 -> !coincides(point1, point2) }
        return when (points.size) {
            1 -> points.first()
            else -> error("Expected one point other than $point1: $intersection")
        }
    }

    @Test
    fun puzzle15_7_stages() {
        // Drop a Perpendicular**
        // (lines only)
        val solution = Element.Line(Point(0.0, 0.0), Point(0.0, 1.0))
        val solutionContext = solve(puzzle15_7_par_stage(), 5) { context ->
            context.hasElement(solution)
        }
        dumpSolution(solutionContext)
    }

    @Test
    fun puzzle15_7_par() {
        // Drop a Perpendicular**
        // (lines only)
        // Look for a parallel line
        val solutionContext = puzzle15_7_par_stage()
        dumpSolution(solutionContext)
    }

    private fun puzzle15_7_par_stage(): EuclideaContext {
        val center = Point(0.0, 2.0)
        val circle = Element.Circle(center, 1.0)
        val line = Element.Line(Point(0.0, 0.0), Point(1.0, 0.0))
        // 'probe' line to cut across the circle and line.
        val probeLine = Element.Line(Point(-1.043215, 0.0), Point(-0.828934, 3.0))
        val initialContext = EuclideaContext(
            config = EuclideaConfig(circleToolEnabled = false),
            points = listOf(center),
            elements = listOf(circle, line)
        ).withElement(probeLine)
        val solutionContext = solve(initialContext, 6) { context ->
            context.elements.any {
                it !== circle && it !== line && it !== probeLine && intersect(
                    line,
                    it
                ) == Intersection.Disjoint
            }
        }
        return solutionContext!!
    }

    @Test
    fun puzzle15_8() {
        // Line-Circle Intersection*
        // (circles only)
        val center = Point(0.0, 0.0)
        val circle = Element.Circle(center, 1.0)
        val point = Point(-2.542897, 0.0)
        val initialContext = EuclideaContext(
            config = EuclideaConfig(lineToolEnabled = false),
            points = listOf(center, point),
            elements = listOf(circle)
        )
        // Check for either solution point, or a useful waypoint
        val solutionKnots = listOf(-1.0, 1.0, -2.0, 2.0)
        fun checkSolution(a: Double, b: Double): Boolean {
            return coincides(a, 0.0) && solutionKnots.any { coincides(it, b) }
        }
        // 6 - nothing
        // 7 - coincidence? (~3 hours)
        val solutionContext = solve(initialContext, 6) { context ->
            context.points.any { checkSolution(it.y, it.x) }
        }
        dumpSolution(solutionContext)
    }

    private fun dumpSolution(solutionContext: EuclideaContext?, namer: Namer = Namer()) {
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

                fun explainSteps() {
                    for (element in elements) {
                        when (element) {
                            is Element.Circle -> explainCircle(element)
                            is Element.Line -> explainLine(element)
                        }
                    }
                }
            }.explainSteps()
        }
    }

    private fun solve(
        initialContext: EuclideaContext,
        maxDepth: Int,
        prune: ((EuclideaContext) -> Boolean)? = null,
        check: (EuclideaContext) -> Boolean
    ): EuclideaContext? {
        fun sub(context: EuclideaContext, depth: Int): EuclideaContext? {
            val nextDepth = depth + 1
            for (next in context.nexts())
                if (check(next))
                    return next
                else if (nextDepth < maxDepth && (prune == null || !prune(next)))
                    sub(next, nextDepth)?.let { return@sub it }
            return null
        }
        if (check(initialContext))
            return initialContext
        return sub(initialContext, 0)
    }

    private fun replaySteps(referenceContext: EuclideaContext, replayInitialContext: EuclideaContext): EuclideaContext {
        val fromReferencePoint = mutableMapOf<Point, Point>()
        val fromReferenceElement = mutableMapOf<Element, Element>()

        fun replayFail(message: String): Nothing {
            throw IllegalStateException(message)
        }

        fun unifyPoint(referencePoint: Point, replayPoint: Point) {
            val existing = fromReferencePoint.putIfAbsent(referencePoint, replayPoint)
            if (existing !== null) {
                // TODO: Maybe check coincides rather than equality?
                if (existing != replayPoint)
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
                // TODO: Maybe check coincides rather than equality?
                if (existing != replayElement)
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

        fun findReplayPoint(referencePoint: Point): Point {
            return fromReferencePoint.getOrElse(referencePoint) {
                when (val intersectionSource = referenceContext.pointSource[referencePoint]) {
                    null -> replayFail("Failed to find replay point for $referencePoint: no reference point source")
                    else -> with(intersectionSource) {
                        val replayElement1 = replayElementFor(element1)
                        val replayElement2 = replayElementFor(element2)
                        val replayIntersection = intersect(replayElement1, replayElement2)
                        unifyIntersection(intersection, replayIntersection)
                        replayPointFor(referencePoint)
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
                    val replayCircle = circleTool(replayCenter, replaySample)
                    if (replayCircle === null)
                        replayFail("Failed to generate replay circle for $referenceElement: replay has degenerate circle with center $replayCenter and sample point $replaySample")
                    replayCircle
                }
                is Element.Line -> {
                    val replayPoint1 = findReplayPoint(referenceElement.point1)
                    val replayPoint2 = findReplayPoint(referenceElement.point2)
                    val replayLine = lineTool(replayPoint1, replayPoint2)
                    if (replayLine === null)
                        replayFail("Failed to generate replay line for $referenceElement: replay has degenerate line with points $replayPoint1 and $replayPoint2")
                    replayLine
                }
            }
        }

        // Unify initial context
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
}
