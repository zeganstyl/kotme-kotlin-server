import kotlin.random.Random

fun exe4(): String {
    fun test4(k: Int, c: Int): String {
        fun checkItems(items: List<String>): List<String> {
            val newItems = ArrayList<String>()

            var logs = items.count { it == "бревно" }
            var vine = items.count { it == "лоза" }
            var fish = items.count { it == "рыба" }
            var coconuts = items.count { it == "кокос" }

            while (logs >= 8 && vine >= 20) {
                newItems.add("плот")
                logs -= 8
                vine -= 20
            }

            while (fish >= 5 && coconuts >= 2) {
                newItems.add("обед")
                fish -= 5
                coconuts -= 2
            }

            return newItems
        }

        for (j in 0 until 10) {
            val numLogs = Random.nextInt(5 * k + c, 20 * k + c)
            val numVines = Random.nextInt(10 * k + c, 50 * k + c)
            val numFish = Random.nextInt(10 * k + c)
            val numCoconuts = Random.nextInt(5 * k + c)

            val items = ArrayList<String>()

            for (i in 0 until numLogs) {
                items.add("бревно")
            }
            for (i in 0 until numVines) {
                items.add("лоза")
            }
            for (i in 0 until numFish) {
                items.add("рыба")
            }
            for (i in 0 until numCoconuts) {
                items.add("кокос")
            }

            Main.bindings["items"] = items

            val actualItems = Main.eval("checkItems(items as List<String>)") as List<String>

            val expectedItems = checkItems(items)
            val raftsExp = expectedItems.count { it == "плот" }
            val raftsAct = actualItems.count { it == "плот" }
            val lunchExp = expectedItems.count { it == "обед" }
            val lunchAct = actualItems.count { it == "обед" }

            if (raftsExp != raftsAct || lunchExp != lunchAct) {
                return """
Имелось:
бревна: $numLogs
лозы: $numVines
рыба: $numFish
кокосы: $numCoconuts

Ваш код посчитал:
плоты: $raftsAct
обеды: $lunchAct

Но ожидалось:
плоты: $raftsExp
обеды: $lunchExp
"""
            }
        }

        return ""
    }

    var str: String = test4(1, 0)
    if (str.isNotEmpty()) return str

    str = test4(3, 10)
    if (str.isNotEmpty()) return str

    return ""
}

fun exe5(): String {
    fun test4(k: Int, c: Int): String {
        fun checkItems(items: List<String>): Map<String, Int> {
            val newItems = HashMap<String, Int>()

            for (i in items.indices) {
                val item = items[i]
                newItems[item] = (newItems[item] ?: 0) + 1
            }

            return newItems
        }

        for (j in 0 until 10) {
            val numLogs = Random.nextInt(5 * k + c, 20 * k + c)
            val numVines = Random.nextInt(10 * k + c, 50 * k + c)
            val numFish = Random.nextInt(10 * k + c)
            val numCoconuts = Random.nextInt(5 * k + c)

            val items = ArrayList<String>()

            for (i in 0 until numLogs) {
                items.add("бревно")
            }
            for (i in 0 until numVines) {
                items.add("лоза")
            }
            for (i in 0 until numFish) {
                items.add("рыба")
            }
            for (i in 0 until numCoconuts) {
                items.add("кокос")
            }

            Main.bindings["items"] = items

            val actualItems = Main.eval("itemsCounting(items as List<String>)") as Map<String, Int>

            val expectedItems = checkItems(items)

            if (actualItems.size != expectedItems.size) {
                return "В вашем массиве не хватает предметов, либо есть лишние"
            }

            expectedItems.entries.forEach {
                if (actualItems[it.key] != it.value) {
                    return """
Ваш код посчитал:
${it.key} - ${actualItems[it.key]}

Но ожидалось:
${it.key} - ${it.value}
"""
                }
            }
        }

        return ""
    }

    var str: String = test4(1, 0)
    if (str.isNotEmpty()) return str

    str = test4(3, 10)
    if (str.isNotEmpty()) return str

    return ""
}
