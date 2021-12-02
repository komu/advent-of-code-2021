fun main() {

    fun parseCommand(s: String): Pair<String, Int> {
        val (command, param) = s.split(" ")
        return Pair(command, param.toInt())
    }

    fun part1(input: List<String>): Int {
        var horizontalPosition = 0
        var depth = 0

        for (line in input) {
            val (command, x) = parseCommand(line)
            when (command) {
                "forward" ->
                    horizontalPosition += x
                "up" ->
                    depth -= x
                "down" ->
                    depth += x
            }
        }

        return horizontalPosition * depth
    }

    fun part2(input: List<String>): Int {
        var horizontalPosition = 0
        var depth = 0
        var aim = 0

        for (line in input) {
            val (command, x) = parseCommand(line)
            when (command) {
                "forward" -> {
                    horizontalPosition += x
                    depth += aim * x
                }
                "up" ->
                    aim -= x
                "down" ->
                    aim += x
            }
        }

        return horizontalPosition * depth
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 150)
    check(part2(testInput) == 900)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
