package com.thelemistix.kotme

// Ready
fun exe8(code: String): EvalResult {
    val rand = rand()

    return Main.eval(code + """
val device = Device()
${cardCheckFun(rand, "cardIn", "cardIn: ICard")}

checkCard$rand(BlueCard())
checkCard$rand(RedCard())

class PurpleCard: ICard {
    private val map = HashMap<Int, String>()

    override operator fun get(code: Int): String = map[code] ?: ""

    override operator fun set(code: Int, value: String) {
        map[code] = value
    }

    override fun getCodes(): List<Int> = map.keys.toList()
}

checkCard$rand(PurpleCard())
""")
}
