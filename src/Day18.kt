fun main() {
    fun part1(input: List<String>) =
        input.map { SnailfishNumber(it) }.reduce { a, b -> a + b }.magnitude

    fun part2(input: List<String>): Int {
        val numbers = input.map { SnailfishNumber(it) }

        var maxMagnitude = Int.MIN_VALUE
        for ((i, x) in numbers.withIndex())
            for ((j, y) in numbers.withIndex())
                if (i != j)
                    maxMagnitude = maxOf(maxMagnitude, (x + y).magnitude)

        return maxMagnitude
    }

    val testInput = readInput("Day18_test")

    checkEqual(part1(testInput), 4140)

    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))

    checkEqual(part1(input), 4116)
    checkEqual(part2(input), 4638)
}



private sealed class SnailfishNumber {

    operator fun plus(rhs: SnailfishNumber): SnailfishNumber {
        var value: SnailfishNumber = Pair(this, rhs)

        while (true)
            value = value.reduceExplodes()?.number ?: value.splitFirst() ?: return value
    }

    abstract val magnitude: Int
    protected abstract fun addToFirstRegular(n: Int): SnailfishNumber
    protected abstract fun addToLastRegular(n: Int): SnailfishNumber
    protected abstract fun splitFirst(): SnailfishNumber?
    protected abstract fun reduceExplodes(depth: Int = 0): ExplosionResult?

    protected class ExplosionResult(val number: SnailfishNumber, val leftResidual: Int, val rightResidual: Int)

    data class Regular(val value: Int) : SnailfishNumber() {
        override fun toString() = value.toString()

        override fun addToFirstRegular(n: Int) =
            Regular(value + n)

        override fun addToLastRegular(n: Int) =
            Regular(value + n)

        override fun splitFirst() =
            if (value >= 10) Pair(value / 2, (value + 1) / 2) else null

        override fun reduceExplodes(depth: Int): ExplosionResult? = null

        override val magnitude: Int
            get() = value
    }

    data class Pair(val left: SnailfishNumber, val right: SnailfishNumber) : SnailfishNumber() {

        constructor(left: Int, right: Int) : this(Regular(left), Regular(right))

        override fun toString() = "[$left,$right]"

        override fun splitFirst(): SnailfishNumber? {
            val l = left.splitFirst()
            if (l != null)
                return Pair(l, right)

            val r = right.splitFirst()
            if (r != null)
                return Pair(left, r)

            return null
        }

        override fun reduceExplodes(depth: Int): ExplosionResult? {
            if (depth == 4)
                return ExplosionResult(Regular(0), (left as Regular).value, (right as Regular).value)

            val l = left.reduceExplodes(depth + 1)
            if (l != null)
                return ExplosionResult(Pair(l.number, right.addToFirstRegular(l.rightResidual)), l.leftResidual, 0)

            val r = right.reduceExplodes(depth + 1)
            if (r != null)
                return ExplosionResult(Pair(left.addToLastRegular(r.leftResidual), r.number), 0, r.rightResidual)

            return null
        }

        override fun addToFirstRegular(n: Int) =
            Pair(left.addToFirstRegular(n), right)

        override fun addToLastRegular(n: Int) =
            Pair(left, right.addToLastRegular(n))

        override val magnitude: Int
            get() = 3 * left.magnitude + 2 * right.magnitude
    }

    companion object {
        operator fun invoke(s: String) = Parser(s).parse()
    }

    private class Parser(private val s: String) {
        private var pos = 0

        fun parse(): SnailfishNumber =
            if (s[pos] == '[') {
                pos++
                val left = parse()
                expect(',')
                val right = parse()
                expect(']')
                Pair(left, right)
            } else {
                Regular(buildString {
                    while (s[pos].isDigit())
                        append(s[pos++])
                }.toInt())
            }

        private fun expect(c: Char) {
            check(s[pos] == c)
            pos++
        }
    }
}
