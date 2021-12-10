private val matchingParenthesis = mapOf('(' to ')', '[' to ']', '{' to '}', '<' to '>')

fun main() {
    fun part1(input: List<String>): Int {
        val scores = mapOf(')' to 3, ']' to 57, '}' to 1197, '>' to 25137)

        fun score(line: String): Int {
            val stack = mutableListOf<Char>()
            for (c in line) {
                if (c in matchingParenthesis.keys)
                    stack.add(c)
                else if (c != matchingParenthesis[stack.removeLast()])
                    return scores[c]!!
            }

            return 0
        }

        return input.sumOf { score(it) }
    }

    fun part2(input: List<String>): Long {
        val scores = mapOf('(' to 1, '[' to 2, '{' to 3, '<' to 4)

        fun score(line: String): Long? {
            val stack = mutableListOf<Char>()
            for (c in line) {
                if (c in matchingParenthesis.keys)
                    stack.add(c)
                else if (c != matchingParenthesis[stack.removeLast()])
                    return null
            }

            return stack.map { scores[it]!! }
                .foldRight(0L) { score, total -> 5 * total + score }
        }

        return input.mapNotNull { score(it) }.median()
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}

