fun main() {
    fun solve(instructions: List<ReactorInstruction>): Long {
        var boxes = listOf<ReactorBox>()

        for (inst in instructions) {
            boxes = if (inst.on) {
                boxes + boxes.fold(listOf(inst.box)) { newBoxes, existing ->
                    newBoxes.flatMap {
                        it.subtractArea(existing)
                    }
                }

            } else {
                boxes.flatMap { it.subtractArea(inst.box) }
            }
        }

        return boxes.sumOf { it.volume }
    }

    fun part1(input: List<String>): Long =
        solve(input.map { ReactorInstruction(it).clip(-50..50) }.filterNot { it.box.isEmpty })

    fun part2(input: List<String>): Long =
        solve(input.map { ReactorInstruction(it) })

    checkEqual(part1(readInput("Day22_test1")), 590784)
    checkEqual(part2(readInput("Day22_test2")), 2758514936282235)

    val input = readInput("Day22")
    println(part1(input))
    checkEqual(part1(input), 647076)
    println(part2(input))
    checkEqual(part2(input), 1233304599156793)
}

private data class ReactorBox(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {

    val volume: Long
        get() = xRange.size.toLong() * yRange.size.toLong() * zRange.size.toLong()

    val isEmpty: Boolean
        get() = xRange.isEmpty() || yRange.isEmpty() || zRange.isEmpty()

    fun clip(bounds: IntRange) =
        ReactorBox(xRange.clip(bounds), yRange.clip(bounds), zRange.clip(bounds))

    fun overlaps(box: ReactorBox) =
        xRange.overlaps(box.xRange) && yRange.overlaps(box.yRange) && zRange.overlaps(box.zRange)

    operator fun contains(box: ReactorBox) =
        box.xRange in xRange && box.yRange in yRange && box.zRange in zRange

    fun subtractArea(area: ReactorBox): List<ReactorBox> {
        if (!overlaps(area)) return listOf(this)

        val (xRemaining, xCuts) = calculateCuts(xRange, area.xRange)
        val (yRemaining, yCuts) = calculateCuts(yRange, area.yRange)
        val (_, zCuts) = calculateCuts(zRange, area.zRange)

        val boxes = mutableListOf<ReactorBox>()

        val remainingAfterX = this.copy(xRange = xRemaining)
        for (xCut in xCuts)
            boxes += this.copy(xRange = xCut)

        val remainingAfterY = remainingAfterX.copy(yRange = yRemaining)
        for (yCut in yCuts)
            boxes += remainingAfterX.copy(yRange = yCut)

        for (zCut in zCuts)
            boxes += remainingAfterY.copy(zRange = zCut)

        return boxes
    }

    companion object {
        private fun calculateCuts(a: IntRange, s: IntRange): Pair<IntRange, List<IntRange>> {
            check(a.overlaps(s))

            val cuts = mutableListOf<IntRange>()
            if (a.first < s.first)
                cuts += a.first until s.first.coerceAtMost(a.last)
            if (s.last < a.last)
                cuts += (s.last + 1).coerceAtLeast(a.first)..a.last

            return Pair(s.clip(a), cuts)
        }
    }
}

private data class ReactorInstruction(val on: Boolean, val box: ReactorBox) {

    fun clip(range: IntRange) =
        ReactorInstruction(on, box.clip(range))

    companion object {
        private val pattern = Regex("""(on|off) x=(-?\d+)\.\.(-?\d+),y=(-?\d+)\.\.(-?\d+),z=(-?\d+)\.\.(-?\d+)""")

        operator fun invoke(s: String): ReactorInstruction {
            val (command, minX, maxX, minY, maxY, minZ, maxZ) = pattern.matchEntire(s)?.destructured
                ?: error("invalid command '$s'")

            return ReactorInstruction(
                on = command == "on",
                box = ReactorBox(
                    xRange = minX.toInt()..maxX.toInt(),
                    yRange = minY.toInt()..maxY.toInt(),
                    zRange = minZ.toInt()..maxZ.toInt()
                )
            )
        }
    }
}
