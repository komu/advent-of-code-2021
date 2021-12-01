fun main() {
    fun part1(input: List<String>): Int =
        input.map { it.toInt() }
            .zipWithNext()
            .count { (a, b) -> b > a }

    fun part2(input: List<String>): Int =
        input.map { it.toInt() }
            .windowed(3)
            .zipWithNext()
            .count { (a, b) -> b.sum() > a.sum() }

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 7)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
