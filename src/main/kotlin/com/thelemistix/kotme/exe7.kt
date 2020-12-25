package com.thelemistix.kotme

import kotlin.random.Random

fun cardCheckFun(rand: Int, classConstruct: String = "Card()", args: String = ""): String {
    return """
fun checkCard$rand($args) {
    val card = $classConstruct
    val codes = HashMap<Int, String>()
    
    for (j in 0 until 10) {
        val code = kotlin.random.Random.nextInt(100, 999)
        val value = "Пом. ${Random.nextInt(1, 400)}"
    
        codes[code] = value
    
        card[code] = value
    }
    
    codes.entries.forEach {
        if (card[it.key] != codes[it.key]) {
            throw Exception("Карта памяти не смогла выдать пару ${'$'}{it.key} -> ${'$'}{it.value}")
        }
    }
    
    device.card = card
    
    codes.entries.forEach {
        if (device.getInfo(it.key) != codes[it.key]) {
            throw Exception("Прибор не смог выдать пару ${'$'}{it.key} -> ${'$'}{it.value}")
        }
    }
    
    val actualCodes = device.getCodes()
    if (actualCodes.size < codes.size) {
        throw Exception("getCodes выдал не все коды, либо выдал пустой список")
    }
    
    for (i in actualCodes.indices) {
        val code = actualCodes[i]
        if (!codes.containsKey(code)) {
            throw Exception("getCodes выдал не все коды, не хватает ${'$'}code")
        }
    }
}
"""
}

// Ready
fun exe7(code: String): EvalResult {
    val rand = rand()

    return Main.eval(code + """
val device = Device()
${cardCheckFun(rand, "Card()")}
checkCard$rand()
""")
}
