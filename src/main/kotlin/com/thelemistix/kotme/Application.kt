package com.thelemistix.kotme

import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

@SpringBootApplication
open class Application {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Main.initiate()

//            val engine = Main.engine as KotlinJsr223JvmScriptEngineBase
//
//            Main.bindings.clear()
////            Main.os.reset()
////            System.setOut(Main.ps)
//
//            val code = """
//interface ICard {
//    operator fun get(code: Int): String
//
//    operator fun set(code: Int, value: String)
//
//    fun getCodes(): List<Int>
//}
//
//abstract class Card: ICard {
//    val map = HashMap<Int, String>()
//
//    override operator fun get(code: Int): String = map[code] ?: ""
//
//    override operator fun set(code: Int, value: String) {
//        map[code] = value
//    }
//
//    override fun getCodes(): List<Int> = map.keys.toList()
//}
//
//class RedCard: Card()
//class BlueCard: Card()
//
//class Device {
//    var card: ICard = RedCard()
//
//    fun putCode(code: Int, value: String) {
//        card[code] = value
//    }
//
//    fun getInfo(code: Int): String = card[code]
//
//    fun getCodes(): List<Int> = card.getCodes()
//}"""
//
//            try {
//                Main.engine.eval(code, Main.bindings)
//
//                val res = exe8()
//                if (res.isEmpty()) {
//                    println("OK")
//                } else {
//                    println(res)
//                }
//            } catch (ex: Exception) {
//                engine
//                println(ex.stackTrace[0].lineNumber)
//                ex.printStackTrace()
//            }

            //SpringApplication.run(Application::class.java, *args)
        }
    }
}
