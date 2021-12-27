import AmphipodType.*
import java.util.*
import kotlin.system.measureTimeMillis

fun main() {

    fun solve(space: AmphipodSpace, rows: List<List<AmphipodType>>): Int {
        val ts = List(4) { i ->  rows.map { it[i] }.reversed() }.flatten()

        val config = AmphipodState(space, ts.zip(space.homes.flatten()) { t, l -> Amphipod(t, l) })

        return shortestPathCost(config) ?: error("no solution")
    }

    fun part1(rows: List<List<AmphipodType>>) =
        solve(AmphipodSpace.simple, rows)

    fun part2(rows: List<List<AmphipodType>>) = solve(AmphipodSpace.expanded, listOf(
        rows[0],
        listOf(DESERT, COPPER, BRONZE, AMBER),
        listOf(DESERT, BRONZE, AMBER, COPPER),
        rows[1]
    ))

    val testInput = listOf(
        listOf(BRONZE, COPPER, BRONZE, DESERT),
        listOf(AMBER, DESERT, COPPER, AMBER)
    )

    val input = listOf(
        listOf(AMBER, COPPER, BRONZE, AMBER),
        listOf(DESERT, DESERT, BRONZE, COPPER),
    )

    println(measureTimeMillis {
        checkEqual(part1(testInput), 12521)
    }.toString() + " ms")

    println(measureTimeMillis {
        checkEqual(part2(testInput), 44169)
    }.toString() + " ms")

    val part1 = part1(input)
    println(part1)
    checkEqual(part1, 18195)

    val part2 = part2(input)
    println(part2)
    checkEqual(part2, 50265)
}

private enum class AmphipodType(val moveCost: Int) {
    AMBER(moveCost = 1),
    BRONZE(moveCost = 10),
    COPPER(moveCost = 100),
    DESERT(moveCost = 1000)
}

private class AmphipodSpaceNode(val id: Int, val homeFor: AmphipodType? = null) {
    val neighbors = mutableListOf<Pair<AmphipodSpaceNode, Int>>()
    lateinit var paths: Map<AmphipodSpaceNode, PathDefinition>

    val isRoom: Boolean
        get() = homeFor != null
}

private class AmphipodSpace(homeSize: Int) {
    val leftmost = AmphipodSpaceNode(idSeq++)
    val left = AmphipodSpaceNode(idSeq++)
    val betweenAmberAndBronze = AmphipodSpaceNode(idSeq++)
    val betweenBronzeAndCopper = AmphipodSpaceNode(idSeq++)
    val betweenCopperAndDesert = AmphipodSpaceNode(idSeq++)
    val right = AmphipodSpaceNode(idSeq++)
    val rightmost = AmphipodSpaceNode(idSeq++)

    val amberHomes = createHome(AMBER, homeSize)
    val bronzeHomes = createHome(BRONZE, homeSize)
    val copperHomes = createHome(COPPER, homeSize)
    val desertHomes = createHome(DESERT, homeSize)
    val homes = listOf(amberHomes, bronzeHomes, copperHomes, desertHomes)
    val hallways =
        listOf(leftmost, left, betweenAmberAndBronze, betweenBronzeAndCopper, betweenCopperAndDesert, right, rightmost)
    private val allNodes = homes.flatten() + hallways

    private fun connect(n1: AmphipodSpaceNode, n2: AmphipodSpaceNode, steps: Int) {
        n1.neighbors += Pair(n2, steps)
        n2.neighbors += Pair(n1, steps)
    }

    init {
        for (homeCollection in homes)
            for ((h, n) in homeCollection.zipWithNext())
                connect(h, n, steps = 1)

        connect(left, leftmost, steps = 1)
        connect(right, rightmost, steps = 1)

        val amberTop = amberHomes.last()
        val bronzeTop = bronzeHomes.last()
        val copperTop = copperHomes.last()
        val desertTop = desertHomes.last()

        connect(amberTop, left, steps = 2)
        connect(amberTop, betweenAmberAndBronze, steps = 2)
        connect(bronzeTop, betweenAmberAndBronze, steps = 2)
        connect(bronzeTop, betweenBronzeAndCopper, steps = 2)
        connect(copperTop, betweenBronzeAndCopper, steps = 2)
        connect(copperTop, betweenCopperAndDesert, steps = 2)
        connect(desertTop, betweenCopperAndDesert, steps = 2)
        connect(desertTop, right, steps = 2)

        connect(left, betweenAmberAndBronze, steps = 2)
        connect(betweenAmberAndBronze, betweenBronzeAndCopper, steps = 2)
        connect(betweenBronzeAndCopper, betweenCopperAndDesert, steps = 2)
        connect(betweenCopperAndDesert, right, steps = 2)

        for (node in allNodes)
            node.paths = pathsFrom(node)
    }

    fun homeRowsFromBottomToTop(type: AmphipodType) = when (type) {
        AMBER -> amberHomes
        BRONZE -> bronzeHomes
        COPPER -> copperHomes
        DESERT -> desertHomes
    }

    companion object {
        private var idSeq = 1

        fun createHome(type: AmphipodType, size: Int) =
            List(size) { AmphipodSpaceNode(idSeq++, type) }

        val simple = AmphipodSpace(homeSize = 2)
        val expanded = AmphipodSpace(homeSize = 4)
    }
}

