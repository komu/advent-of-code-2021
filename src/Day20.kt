fun main() {
    fun solve(input: List<String>, steps: Int): Int {
        val code = input.first()
        val original = OriginalTrenchMap(input.drop(2))

        var image: TrenchMap = original
        repeat(steps) {
            image = ConvolutedImage(code, image)
        }

        var count = 0
        for (x in -steps..original.width + steps)
            for (y in -steps..original.height + steps)
                if (image.isLit(x, y))
                    count++
        return count
    }

    fun part1(input: List<String>) =
        solve(input, 2)

    fun part2(input: List<String>) =
        solve(input, 50)

    val testInput = readInput("Day20_test")
    checkEqual(part1(testInput), 35)

    val input = readInput("Day20")
    println("result: " + part1(input))
    checkEqual(part1(input), 5573)

    val result2 = part2(input)
    println(result2)
    checkEqual(result2, 20097)
}

private interface TrenchMap {
    fun isLit(x: Int, y: Int): Boolean
}

private class ConvolutedImage(private val code: String, private val image: TrenchMap) : TrenchMap {

    private val cache = mutableMapOf<Pair<Int, Int>, Boolean>()

    override fun isLit(x: Int, y: Int) = cache.getOrPut(Pair(x, y)) {
        val bits = mutableListOf<Boolean>()
        for (dy in -1..1)
            for (dx in -1..1)
                bits += image.isLit(x + dx, y + dy)

        code[bits.fold(0) { acc, v -> acc * 2 + v.toInt() }] == '#'
    }
}

private class OriginalTrenchMap(private val lines: List<String>) : TrenchMap {

    val width = lines.first().length
    val height = lines.size

    override fun isLit(x: Int, y: Int) = lines.getOrNull(y)?.getOrNull(x) == '#'
}
