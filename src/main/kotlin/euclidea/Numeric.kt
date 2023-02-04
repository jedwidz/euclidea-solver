package euclidea

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun solveByBisection(o1: Double, o2: Double, f: (Double) -> Double): Double {
    var x1 = min(o1, o2)
    var x2 = max(o1, o2)
    val so1 = sign(f(x1))
    val so2 = sign(f(x2))
    if (so1 == 0.0)
        return x1
    if (so2 == 0.0)
        return x2
    require(so1 != so2)
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
