fun main() {
    fun part1(input: List<String>): Int {
        fun recurse(node: CaveNode, path: List<CaveNode>): Int =
            if (node.isEnd)
                1
            else
                node.neighbors.filter { !it.isSmall || it !in path }.sumOf { recurse(it, path + it) }

        val start = parseCaveSystem(input)
        return recurse(start, emptyList())
    }

    fun part2(input: List<String>): Int {
        fun recurse(node: CaveNode, path: List<CaveNode>, visitedSmallTwice: Boolean): Int =
            if (node.isEnd)
                1
            else
                node.neighbors.sumOf { neighbor ->
                    when {
                        !neighbor.isSmall || neighbor !in path ->
                            recurse(neighbor, path + neighbor, visitedSmallTwice)
                        !visitedSmallTwice ->
                            recurse(neighbor, path + neighbor, visitedSmallTwice = true)
                        else ->
                            0
                    }
                }

        val start = parseCaveSystem(input)
        return recurse(start, emptyList(), visitedSmallTwice = false)
    }

    val testInput1 = readInput("Day12_test1")
    val testInput2 = readInput("Day12_test2")
    val testInput3 = readInput("Day12_test3")
    check(part1(testInput1) == 10)
    check(part1(testInput2) == 19)
    check(part1(testInput3) == 226)
    check(part2(testInput1) == 36)
    check(part2(testInput2) == 103)
    check(part2(testInput3) == 3509)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))

    check(part1(input) == 4573)
    check(part2(input) == 117509)
}

private class CaveNode(private val name: String) {

    val neighbors = mutableListOf<CaveNode>()

    val isStart: Boolean
        get() = name == "start"

    val isEnd: Boolean
        get() = name == "end"

    val isSmall: Boolean
        get() = name[0].isLowerCase()
}

private fun parseCaveSystem(input: List<String>): CaveNode {
    val nodes = mutableMapOf<String, CaveNode>()

    for (line in input) {
        val (src, dst) = line.split('-')

        val srcNode = nodes.getOrPut(src) { CaveNode(src) }
        val dstNode = nodes.getOrPut(dst) { CaveNode(dst) }

        if (!dstNode.isStart && !srcNode.isEnd)
            srcNode.neighbors += dstNode

        if (!srcNode.isStart && !dstNode.isEnd)
            dstNode.neighbors += srcNode
    }

    return nodes["start"] ?: error("no start node")
}
