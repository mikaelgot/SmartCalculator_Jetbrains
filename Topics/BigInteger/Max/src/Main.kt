import java.math.BigInteger

fun main() {
    val a = readln().trim().toBigInteger()
    val b = readln().trim().toBigInteger()
    print(max(a, b))
}

fun max(a: BigInteger, b:BigInteger): BigInteger {
    val add = a + b
    val minus = (a - b).abs()
    return (add + minus) / 2.toBigInteger()
}