private data class Amphipod(val type: AmphipodType, val location: AmphipodSpaceNode, val moveCount: Int = 0) {

    fun move(target: AmphipodSpaceNode) = copy(location = target, moveCount = moveCount + 1)

    val mayMove: Boolean
        get() = moveCount <= 1

    val isInRoom: Boolean
        get() = location.isRoom

    val isAtHome: Boolean
        get() = location.homeFor == type
}

@JvmInline
private value class AmphipodSpaceNodeSet(private val bits: Int = 0) {

    operator fun contains(node: AmphipodSpaceNode) =
        ((bits shr node.id) and 1) == 1

    operator fun plus(node: AmphipodSpaceNode): AmphipodSpaceNodeSet =
        AmphipodSpaceNodeSet(bits = bits or (1 shl node.id))

    infix fun and(set: AmphipodSpaceNodeSet) = AmphipodSpaceNodeSet(bits and set.bits)

    val isEmpty: Boolean
        get() = bits == 0

    companion object {

        val empty = AmphipodSpaceNodeSet(0)

        operator fun invoke(amphipods: List<Amphipod>): AmphipodSpaceNodeSet {
            var set = empty

            for (a in amphipods)
                set += a.location

            return set
        }
    }
}

private class AmphipodState(
    val space: AmphipodSpace,
    val amphipods: List<Amphipod>,
) {

    private val occupiedRooms = AmphipodSpaceNodeSet(amphipods)

    override fun equals(other: Any?): Boolean =
        other is AmphipodState && amphipods == other.amphipods

    override fun hashCode(): Int =
        amphipods.hashCode()

    val isSolved: Boolean
        get() = amphipods.all { it.isAtHome }

    val edges: List<Pair<AmphipodState, Int>>
        get() = buildList {
            for (amphipod in amphipods) {
                if (!amphipod.mayMove) continue

                if (amphipod.isInRoom) {
                    if (completedFor(amphipod.type)) continue

                    for (target in space.hallways) {
                        val steps = pathLength(amphipod.location, target)
                        if (steps != null)
                            add(move(amphipod, target, steps))
                    }
                } else if (homeIsAvailable(amphipod.type)) {
                    for (target in space.homeRowsFromBottomToTop(amphipod.type))
                        if (target !in occupiedRooms) {
                            val steps = pathLength(amphipod.location, target)
                            if (steps != null)
                                add(move(amphipod, target, steps))
                        }
                }
            }
        }

    private fun homeIsAvailable(type: AmphipodType): Boolean =
        amphipods.all { it.location.homeFor != type || it.type == type }

    private fun pathLength(from: AmphipodSpaceNode, target: AmphipodSpaceNode): Int? =
        from.paths[target]?.takeIf { (it.nodes and occupiedRooms).isEmpty }?.cost

    fun move(amphipod: Amphipod, target: AmphipodSpaceNode, steps: Int): Pair<AmphipodState, Int> {
        val cost = steps * amphipod.type.moveCost

        val newState = AmphipodState(
            space, amphipods.map {
                if (it === amphipod) it.move(target) else it
            }
        )

        return Pair(newState, cost)
    }

    private fun completedFor(type: AmphipodType): Boolean =
        amphipods.all { it.type != type || it.isAtHome }
}

private fun shortestPathCost(from: AmphipodState): Int? {
    val initial = AmphipodPathNode(from, 0, null)
    val nodes = mutableMapOf(from to initial)
    val queue = PriorityQueue(setOf(initial))

    while (queue.isNotEmpty()) {
        val v = queue.remove()

        if (v.node.isSolved)
            return v.distance

        for ((u, cost) in v.node.edges) {
            val newDistance = v.distance + cost
            val previousDistance = nodes[u]?.distance
            if (previousDistance == null || newDistance < previousDistance) {
                val newNode = AmphipodPathNode(u, newDistance, v)
                nodes[u] = newNode
                queue += newNode
            }
        }
    }

    return null
}

private class PathDefinition(val cost: Int, val nodes: AmphipodSpaceNodeSet)

private fun pathsFrom(from: AmphipodSpaceNode): Map<AmphipodSpaceNode, PathDefinition> {
    val initial = AmphipodPathNode(from, 0, null)
    val nodes = mutableMapOf(from to initial)
    val queue = PriorityQueue(setOf(initial))

    while (queue.isNotEmpty()) {
        val v = queue.remove()

        for ((u, cost) in v.node.neighbors) {
            val newDistance = v.distance + cost
            val previousDistance = nodes[u]?.distance
            if (previousDistance == null || newDistance < previousDistance) {
                val newNode = AmphipodPathNode(u, newDistance, v)
                nodes[u] = newNode
                queue += newNode
            }
        }
    }

    val result = mutableMapOf<AmphipodSpaceNode, PathDefinition>()
    for (pathNode in nodes.values) {
        var nodesOnPath = AmphipodSpaceNodeSet()
        var n = pathNode
        while (true) {
            if (n.node != from)
                nodesOnPath += n.node
            n = n.previous ?: break
        }

        result[pathNode.node] = PathDefinition(pathNode.distance, nodesOnPath)
    }

    return result
}

private class AmphipodPathNode<T>(val node: T, val distance: Int, val previous: AmphipodPathNode<T>?) : Comparable<AmphipodPathNode<T>> {
    override fun compareTo(other: AmphipodPathNode<T>) = distance.compareTo(other.distance)
}
