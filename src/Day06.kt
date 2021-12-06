fun main() {
    fun solve(input: List<String>, days: Int): Long {
        val initialTimers = input.first().split(",").map { it.toInt() }

        val fishesAtTime = LongArray(9)
        for (timer in initialTimers)
            fishesAtTime[timer] = fishesAtTime[timer] + 1

        repeat(days) {
            val fishesAtZero = fishesAtTime[0]
            fishesAtTime.copyInto(fishesAtTime, destinationOffset = 0, startIndex = 1)

            fishesAtTime[6] += fishesAtZero
            fishesAtTime[8] = fishesAtZero
        }

        return fishesAtTime.sum()
    }

    fun part1(input: List<String>) = solve(input, 80)
    fun part2(input: List<String>) = solve(input, 256)

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5934L)
    check(part2(testInput) == 26984457539L)

    val input = readInput("Day06")
    println(part1(input))
    println(part2(input))
}
