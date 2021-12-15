import java.util.*

fun main() {

    fun solve(input: List<String>, scale: Int): Int {
        val map = ChitonMap(input).scale(scale)

        return shortestPathCost(map.topLeft, map.bottomRight) { p ->
            p.neighbors.filter { it in map }.map { it to map.cost(it) }
        }
    }

    fun part1(input: List<String>) = solve(input, scale = 1)
    fun part2(input: List<String>) = solve(input, scale = 5)

    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println(part1(input))
    println(part2(input))
}

private class ChitonMap(private val costs: List<List<Int>>) {

    private val size: Int
        get() = costs.size

    val topLeft: Point
        get() = Point(0, 0)

    val bottomRight: Point
        get() = Point(size - 1, size - 1)

    fun scale(multiplier: Int) =
        ChitonMap(List(size * multiplier) { y ->
            List(size * multiplier) { x ->
                val baseRisk = costs[y % size][x % size]
                val increase = y / size + x / size
                (baseRisk + increase - 1) % 9 + 1
            }
        })

    operator fun contains(p: Point) =
        p.y in 0 until size && p.x in 0 until size

    fun cost(p: Point) = costs[p.y][p.x]

    companion object {
        operator fun invoke(input: List<String>) = ChitonMap(input.map { r -> r.map { it.digitToInt() } })
    }
}

private inline fun shortestPathCost(from: Point, target: Point, edges: (Point) -> List<Pair<Point, Int>>): Int {
    val initial = PathNode(from, 0)
    val nodes = mutableMapOf(from to initial)
    val queue = PriorityQueue(setOf(initial))

    while (queue.isNotEmpty()) {
        val v = queue.remove()

        for ((u, cost) in edges(v.point)) {
            val newDistance = v.distance + cost
            val previousDistance = nodes[u]?.distance
            if (previousDistance == null || newDistance < previousDistance) {
                val newNode = PathNode(u, newDistance)
                nodes[u] = newNode
                queue += newNode
            }
        }
    }

    return nodes[target]!!.distance
}

private class PathNode(val point: Point, val distance: Int) : Comparable<PathNode> {
    override fun compareTo(other: PathNode) = distance.compareTo(other.distance)
}
