fun main() {

    fun part1(input: List<String>): Int {
        val numbers = input.first().split(",").map { it.toInt() }
        val boards = input.drop(1).chunked(6).map { BingoBoard(it.drop(1)) }

        for (number in numbers) {
            for (board in boards) {
                board.seen(number)
                if (board.hasBingo)
                    return number * board.unmarkedSum
            }
        }

        return -1
    }

    fun part2(input: List<String>): Int {
        val numbers = input.first().split(",").map { it.toInt() }
        val boards = input.drop(1).chunked(6).map { BingoBoard(it.drop(1)) }.toMutableSet()

        for (number in numbers) {
            val it = boards.iterator()
            while (it.hasNext()) {
                val board = it.next()
                board.seen(number)

                if (board.hasBingo)
                    it.remove()

                if (boards.isEmpty())
                    return number * board.unmarkedSum
            }
        }

        return -1
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))

    check(part1(input) == 8580)
    check(part2(input) == 9576)
}

private class BingoBoard(lines: List<String>) {

    private val cells = lines
        .flatMap { it.trim().split(whitespace) }
        .map { Cell(it.toInt()) }

    val hasBingo: Boolean
        get() = validBingoIndices.any { indices -> indices.all { cells[it].seen } }

    val unmarkedSum: Int
        get() = cells.filter { !it.seen }.sumOf { it.value }

    fun seen(number: Int) {
        for (cell in cells)
            if (cell.value == number)
                cell.seen = true
    }

    private class Cell(val value: Int) {
        var seen = false
    }

    private companion object {
        private val whitespace = Regex("\\s+")

        private val lineIndices = (0..24).chunked(5)
        private val validBingoIndices = lineIndices + lineIndices.transpose()
    }
}
