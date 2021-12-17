import kotlin.math.sign

fun main() {

    fun maxHeight(target: TargetArea, dx: Int, dy: Int): Int {
        val probe = Probe(dx, dy)

        var maxHeight = Int.MIN_VALUE
        while (probe.y >= target.yRange.first) {
            maxHeight = maxOf(maxHeight, probe.y)
            if (probe in target)
                return maxHeight

            probe.step()
        }

        return Int.MIN_VALUE
    }

    fun part1(target: TargetArea): Int {
        var bestMaxHeight = Int.MIN_VALUE
        for (dx in 1..100)
            for (dy in 0..200)
                bestMaxHeight = maxOf(bestMaxHeight, maxHeight(target, dx, dy))

        return bestMaxHeight
    }

    fun part2(target: TargetArea): Int {
        var count = 0
        for (dx in 1..target.xRange.last)
            for (dy in target.yRange.first..150)
                if (maxHeight(target, dx, dy) != Int.MIN_VALUE)
                    count++

        return count
    }

    val testInput = TargetArea(20..30, -10..-5)
    val input = TargetArea(185..221, -122..-74)

    check(maxHeight(testInput, 6, 9) == 45)
    check(part2(testInput) == 112)

    println(part1(input))
    println(part2(input))

    check(part1(input) == 7381)
    check(part2(input) == 3019)
}

private class Probe(private var dx: Int, private var dy: Int) {

    var x = 0
    var y = 0

    fun step() {
        x += dx
        y += dy
        dx -= dx.sign
        dy -= 1
    }
}

private class TargetArea(val xRange: IntRange, val yRange: IntRange) {
    operator fun contains(p: Probe) = p.x in xRange && p.y in yRange
}
