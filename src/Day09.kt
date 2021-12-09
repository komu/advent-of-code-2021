fun main() {
    fun part1(input: List<String>): Int {
        val heightMap = HeightMap(input)

        return heightMap.points
            .filter { heightMap.isLow(it) }
            .sumOf { 1 + heightMap[it] }
    }

    fun part2(input: List<String>): Int {
        val heightMap = HeightMap(input)
        val visited = mutableSetOf<Point>()

        fun floodFill(point: Point): Int =
            if (point in heightMap && heightMap[point] != 9 && visited.add(point))
                1 + point.neighbors.sumOf { floodFill(it) }
            else
                0

        return heightMap.points
            .map { floodFill(it) }
            .sortedDescending()
            .take(3)
            .product()
    }

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
    check(part2(input) == 1045660)
}

private class HeightMap(private val lines: List<String>) {

    val height = lines.size
    val width = lines.first().length

    val points: List<Point>
        get() = buildList {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    add(Point(x, y))
                }
            }
        }

    operator fun get(p: Point) = lines[p.y][p.x].digitToInt()
    operator fun contains(p: Point) = p.x in 0 until width && p.y in 0 until height

    fun isLow(p: Point): Boolean {
        val height = this[p]

        return p.neighbors.all { it !in this || this[it] > height }
    }
}

