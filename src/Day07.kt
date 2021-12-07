import kotlin.math.abs

fun main() {

    fun solve(input: List<String>, score: (Int) -> Int): Long {
        val positions = input.first().split(",").map { it.toInt() }

        val min = positions.minOf { it }
        val max = positions.maxOf { it }

        var bestScore = Long.MAX_VALUE
        for (candidate in min until max)
            bestScore = minOf(bestScore, positions.sumOf { score(abs(it - candidate)).toLong() })

        return bestScore
    }

    fun part1(input: List<String>) =
        solve(input) { it }

    fun part2(input: List<String>) =
        solve(input) { d -> (d * (d + 1)) / 2 }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37L)
    check(part2(testInput) == 168L)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
    check(part2(input) == 89791146L)
}
