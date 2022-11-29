import java.math.BigInteger
import kotlin.math.pow

val store = mutableMapOf<String, BigInteger>()
var values = mutableListOf<BigInteger>()
var ops = mutableListOf<String>()

fun main() {
    val regexCom = "\\/\\w+".toRegex()
    val regexSet = "\\w+(\\s*\\=\\s*[-+]*\\w+)+".toRegex()
    val regexGet = "[A-Za-z]+".toRegex()
    val regexNum = "[-+]?\\d+".toRegex()
    val regexExp = "\\(*[-+]?\\w+(\\s*([+]*|[-]*|\\/|\\*|\\^)\\s*\\(*[-+]?\\w+\\)*)+".toRegex()
    while(true) {
        val input = readln().trim()
        if ( input.matches(regexCom) ) { //input is a command
            if (input == "/exit") {
                println("Bye!")
                break
            } else if (input == "/help") {
                println("The program calculates the sum of numbers")
            }
            else println("Unknown command")
        }
        else if (input.matches(regexGet)) {
            println(getItem(input))
        }
        else if(input.matches(regexNum)) {
            println(input.toInt())
        }
        else if (input.matches(regexExp) && input.count{it == '('} == input.count{ it == ')'}) {
            println(workInput(input))
        }
        else if (input.matches(regexSet)) {
            addItem(input)
        }
        else if (input == "") {}
        else println("Invalid expression")
    }
}

fun getItem(input: String): String {
    val regexValid = "[A-Za-z]+".toRegex()
    if (!input.matches(regexValid)) {
        return "Invalid identifier"
    }
    else {
        if (store.containsKey(input)) {
            //println("input = $input, value = ${store[input].toString()}")
            return store[input].toString()
        }
        else {
            return "Unknown variable"
        }
    }
}

fun addItem(input:String) {
    val nameEqVal = input.split("\\s*\\=\\s*".toRegex())
    //println("nameEqualsvalue: $nameEqVal, size: ${nameEqVal.size}")
    if(!nameEqVal[0].matches("[A-Za-z]+".toRegex())){   //Invalid name
        println("Invalid identifier")
    }
    else if (nameEqVal.size == 2) { //Valid name and 2 arguments
        //println("Valid name")
        if (nameEqVal[1].matches("[-+]?\\d+".toRegex())) {
            //println("Valid name with integer input")
            store[nameEqVal[0]] = nameEqVal[1].toBigInteger()
        }
        else if (nameEqVal[1].matches("[A-Za-z]+".toRegex())) {
            //println("Valid name with text input")
            if (!store.containsKey(nameEqVal[1])) { // Variable not set
                println("Unknown variable")
            }
            else { //Previously set variable
                //println("Previously set nameEqVal[1] =  ${store[nameEqVal[1]]}")
                store[nameEqVal[0]] = store.getValue(nameEqVal[1])
            }
        }
        else println("Invalid assignment")
    }
    else println("Invalid assignment")
}

fun workInput(equation: String): String {
    //val equation = "7 ++++ 3 * ((4 + 3) * 7 ++ 1) ----- 6 / (2 ---- 1)"
    //val equation = "33 + 20 + 11 + 49 - 32 - 9 + 1 - 80 + 4"
    var eq = equation.replace("\\s+".toRegex(), "").replace("[+]+".toRegex(), "+")
    val minRegex = "[-]{2,}".toRegex()
    while (eq.contains(minRegex)) {
        val multiMinuses = minRegex.find(eq)
        val replacement = if (multiMinuses!!.value.length %2 == 0) "+" else "-"
        eq  = eq.replaceFirst(minRegex, replacement)
    }
    val varRegex = "[A-Za-z]+".toRegex()
    while (eq.contains(varRegex)) {
        //println("equation $eq contains variable")
        val variable = varRegex.find(eq)!!.value
        val varValue = getItem(variable)
        if (varValue.toBigIntegerOrNull() == null) return "Unknown variable"
        eq = eq.replace(variable, varValue)
    }
    //println("main: Starting eq: $eq")
    val op = "[\\^\\*\\/\\+\\-]"
    val parRegex  = "\\(\\d+($op\\d+)+\\)".toRegex()

    while (eq.contains(parRegex)) {
        //println("eq contains parentheses")
        val firstPar = parRegex.find(eq)
        val parEq = firstPar!!.value.drop(1).dropLast(1)
        eq = eq.replace(firstPar.value, solveBasic(parEq) )
    }
    eq = solveBasic(eq)
    //println(eq)
    return eq
}

fun fixDoubleOps(eq: String): String {
    val doubleRegex = "[-+][-+]".toRegex()
    var eq = eq
    while (eq.contains(doubleRegex)) {
        val doubleSign = doubleRegex.find(eq)!!.value
        val sign = when (doubleSign) {
            "--" -> "+"
            "-+" -> "-"
            "++" -> "+"
            "+-" -> "-"
            else -> "+"
        }
        eq = eq.replace(doubleSign, sign)
    }
    return eq
}

fun solveBasic(eq: String): String {
    val op = "[\\^\\*\\/\\+\\-]"
    val equat = fixDoubleOps(eq)
    val all = equat.split("(?<=$op)|(?=$op)".toRegex())
    //println("all: $all")
    values = all.filterIndexed { index , _-> index % 2 == 0  }.map { it.toBigInteger() }.toMutableList()
    ops = all.filterIndexed { index , _-> index % 2 == 1  }.toMutableList()

    //printStatus()
    while (ops.isNotEmpty()) {
        when {
            ops.contains("^") -> calcPow()
            ops.contains("*") -> calcMulti()
            ops.contains("/") -> calcDiv()
            ops.contains("+") || ops.contains("-") -> calcPlusMinus()
            else -> Unit
        }
        //printStatus()
    }
    return values[0].toString()
}

fun calcPow() {
    //println("Powering")
    val i = ops.indexOf("^")
    val res = values[i].toFloat().pow(values[i + 1].toFloat()).toInt().toBigInteger()
    updateLists(i, res)
}
fun calcMulti() {
    //println("Multiplication")
    val i = ops.indexOf("*")
    val res = values[i] * values[i + 1]
    updateLists(i, res)
}
fun calcDiv() {
    //println("Division")
    val i = ops.indexOf("/")
    val res = values[i] / values[i + 1]
    updateLists(i, res)
}
fun calcPlusMinus() {
    //println("Adding or Subtracting")
    val plusIndex = ops.indexOf("+")
    val minusIndex = ops.indexOf("-")
    if(plusIndex in 0 until minusIndex || minusIndex < 0) {
        val res = values[plusIndex] + values[plusIndex + 1]
        updateLists(plusIndex, res)
    }
    else {
        val res = values[minusIndex] - values[minusIndex + 1]
        updateLists(minusIndex, res)
    }
}

fun printStatus() {
    println("values: $values")
    println("ops: $ops")
}

fun updateLists(i: Int, res: BigInteger) {
    ops.removeAt(i)
    values[i] = res
    values.removeAt(i + 1)
}