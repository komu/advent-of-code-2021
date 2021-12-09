private val SEGMENT_CHARACTERS = "abcdefg".toSet()
private val DIGIT_MAPPINGS = listOf(
    "abcefg", "cf", "acdeg", "acdfg", "bcdf", "abdfg", "abdefg", "acf", "abcdefg", "abcdfg"
).map(::SignalPattern)

private class SegmentConstraints {
    private val signalConstraints = SEGMENT_CHARACTERS.associateWith { SEGMENT_CHARACTERS.toMutableSet() }

    fun equalToSome(real: Char, data: Set<Char>) {
        val constraints = signalConstraints[real]!!
        constraints.retainAll(data)

        if (constraints.size == 1) {
            val d = constraints.first()
            for (other in signalConstraints.keys)
                if (other != real)
                    signalConstraints[other]!!.remove(d)
        }
    }

    fun notEqualToAny(real: Char, data: Set<Char>) {
        signalConstraints[real]!!.removeAll(data)
    }

    fun bestCandidate(remaining: Set<Char>) =
        signalConstraints.entries.filter { it.key in remaining }.minByOrNull { it.value.size }!!
}

private class WireAssignments private constructor(private val assignments: Map<Char, Char>) {

    constructor() : this(emptyMap())

    fun extend(src: Char, dst: Char) =
        WireAssignments(assignments + (src to dst))

    fun validate(inputs: Collection<SignalPattern>) =
        inputs.mapNotNull { resolve(it) }.toSet().size == 10

    fun resolve(code: SignalPattern): Int? {
        for ((digit, pattern) in DIGIT_MAPPINGS.withIndex())
            if (apply(pattern) == code)
                return digit

        return null
    }

    private fun apply(s: SignalPattern): SignalPattern =
        SignalPattern(s.wires.map { assignments[it]!! }.toSet())
}

private data class SignalPattern(val wires: Set<Char>) {
    constructor(s: String) : this(s.toSet())

    val size: Int
        get() = wires.size

    companion object {
        val unknown = SignalPattern(emptySet())
    }
}

private fun solve(
    constraints: SegmentConstraints,
    inputs: Collection<SignalPattern>
): WireAssignments {

    fun recurse(assignment: WireAssignments, remaining: Set<Char>): WireAssignments? {
        if (remaining.isEmpty())
            return if (assignment.validate(inputs)) assignment else null

        val (wire, candidates) = constraints.bestCandidate(remaining)
        for (candidate in candidates) {
            val solution = recurse(assignment.extend(wire, candidate), remaining - wire)
            if (solution != null)
                return solution
        }

        return null
    }

    return recurse(WireAssignments(), SEGMENT_CHARACTERS) ?: error("failed to solve $inputs")
}

fun main() {

    fun part1(input: List<String>) = input.sumOf { line ->
        val measurement = SegmentMeasurement.parse(line)
        val knownNumbers = mutableSetOf<SignalPattern>()
        for (measurementInput in measurement.inputs)
            if (measurementInput.size in setOf(2, 3, 4, 7))
                knownNumbers += measurementInput

        measurement.outputs.count { it in knownNumbers }
    }

    fun process(inputs: Collection<SignalPattern>): WireAssignments {
        val constraints = SegmentConstraints()

        var one = SignalPattern.unknown
        var seven = SignalPattern.unknown
        var four = SignalPattern.unknown
        for (input in inputs) {
            when (input.size) {
                2 ->
                    one = input
                3 ->
                    seven = input
                4 ->
                    four = input
                else -> {
                }
            }
        }

        constraints.equalToSome('a', seven.wires - one.wires)

        constraints.equalToSome('c', one.wires)
        constraints.equalToSome('f', one.wires)

        constraints.equalToSome('b', four.wires)
        constraints.equalToSome('c', four.wires)
        constraints.equalToSome('d', four.wires)
        constraints.equalToSome('f', four.wires)

        constraints.equalToSome('a', seven.wires)
        constraints.equalToSome('c', seven.wires)
        constraints.equalToSome('f', seven.wires)

        constraints.notEqualToAny('a', one.wires)
        constraints.notEqualToAny('b', one.wires)
        constraints.notEqualToAny('d', one.wires)
        constraints.notEqualToAny('e', one.wires)
        constraints.notEqualToAny('g', one.wires)

        constraints.notEqualToAny('b', seven.wires)
        constraints.notEqualToAny('d', seven.wires)
        constraints.notEqualToAny('e', seven.wires)
        constraints.notEqualToAny('g', seven.wires)

        return solve(constraints, inputs)
    }

    fun part2(input: List<String>): Int {
        var totalSum = 0
        for (line in input) {
            val measurement = SegmentMeasurement.parse(line)
            val assignments = process(measurement.inputs)

            var sum = 0
            for (output in measurement.outputs) {
                sum *= 10
                sum += assignments.resolve(output) ?: error("could not resolve")
            }
            totalSum += sum
        }
        return totalSum
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
    check(part1(input) == 321)
    check(part2(input) == 1028926)
}

private data class SegmentMeasurement(val inputs: List<SignalPattern>, val outputs: List<SignalPattern>) {
    companion object {
        fun parse(line: String): SegmentMeasurement {
            val (inputs, outputs) = line.split('|')

            return SegmentMeasurement(
                inputs = inputs.trim().split(' ').map { SignalPattern(it) },
                outputs = outputs.trim().split(' ').map { SignalPattern(it) },
            )
        }
    }
}
