import kotlin.random.Random

fun exe6(): String {
    val codes = HashMap<Int, String>()

    Main.eval("val device = Device()")

    for (j in 0 until 10) {
        val code = Random.nextInt(100, 999)
        val value = "Пом. ${Random.nextInt(1, 400)}"

        codes[code] = value

        Main.eval("device.putCode($code, \"$value\")")
    }

    codes.entries.forEach {
        if (Main.eval("device.getInfo(${it.key})") != codes[it.key]) {
            return "getInfo не смог выдать пару ${it.key} -> ${it.value}"
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