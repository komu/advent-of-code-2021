import PaperAxis.*

fun main() {
    fun part1(input: List<String>): Int {
        val (points, folds) = parseOrigamiInstructions(input)
        val (foldAxis, foldValue) = folds.first()
        return OrigamiPaper(points).fold(foldAxis, foldValue).points.size
    }

    fun part2(input: List<String>): String {
        val (points, folds) = parseOrigamiInstructions(input)
        return folds.fold(OrigamiPaper(points)) { p, (axis, value) -> p.fold(axis, value) }.toString()
    }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}

private class OrigamiPaper(val points: Set<Point>) {

    fun fold(axis: PaperAxis, value: Int): OrigamiPaper {
        val (kept, folded) = points.partition { it[axis] < value }
        val foldedPoints = folded.map { it.copy(axis, 2 * value - it[axis]) }

        return OrigamiPaper((kept + foldedPoints).toSet())
    }

    override fun toString(): String {
        val maxX = points.maxOf { it.x }
        val maxY = points.maxOf { it.y }

        return buildString {
            for (y in 0..maxY) {
                for (x in 0..maxX)
                    append(if (Point(x, y) in points) '#' else '.')
                appendLine()
            }
        }
    }
}

private enum class PaperAxis { X, Y }

private operator fun Point.get(axis: PaperAxis): Int = when (axis) {
    X -> x
    Y -> y
}

private fun Point.copy(axis: PaperAxis, value: Int): Point = when (axis) {
    X -> copy(x = value)
    Y -> copy(y = value)
}

private val foldPattern = Regex("""fold along ([xy])=(\d+)""")

private fun parseOrigamiInstructions(input: List<String>): Pair<Set<Point>, List<Pair<PaperAxis, Int>>> {
    val points = input.takeWhile { it.isNotEmpty() }.map {
        val (x, y) = it.split(',')
        Point(x.toInt(), y.toInt())
    }.toSet()

    val folds = input.dropWhile { it.isNotEmpty() }.drop(1).map {
        val (_, axis, value) = foldPattern.matchEntire(it)!!.groupValues
        Pair(PaperAxis.valueOf(axis.uppercase()), value.toInt())
    }

    return Pair(points, folds)
}
