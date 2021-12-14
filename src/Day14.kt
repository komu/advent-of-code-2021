import java.lang.StringBuilder

fun main() {

    fun part1(input: List<String>): Int {
        val (template, rules) = parsePolymerRules(input)

        fun applyRules(value: String, rules: Map<String, Char>): String {
            val sb = StringBuilder(value.length * 2)

            for (window in value.windowed(2))
                sb.append(window.first()).append(rules[window])

            sb.append(value.last())
            return sb.toString()
        }

        var value = template
        repeat(10) {
            value = applyRules(value, rules)
        }

        val counts = value.groupingBy { it }.eachCount()

        return counts.maxOf { it.value } - counts.minOf { it.value }
    }

    fun part2(input: List<String>, steps: Int = 40): Long {
        val (template, rules) = parsePolymerRules(input)

        data class CacheKey(val left: Char, val right: Char, val level: Int)

        val cache = mutableMapOf<CacheKey, CharacterCounts>()

        fun recurse(left: Char, right: Char, level: Int): CharacterCounts = cache.getOrPut(CacheKey(left, right, level)) {
            if (level <= 0)
                CharacterCounts.single(left)
            else {
                val replacement = rules["$left$right"]!!
                recurse(left, replacement, level - 1) + recurse(replacement, right, level - 1)
            }
        }

        var counts = CharacterCounts.single(template.last())
        for (w in template.windowed(2))
            counts += recurse(w[0], w[1], steps)

        return counts.max - counts.min
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588)
    check(part2(testInput) == 2188189693529)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}

private class CharacterCounts(private val counts: Map<Char, Long>) {

    operator fun plus(rhs: CharacterCounts): CharacterCounts =
        CharacterCounts((this.counts.keys + rhs.counts.keys).associateWith { (counts[it] ?: 0L) + (rhs.counts[it] ?: 0L) })

    val min: Long
        get() = counts.minOf { it.value }

    val max: Long
        get() = counts.maxOf { it.value }

    companion object {
        fun single(char: Char) = CharacterCounts(mapOf(char to 1L))
    }
}

private val polymerRulePattern = Regex("(.+) -> (.+)")

private fun parsePolymerRules(input: List<String>): Pair<String, Map<String, Char>> {
    val template = input[0]
    val rules = input.drop(2).associate {
        val (_, from, to) = polymerRulePattern.matchEntire(it)!!.groupValues
        from to to[0]
    }

    return Pair(template, rules)
}
