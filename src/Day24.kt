@file:Suppress("DuplicatedCode")

fun main() {
    fun solve(options: IntProgression): Long {
        val s0 = ChecksumState(0)

        for (n1 in options) {
            val s1 = s0.push(n1 + 15)
            for (n2 in options) {
                val s2 = s1.push(n2 + 16)
                for (n3 in options) {
                    val s3 = s2.push(n3 + 4)
                    for (n4 in options) {
                        val s4 = s3.push(n4 + 14)

                        val n5 = s4.expected(8)
                        if (n5 !in options) continue
                        val s5 = s4.pop()

                        val n6 = s5.expected(10)
                        if (n6 !in options) continue
                        val s6 = s5.pop()

                        for (n7 in options) {
                            val s7 = s6.push(n7 + 1)

                            val n8 = s7.expected(3)
                            if (n8 !in options) continue
                            val s8 = s7.pop()

                            for (n9 in options) {
                                val s9 = s8.push(n9 + 3)

                                val n10 = s9.expected(4)
                                if (n10 !in options) continue
                                val s10 = s9.pop()

                                for (n11 in options) {
                                    val s11 = s10.push(n11 + 5)

                                    val n12 = s11.expected(5)
                                    if (n12 !in options) continue
                                    val s12 = s11.pop()

                                    val n13 = s12.expected(8)
                                    if (n13 !in options) continue
                                    val s13 = s12.pop()

                                    val n14 = s13.expected(11)
                                    if (n14 !in options) continue

                                    return "$n1$n2$n3$n4$n5$n6$n7$n8$n9$n10$n11$n12$n13$n14".toLong()
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0
    }

    fun part1() =
        solve(9 downTo 1)

    fun part2() =
        solve(1..9)

//    translateProgram(readInput("Day24"))

    val result1 = part1()
    println(result1)
    checkEqual(result1, 51939397989999)

    val result2 = part2()
    println(result2)
    checkEqual(result2, 11717131211195)
}

@JvmInline
private value class ChecksumState(private val state: Int) {

    fun push(value: Int) =
        ChecksumState(state * 26 + value)

    fun expected(magic: Int) =
        state % 26 - magic

    fun pop() =
        ChecksumState(state / 26)
}

/**
 * Translates the input program to Kotlin code so that it's easier to analyze
 */
@Suppress("unused")
private fun translateProgram(input: List<String>) {
    println(parseProgram(input)
        .rewriteVariables()
        .propagateConstants()
        .simplify()
        .propagateConstants()
        .simplify()
        .toKotlin())
}

private fun List<Assign>.toKotlin(): String {
    val prefix = """
        private fun evaluate(input: List<Int>): Int {
    """.trimIndent()

    val lines = this.joinToString("\n") { "    val ${it.target} = ${it.exp.toKotlin()}" }

    val suffix = """
            return ${this.last().target}
        }
    """.trimIndent()

    return "$prefix\n$lines\n$suffix"
}

private data class Assign(val target: Var, val exp: Expression)

private enum class BinOp(val op: String) {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    EQL("eql"),
    NOT_EQL("noteql")
}

private sealed class Expression {

    abstract fun toKotlin(prec: Int = 0): String
    abstract fun rewriteVariables(mapping: Map<Var, Expression>): Expression
    abstract fun countUses(mapping: MutableMap<Var, Int>)
    abstract fun simplify(): Expression
}

private class BinaryExpression(val op: BinOp, val lhs: Expression, val rhs: Expression) : Expression() {
    override fun toKotlin(prec: Int): String {
        val l = lhs.toKotlin(1)
        val r = rhs.toKotlin(1)
        val s = when (op) {
            BinOp.EQL -> "($l == $r).toInt()"
            BinOp.NOT_EQL -> "($l != $r).toInt()"
            else -> "$l ${op.op} $r"
        }
        return if (prec == 0) s else "($s)"
    }

    override fun rewriteVariables(mapping: Map<Var, Expression>) =
        BinaryExpression(op, lhs.rewriteVariables(mapping), rhs.rewriteVariables(mapping))

    override fun simplify(): Expression {
        val l = lhs.simplify()
        val r = rhs.simplify()

        return when {
            op == BinOp.MUL && r is Constant && r.value == 0 -> Constant(0)
            op == BinOp.MUL && l is Constant && l.value == 0 -> Constant(0)
            op == BinOp.DIV && l is Constant && l.value == 0 -> Constant(0)
            op == BinOp.MOD && l is Constant && l.value == 0 -> Constant(0)
            op == BinOp.ADD && l is Constant && l.value == 0 -> r
            op == BinOp.ADD && r is Constant && r.value < 0 -> BinaryExpression(BinOp.SUB, l, Constant(-r.value))
            op == BinOp.EQL && l is BinaryExpression && l.op == BinOp.EQL && r is Constant && r.value == 0 -> BinaryExpression(
                BinOp.NOT_EQL,
                l.lhs,
                l.rhs)
            l is Constant && r !is Constant -> BinaryExpression(op, r, l)
            else -> BinaryExpression(op, l, r)
        }
    }

    override fun countUses(mapping: MutableMap<Var, Int>) {
        lhs.countUses(mapping)
        rhs.countUses(mapping)
    }
}

private data class Var(private val name: String) : Expression() {
    override fun toString() = toKotlin()
    override fun toKotlin(prec: Int) = name
    override fun rewriteVariables(mapping: Map<Var, Expression>) = mapping[this] ?: this
    override fun countUses(mapping: MutableMap<Var, Int>) {
        mapping[this] = (mapping[this] ?: 0) + 1
    }

    override fun simplify() = this
}

private data class Constant(val value: Int) : Expression() {
    override fun toKotlin(prec: Int) = value.toString()
    override fun toString() = toKotlin()
    override fun rewriteVariables(mapping: Map<Var, Expression>) = this
    override fun countUses(mapping: MutableMap<Var, Int>) {
    }

    override fun simplify() = this
}

private data class Inp(val index: Int) : Expression() {
    override fun toKotlin(prec: Int) = "input[$index]"
    override fun rewriteVariables(mapping: Map<Var, Expression>) = this
    override fun countUses(mapping: MutableMap<Var, Int>) {
    }

    override fun simplify() = this
}

private fun parseProgram(lines: List<String>): List<Assign> {
    val regex = Regex("""(\w+) (\w)( (.+))?""")
    var inputIndex = 0
    return lines.map { line ->
        val (op, op1, _, op2) = regex.matchEntire(line)?.destructured ?: error("invalid op '$line'")
        if (op == "inp") Assign(op1.toRegister(), Inp(inputIndex++))
        else
            Assign(op1.toRegister(),
                BinaryExpression(BinOp.valueOf(op.uppercase()), op1.toRegister(), op2.toSource()))
    }
}

private fun List<Assign>.rewriteVariables(): List<Assign> {
    var index = 1
    val result = mutableListOf<Assign>()
    val assignments = mutableMapOf<Var, Var>()

    for (assign in this) {
        val newVar = Var("${assign.target}${index++}")
        result += Assign(newVar, assign.exp.rewriteVariables(assignments))
        assignments[assign.target] = newVar
    }

    return result
}

private fun List<Assign>.simplify(): List<Assign> =
    map { Assign(it.target, it.exp.simplify()) }

private fun List<Assign>.propagateConstants(): List<Assign> {
    var instructions = this
    while (true) {
        val useMap = mutableMapOf(
            instructions.last().target to 1
        )
        for (assign in instructions)
            assign.exp.countUses(useMap)

        val props = mutableMapOf<Var, Expression>()
        val result = mutableListOf<Assign>()
        for (assign in instructions) {
            val uses = useMap[assign.target] ?: 0
            if (uses == 0) {
                // drop
            } else if (uses == 1 && assign != instructions.last()) {
                props[assign.target] = assign.exp.rewriteVariables(props)
            } else if (assign.exp is Inp || assign.exp is Var) {
                props[assign.target] = assign.exp.rewriteVariables(props)
            } else {
                result += Assign(assign.target, assign.exp.rewriteVariables(props))
            }
        }

        if (instructions.size == result.size)
            return result

        instructions = result
    }
}

private fun String.toRegister() = Var(this)
private fun String.toSource() =
    if (first().isDigit() || first() == '-') Constant(toInt()) else Var(this)
