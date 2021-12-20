import kotlin.math.abs

fun main() {

    fun finalTransformBetween(p: Point3d, ref: Point3d, rotation: AffineTransform): AffineTransform {
        val rotated = rotation(p)
        return rotation.withTranslation(dx = ref.x - rotated.x, dy = ref.y - rotated.y, dz = ref.z - rotated.z)
    }

    fun tryToMerge(basis: BeaconSet, merged: BeaconSet): BeaconSet? {
        // Note that we can't find 12 matches in the last 11 items, which is why we can bail out early in both loops
        for (referenceBeacon in basis.beacons.take(maxOf(0, basis.beacons.size - 11))) {
            for (beacon in merged.beacons.take(maxOf(0, merged.size - 11))) {
                for (rotation in AffineTransform.rotations) {
                    val transformation = finalTransformBetween(beacon, referenceBeacon, rotation)
                    val transformedSet = merged.transform(transformation)

                    if (transformedSet.beacons.count { it in basis.beacons } >= 12)
                        return basis + transformedSet
                }
            }
        }

        return null
    }

    fun mergeCoordinateSets(sets: MutableList<BeaconSet>): Boolean {
        for ((i, set1) in sets.withIndex())
            for ((j, set2) in sets.withIndex())
                if (i != j) {
                    val known = tryToMerge(set1, set2) ?: continue
                    sets.remove(set1)
                    sets.remove(set2)
                    sets.add(known)
                    return true
                }

        return false
    }

    fun solve(input: List<String>): BeaconSet {
        val coordinateSets = BeaconSet.parseScanners(input).toMutableList()

        while (coordinateSets.size > 1)
            mergeCoordinateSets(coordinateSets)

        return coordinateSets.first()
    }

    fun part1(solution: BeaconSet) =
        solution.size

    fun part2(solution: BeaconSet) =
        solution.scanners.maxOf { v -> solution.scanners.maxOf { u -> v.manhattanDistance(u) } }

    checkEqual(part1(solve(readInput("Day19_test"))), 79)
    checkEqual(part2(solve(readInput("Day19_test"))), 3621)

    val solution = solve(readInput("Day19"))
    val result1 = part1(solution)
    val result2 = part2(solution)
    println(result1)
    println(result2)
    checkEqual(result1, 467)
    checkEqual(result2, 12226)
}

private data class AffineTransform(
    val x1: Int, val y1: Int, val z1: Int, val w1: Int,
    val x2: Int, val y2: Int, val z2: Int, val w2: Int,
    val x3: Int, val y3: Int, val z3: Int, val w3: Int,
) : (Point3d) -> Point3d {

    fun withTranslation(dx: Int, dy: Int, dz: Int) =
        copy(w1 = dx, w2 = dy, w3 = dz)

    fun rotateAroundZ() = AffineTransform(
        -x2, -y2, -z2, -w2,
        x1, y1, z1, w1,
        x3, y3, z3, w3
    )

    override fun invoke(p: Point3d) =
        Point3d(
            x = x1 * p.x + y1 * p.y + z1 * p.z + w1,
            y = x2 * p.x + y2 * p.y + z2 * p.z + w2,
            z = x3 * p.x + y3 * p.y + z3 * p.z + w3,
        )

    companion object {

        fun rotation(f: (Int, Int, Int) -> Point3d): AffineTransform {
            val v = f(1, 0, 0)
            val u = f(0, 1, 0)
            val t = f(0, 0, 1)

            return AffineTransform(
                v.x, u.x, t.x, 0,
                v.y, u.y, t.y, 0,
                v.z, u.z, t.z, 0
            )
        }

        private val basicFaces = listOf(
            rotation { x, y, z -> Point3d(-x, y, -z) },
            rotation { x, y, z -> Point3d(x, y, z) },
            rotation { x, y, z -> Point3d(x, z, -y) },
            rotation { x, y, z -> Point3d(-x, z, y) },
            rotation { x, y, z -> Point3d(z, y, -x) },
            rotation { x, y, z -> Point3d(-z, y, x) },
        )

        val rotations = basicFaces.flatMap { face -> (1..3).runningFold(face) { t, _ -> t.rotateAroundZ() } }
    }
}

private data class Point3d(val x: Int, val y: Int, val z: Int) {

    fun manhattanDistance(rhs: Point3d) = abs(x - rhs.x) + abs(y - rhs.y) + abs(z - rhs.z)

    override fun toString() = "$x,$y,$z"

    companion object {

        private val pointRegex = Regex("""(-?\d+),(-?\d+),(-?\d+)""")

        fun parse(s: String): Point3d {
            val (_, x, y, z) = pointRegex.matchEntire(s)?.groupValues ?: error("no match: '$s'")
            return Point3d(x.toInt(), y.toInt(), z.toInt())
        }
    }
}

private class BeaconSet(
    val name: String,
    scanners: Collection<Point3d>,
    beacons: Collection<Point3d>,
) {

    val scanners = scanners.toSet()
    val beacons = beacons.toSet()

    val size: Int
        get() = beacons.size

    fun transform(transform: AffineTransform) =
        BeaconSet(name,
            scanners.mapTo(mutableSetOf(), transform),
            beacons.mapTo(mutableSetOf(), transform))

    operator fun plus(rhs: BeaconSet) =
        BeaconSet("$name+${rhs.name}", scanners + rhs.scanners, beacons + rhs.beacons)

    override fun toString() = "$name (${beacons.size})"

    companion object {
        private val scannerRegex = Regex("""--- scanner (\d+) ---""")

        fun parseScanners(input: List<String>): List<BeaconSet> {
            val result = mutableListOf<BeaconSet>()
            var name = ""
            val currentCoordinates = mutableListOf<Point3d>()

            fun flush() {
                check(name != "")
                check(currentCoordinates.isNotEmpty())
                result += BeaconSet(name, setOf(Point3d(0, 0, 0)), currentCoordinates)
                currentCoordinates.clear()
            }

            for (line in input) {
                val match = scannerRegex.matchEntire(line)
                if (match != null) {
                    check(currentCoordinates.isEmpty())
                    name = match.groupValues[1]

                } else if (line == "") {
                    flush()

                } else {
                    currentCoordinates += Point3d.parse(line)
                }
            }

            flush()

            return result
        }
    }
}
