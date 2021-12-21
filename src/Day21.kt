fun main() {
    fun part1(player1Start: Int, player2Start: Int): Int {
        var dieNext = 1
        var rolls = 0

        fun roll(): Int {
            val value = dieNext
            rolls++
            dieNext = (dieNext % 100) + 1
            return value
        }

        var player1 = DiracPlayer(1, player1Start)
        var player2 = DiracPlayer(2, player2Start)

        while (true) {
            player1 = player1.move(roll() + roll() + roll())
            if (player1.score >= 1000)
                return player2.score * rolls
            player2 = player2.move(roll() + roll() + roll())
            if (player2.score >= 1000)
                return player1.score * rolls
        }
    }

    fun part2(player1Start: Int, player2Start: Int): Long {
        val moveDistribution = mapOf(3 to 1L, 4 to 3L, 5 to 6L, 6 to 7L, 7 to 6L, 8 to 3L, 9 to 1L)
        var universes = listOf(DiracUniverse(DiracPlayer(1, player1Start), DiracPlayer(2, player2Start)))
        val wins = mutableListOf(0L, 0L)

        while (universes.isNotEmpty()) {
            val nextUniverses = ArrayList<DiracUniverse>()
            for (universe in universes) {
                if (universe.previousPlayer.score >= 21) {
                    wins[universe.previousPlayer.id - 1] += universe.count

                } else {
                    for ((steps, count) in moveDistribution)
                        nextUniverses += universe.move(steps, count)
                }
            }

            // merge duplicate universes
            universes = nextUniverses.groupBy { Pair(it.playerInTurn, it.previousPlayer) }.map { (k, vs) ->
                DiracUniverse(k.first, k.second, vs.sumOf { it.count })
            }
        }

        return wins.maxOrNull()!!
    }

    checkEqual(part1(player1Start = 4, player2Start = 8), 739785)
    checkEqual(part2(player1Start = 4, player2Start = 8), 444356092776315)

    println(part1(player1Start = 1, player2Start = 5))
    checkEqual(part1(player1Start = 1, player2Start = 5), 432450)
    println(part2(player1Start = 1, player2Start = 5))
    checkEqual(part2(player1Start = 1, player2Start = 5), 138508043837521)
}

private class DiracUniverse(val playerInTurn: DiracPlayer, val previousPlayer: DiracPlayer, val count: Long = 1) {

    fun move(steps: Int, count: Long) = DiracUniverse(
        playerInTurn = previousPlayer,
        previousPlayer = playerInTurn.move(steps),
        count = this.count * count
    )
}

private data class DiracPlayer(val id: Int, val pos: Int, val score: Int = 0) {

    fun move(steps: Int): DiracPlayer {
        val newPos = (pos - 1 + steps) % 10 + 1
        return DiracPlayer(
            id = id,
            pos = newPos,
            score = score + newPos
        )
    }
}
