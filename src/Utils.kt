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

fun Boolean.toLong(): Long =
    if (this) 1 else 0

fun Boolean.toInt(): Int =
    if (this) 1 else 0

fun CharSequence.bitsToInt(): Int {
    var i = 0

    for (b in this) {
        i *= 2
        if (b == '1')
            i += 1
    }

    return i
}

fun Iterable<Int>.product() = fold(1) { x, y -> x * y }
inline fun <T> Iterable<T>.productOf(f: (T) -> Long) = fold(1L) { p, x -> p * f(x) }

fun <T> List<List<T>>.transpose(): List<List<T>> {
    val rows = size
    val cols = first().size

    return List(cols) { col -> List(rows) { row -> this[row][col] } }
}

data class Point(val x: Int, val y: Int) {
    val neighbors: List<Point>
        get() = primaryDirections.map { (dx, dy) -> Point(x + dx, y + dy) }

    val allNeighbors: List<Point>
        get() = allDirections.map { (dx, dy) -> Point(x + dx, y + dy) }

    companion object {
        private val primaryDirections = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        private val allDirections =
            listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
    }
}

fun <T : Comparable<T>> List<T>.median(): T =
    sorted()[size / 2]

fun <T> checkEqual(lhs: T, rhs: T) {
    if (lhs != rhs)
        error("$lhs != $rhs")
}

