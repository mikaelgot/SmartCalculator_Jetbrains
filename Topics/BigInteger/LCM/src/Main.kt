import java.math.BigInteger

fun main() {
    val a = readln().trim().toBigInteger()
    val b = readln().trim().toBigInteger()
    val (q, r) = a.divideAndRemainder(b)
    print("$a = $b * $q + $r")
}

