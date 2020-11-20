package org.thelemistix.kotme

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

fun exe1(): String {
    Main.eval("main()")
    System.setOut(Main.console)

    val text = Main.os.toString().trim()
    if (text != "Привет Котлин!") {
        return "Ваш код выдал:\n$text\nНо это не верно."
    }

    return ""
}

fun roundFloat(v: Float): Float = (v * 100f).roundToInt() * 0.01f

fun exe2(): String {
    for (i in 0 until 10) {
        val x1 = roundFloat(Random.nextFloat() * 100f - 50f)
        val y1 = roundFloat(Random.nextFloat() * 100f - 50f)
        val x2 = roundFloat(Random.nextFloat() * 100f - 50f)
        val y2 = roundFloat(Random.nextFloat() * 100f - 50f)

        val dist = roundFloat(sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2)))

        val test = "distance(${x1}f, ${y1}f, ${x2}f, ${y2}f)"

        val testDist = roundFloat(Main.eval(test) as Float)
        System.setOut(Main.console)

        if (dist != testDist) {
            return """
Тест не пройден:
$test
Ваш код выдал:
$testDist
Но это не верно
Ожидалось $dist
"""
        }
    }

    return ""
}

fun exe3(): String {
    val steps2 = Random.nextInt(5, 30)
    val rand = Random.nextInt(5, 30)

    for (i in 0 until 10) {
        Main.os.reset()

        val steps = Random.nextInt(5, 30)

        var stepsStr = ""

        for (j in 0 until steps) {
            stepsStr += if (j == steps - 1) "Шаг ${j+1} последний\n" else "Шаг ${j+1} идем далее\n"
        }

        Main.eval("stepsCounting($steps)")

        val text = Main.os.toString().trim()
        if (text != stepsStr.trim()) {
            return """
Тест не пройден:
stepsCounting($steps)
Ваш код выдал:
$text
Но это не верно
Ожидалось:
$stepsStr
"""
        }
    }

    Main.eval("var result$rand: Int = -1")
    Main.eval("moveToGoal({ step -> result$rand = step; step == $steps2 })")
    val b = Main.eval("result$rand == $steps2") as Boolean

    if (!b) {
        return "Ваш код в moveToGoal не срабатывает на нужном шаге"
    }

    System.setOut(Main.console)

    return ""
}
