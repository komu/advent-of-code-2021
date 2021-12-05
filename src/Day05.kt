import kotlin.math.abs
import kotlin.math.sign

fun main() {

    fun solve(input: List<String>, predicate: (LineSegment) -> Boolean): Int {
        val lines = input.map { LineSegment(it) }.filter(predicate)
        val oceanFloor = OceanFloor()

        for (line in lines) {
            val dx = (line.end.x - line.start.x).sign
            val dy = (line.end.y - line.start.y).sign

            repeat(line.length) { n ->
                oceanFloor.add(
                    x = line.start.x + n * dx,
                    y = line.start.y + n * dy
                )
            }
        }

        return oceanFloor.hasAtLeastTwoOverlaps()
    }

    fun part1(input: List<String>): Int =
        solve(input) { it.start.y == it.end.y || it.start.x == it.end.x }

    fun part2(input: List<String>): Int =
        solve(input) { true }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 12)

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))

    check(part1(input) == 5197)
    check(part2(input) == 18605)
}

private class OceanFloor {

    private val visits = IntArray(WIDTH * HEIGHT)

    fun add(x: Int, y: Int) {
        visits[y * WIDTH + x] += 1
    }

    fun hasAtLeastTwoOverlaps() =
        visits.count { it >= 2 }

    companion object {
        private const val WIDTH = 1000
        private const val HEIGHT = 1000
    }
}

private data class Point(val x: Int, val y: Int)

private data class LineSegment(val start: Point, val end: Point) {

    val length: Int
        get() = 1 + maxOf(abs(start.x - end.x), abs(start.y - end.y))

    companion object {

        operator fun invoke(line: String): LineSegment {
            val (_, x1, y1, x2, y2) = inputPattern.matchEntire(line)?.groupValues ?: error("invalid line '$line'")
            return LineSegment(Point(x1.toInt(), y1.toInt()), Point(x2.toInt(), y2.toInt()))
        }

        private val inputPattern = Regex("""(\d+),(\d+) -> (\d+),(\d+)""")
    }
}
