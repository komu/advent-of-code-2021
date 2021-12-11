fun main() {
    fun part1(input: List<String>): Int {
        val grid = OctopusGrid(input)

        var flashes = 0
        repeat(100) {

            for (p in grid.points)
                grid[p]++

            val work = grid.points.filter { grid[it] > 9 }.toMutableList()

            while (work.isNotEmpty()) {
                flashes++
                val point = work.removeLast()
                for (neighbor in point.allNeighbors) {
                    if (neighbor in grid && grid[neighbor] <= 9) {
                        grid[neighbor]++
                        if (grid[neighbor] > 9)
                            work.add(neighbor)
                    }
                }
            }

            for (p in grid.points)
                if (grid[p] > 9)
                    grid[p] = 0
        }

        return flashes
    }

    fun part2(input: List<String>): Int {
        val grid = OctopusGrid(input)

        var step = 1
        while (true) {

            for (p in grid.points)
                grid[p]++
            val work = grid.points.filter { grid[it] > 9 }.toMutableList()

            while (work.isNotEmpty()) {
                val point = work.removeLast()
                for (neighbor in point.allNeighbors) {
                    if (neighbor in grid && grid[neighbor] <= 9) {
                        grid[neighbor]++
                        if (grid[neighbor] > 9)
                            work.add(neighbor)
                    }
                }
            }

            val first = grid[Point(0, 0)]
            if (grid.points.all { grid[it] == first })
                return step

            for (p in grid.points)
                if (grid[p] > 9)
                    grid[p] = 0

            step++
        }
    }

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)
    check(part2(testInput) == 195)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))

    check(part1(input) == 1625)
    check(part2(input) == 244)
}

private class OctopusGrid(input: List<String>) {
    private val octopuses = input.map { line -> line.map { it.digitToInt() }.toMutableList() }

    val points = (0..9).flatMap { y -> (0..9).map { x -> Point(x, y) } }

    operator fun contains(p: Point) =
        p.x in 0..9 && p.y in 0..9

    operator fun get(p: Point) =
        octopuses[p.y][p.x]

    operator fun set(p: Point, v: Int) {
        octopuses[p.y][p.x] = v
    }
}
