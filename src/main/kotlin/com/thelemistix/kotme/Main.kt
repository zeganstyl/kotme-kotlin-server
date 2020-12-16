package com.thelemistix.kotme

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.script.ScriptEngineManager

object Main {
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")!!

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)
    val console = System.out

    val bindings = engine.createBindings()

    fun initiate() {
        engine.eval("fun main(){}")
    }

    fun eval(code: String): Any? = engine.eval(code, bindings)

    fun script() {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")!!

        val os = ByteArrayOutputStream()
        val ps = PrintStream(os)
        val console = System.out

        val bindings = engine.createBindings()
        bindings.clear()

        System.setOut(ps)
        engine.eval("""
fun stepsCounting() {}

fun moveToGoal() {}
        """.trimIndent(), bindings)
        engine.eval("moveToGoal()", bindings)
        System.setOut(console)

        println(os.toString())
    }
}
