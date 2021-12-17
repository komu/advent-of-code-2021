fun main() {
    fun part1(input: String) =
        Packet.parse(BitStream.fromHexInput(input)).versionSum()

    fun part2(input: String) =
        Packet.parse(BitStream.fromHexInput(input)).evaluate()

    check(part1("8A004A801A8002F478") == 16)
    check(part1("620080001611562C8802118E34") == 12)
    check(part1("C0015000016115A2E0802F182340") == 23)
    check(part1("A0016C880162017C3686B18A3D4780") == 31)

    check(part2("C200B40A82") == 3L)
    check(part2("04005AC33890") == 54L)
    check(part2("880086C3E88112") == 7L)
    check(part2("CE00C43D881120") == 9L)
    check(part2("D8005AC2A8F0") == 1L)
    check(part2("9C005AC2F8F0") == 0L)
    check(part2("9C0141080250320F1802104A08") == 1L)

    val input = readInput("Day16").first()
    println(part1(input))
    println(part2(input))

    check(part1(input) == 927)
    check(part2(input) == 1725277876501)
}

private sealed class Packet {

    abstract fun versionSum(): Int
    abstract fun evaluate(): Long

    data class Literal(val version: Int, val value: Long) : Packet() {
        override fun versionSum() = version
        override fun evaluate() = value
    }

    data class Command(val version: Int, val type: Int, val children: List<Packet>) : Packet() {
        override fun versionSum() = version + children.sumOf { it.versionSum() }

        override fun evaluate(): Long = when (type) {
            0 -> children.sumOf { it.evaluate() }
            1 -> children.productOf { it.evaluate() }
            2 -> children.minOf { it.evaluate() }
            3 -> children.maxOf { it.evaluate() }
            5 -> (children[0].evaluate() > children[1].evaluate()).toLong()
            6 -> (children[0].evaluate() < children[1].evaluate()).toLong()
            7 -> (children[0].evaluate() == children[1].evaluate()).toLong()
            else -> error("invalid type $type")
        }
    }

    companion object {
        fun parse(bits: BitStream): Packet {
            val version = bits.readInt(3)
            val type = bits.readInt(3)

            if (type == 4) {
                var literal = 0L
                do {
                    val hasMore = bits.readInt(1) == 1
                    literal = (literal shl 4) + bits.readInt(4)
                } while (hasMore)

                return Literal(version, literal)
            } else {
                val lengthTypeId = bits.readInt(1)

                val packets = mutableListOf<Packet>()
                if (lengthTypeId == 0) {
                    val substream = bits.substream(bits.readInt(15))
                    while (substream.hasMore)
                        packets.add(parse(substream))
                } else {
                    val subPacketCount = bits.readInt(11)
                    repeat(subPacketCount) {
                        packets.add(parse(bits))
                    }
                }

                return Command(version, type, packets)
            }
        }
    }
}

private class BitStream private constructor(private val bits: CharSequence) {

    private var pos = 0

    val hasMore: Boolean
        get() = pos < bits.length

    fun substream(len: Int): BitStream {
        val bits = bits.subSequence(pos, pos + len)
        pos += len
        return BitStream(bits)
    }

    fun readInt(len: Int): Int {
        val bits = bits.subSequence(pos, pos + len)
        pos += len
        return bits.bitsToInt()
    }

    companion object {
        private val binaryDigits = listOf(
            "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"
        )

        fun fromHexInput(s: String) = BitStream(decodeHex(s))

        private fun decodeHex(input: String): String =
            input.flatMap { binaryDigits[it.digitToInt(16)].toList() }.joinToString("")

    }
}
