fun main() {
    fun part1(input: List<String>): Int {
        var state = CucumberFloor(input)

        var steps = 0
        while (true) {
            steps++
            state = state.step() ?: return steps
        }
    }

    val testInput = readInput("Day25_test")
    checkEqual(part1(testInput), 58)

    val result1 = part1(readInput("Day25"))
    println(result1)
    checkEqual(result1, 435)
}

private class CucumberFloor(private val width: Int, private val height: Int, init: (Int, Int) -> Char) {

    private val data = CharArray(width * height) { i -> init(i % width, i / width) }

    operator fun get(x: Int, y: Int) =
        data[(y + height) % height * width + (x + width) % width]

    fun step(): CucumberFloor? {
        val first = CucumberFloor(width, height) { x, y ->
            when {
                this[x, y] == '.' && this[x - 1, y] == '>' -> '>'
                this[x, y] == '>' && this[x + 1, y] == '.' -> '.'
                else -> this[x, y]
            }
        }

        val second = CucumberFloor(width, height) { x, y ->
            when {
                first[x, y] == '.' && first[x, y - 1] == 'v' -> 'v'
                first[x, y] == 'v' && first[x, y + 1] == '.' -> '.'
                else -> first[x, y]
            }
        }

        return if (data.contentEquals(second.data)) null else second
    }

    companion object {
        operator fun invoke(input: List<String>) =
            CucumberFloor(width = input.first().length, height = input.size) { x, y ->
                input[y][x]
            }
    }
}
