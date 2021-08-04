package com.kotme

import kotlin.random.Random

// Ready
fun exe4(code: String): EvalResult {
    val rand = rand()

    return Main.eval(
        code + """
fun checkItems$rand(items: List<String>): List<String> {
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

fun test$rand(k: Int, c: Int) {
    for (j in 0 until 10) {
        val numLogs = kotlin.random.Random.nextInt(5 * k + c, 20 * k + c)
        val numVines = kotlin.random.Random.nextInt(10 * k + c, 50 * k + c)
        val numFish = kotlin.random.Random.nextInt(10 * k + c)
        val numCoconuts = kotlin.random.Random.nextInt(5 * k + c)
        
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
        
        val actualItems = checkItems(items as List<String>)
        
        val expectedItems = checkItems$rand(items)
        val raftsExp = expectedItems.count { it == "плот" }
        val raftsAct = actualItems.count { it == "плот" }
        val lunchExp = expectedItems.count { it == "обед" }
        val lunchAct = actualItems.count { it == "обед" }
        
        if (raftsExp != raftsAct || lunchExp != lunchAct) {
            throw Exception(""${'"'}
Имелось:
бревна: ${'$'}numLogs
лозы: ${'$'}numVines
рыба: ${'$'}numFish
кокосы: ${'$'}numCoconuts

Ваш код посчитал:
плоты: ${'$'}raftsAct
обеды: ${'$'}lunchAct

Но ожидалось:
плоты: ${'$'}raftsExp
обеды: ${'$'}lunchExp
""${'"'})
        }
    }
}

test$rand(1, 0)
test$rand(3, 10)
""")
}

// Ready
fun exe5(code: String): EvalResult {
    val rand = rand()

    return Main.eval(code + """
fun checkItems$rand(items: List<String>): Map<String, Int> {
    val newItems = HashMap<String, Int>()

    for (i in items.indices) {
        val item = items[i]
        newItems[item] = (newItems[item] ?: 0) + 1
    }

    return newItems
}

fun test$rand(k: Int, c: Int) {
    for (j in 0 until 10) {
        val numLogs = kotlin.random.Random.nextInt(5 * k + c, 20 * k + c)
        val numVines = kotlin.random.Random.nextInt(10 * k + c, 50 * k + c)
        val numFish = kotlin.random.Random.nextInt(10 * k + c)
        val numCoconuts = kotlin.random.Random.nextInt(5 * k + c)

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

        val actualItems = itemsCounting(items)

        val expectedItems = checkItems$rand(items)

        if (actualItems.size != expectedItems.size) {
            throw Exception("В вашем массиве не хватает предметов, либо есть лишние")
        }

        expectedItems.entries.forEach {
            if (actualItems[it.key] != it.value) {
                throw Exception(""${'"'}
Ваш код посчитал:
${'$'}{it.key} - ${'$'}{actualItems[it.key]}

Но ожидалось:
${'$'}{it.key} - ${'$'}{it.value}
""${'"'})
            }
        }
    }
}

test$rand(1, 0)
test$rand(3, 10)
""")
}
