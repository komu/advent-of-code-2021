fun main() {

    fun Collection<String>.bitsMatch(bit: Int, greater: Boolean): Boolean {
        val ones = count { it[bit] == '1' }
        val zeros = size - ones
        return ones >= zeros == greater
    }

    fun part1(input: List<String>): Int {
        fun createNumber(greater: Boolean): Int {
            var n = 0

            val bits = input.first().length
            for (bit in 0 until bits) {
                n *= 2
                if (input.bitsMatch(bit, greater))
                    n += 1
            }

            return n
        }

        val epsilon = createNumber(greater = true)
        val gamma = createNumber(greater = false)

        return epsilon * gamma
    }

    fun part2(input: List<String>): Int {
        fun process(greater: Boolean): Int {
            val candidates = input.toMutableSet()

            val bits = input.first().length
            for (bit in 0 until bits) {
                if (candidates.size == 1)
                    break

                val requiredBit = if (candidates.bitsMatch(bit, greater)) '1' else '0'
                candidates.removeIf { it[bit] != requiredBit }
            }

            return candidates.first().bitsToInt()
        }

        val oxygen = process(greater = true)
        val scrubber = process(greater = false)
        return oxygen * scrubber
    }

    val testInput = readInput("Day03_test")
    println(part1(testInput))
    println(part2(testInput))
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
    check(part1(input) == 1307354)
    check(part2(input) == 482500)
}
