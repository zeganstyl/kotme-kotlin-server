package com.kotme.exercise

import com.kotme.Main

// Ready
fun exe6(code: String): EvalResult {
    return Main.eval(
        code + """
val device = Device()

val codes = HashMap<Int, String>()
for (j in 0 until 10) {
    val code = kotlin.random.Random.nextInt(100, 999)
    val value = "Пом. ${'$'}{kotlin.random.Random.nextInt(1, 400)}"

    codes[code] = value

    device.putCode(code, value.toString())
}

codes.entries.forEach {
    if (device.getInfo(it.key) != codes[it.key]) {
        throw Exception("getInfo не смог выдать пару ${'$'}{it.key} -> ${'$'}{it.value}")
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
"""
    )
}