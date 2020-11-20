package org.thelemistix.kotme

import kotlin.random.Random

fun checkCard(cardName: String): String {
    val codes = HashMap<Int, String>()
    Main.eval("val $cardName = Card()")

    for (j in 0 until 10) {
        val code = Random.nextInt(100, 999)
        val value = "Пом. ${Random.nextInt(1, 400)}"

        codes[code] = value

        Main.eval("$cardName[$code] = \"$value\")")
    }

    codes.entries.forEach {
        if (Main.eval("$cardName[${it.key}]") != codes[it.key]) {
            return "Карта памяти не смогла выдать пару ${it.key} -> ${it.value}"
        }
    }

    Main.eval("device.card = $cardName")
    codes.entries.forEach {
        if (Main.eval("device.getInfo(${it.key})") != codes[it.key]) {
            return "Прибор не смог выдать пару ${it.key} -> ${it.value}"
        }
    }

    val actualCodes = Main.eval("device.getCodes()") as List<Int>
    if (actualCodes.size < codes.size) {
        return "getCodes выдал не все коды, либо выдал пустой список"
    }

    for (i in actualCodes.indices) {
        val code = actualCodes[i]
        if (!codes.containsKey(code)) {
            return "getCodes выдал не все коды, не хватает $code"
        }
    }

    return ""
}

fun exe7(): String {
    Main.eval("val device = Device()")

    var result = checkCard("card1")
    if (result.isNotEmpty()) return result

    result = checkCard("card2")
    if (result.isNotEmpty()) return result

    return ""
}
