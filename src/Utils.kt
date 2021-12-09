import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun String.bitsToInt(): Int {
    var i = 0

    for (b in this) {
        i *= 2
        if (b == '1')
            i += 1
    }

    return i
}

fun Iterable<Int>.product() = fold(1) { x, y -> x * y }

fun <T> List<List<T>>.transpose(): List<List<T>> {
    val rows = size
    val cols = first().size

    return List(cols) { col -> List(rows) { row -> this[row][col] } }
}

data class Point(val x: Int, val y: Int) {
    val neighbors: List<Point>
        get() = primaryDirections.map { (dx, dy) -> Point(x + dx, y + dy) }

    companion object {
        private val primaryDirections = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
    }
}
