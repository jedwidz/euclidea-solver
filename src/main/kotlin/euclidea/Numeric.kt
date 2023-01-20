package euclidea

import kotlin.math.sign

fun solveByBisection(o1: Double, o2: Double, f: (Double) -> Double): Double {
    val so1 = sign(f(o1))
    val so2 = sign(f(o2))
    if (so1 == 0.0)
        return o1
    if (so2 == 0.0)
        return o2
    require(so1 != so2)
    var x1 = o1
    var x2 = o2
    while (true) {
        val m = (x1 + x2) * 0.5
        if (!(x1 < m && m < x2))
            return m
        val sm = sign(f(m))
        if (sm == 0.0)
            return m
        if (sm == so1)
            x1 = m
        else
            x2 = m
    }
}
