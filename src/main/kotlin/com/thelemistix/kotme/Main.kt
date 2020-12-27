package com.thelemistix.kotme

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Main {
    val code = SourceCodeImp()
    val host = BasicJvmScriptingHost()
    val compilationConfiguration = ScriptCompilationConfiguration()

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)
    val console = System.out

    fun initiate() {
//        os.reset()
//        //System.setOut(ps)
//
//        val result = exe10("""
//open class Tag() {
//    val children = ArrayList<Tag>()
//}
//
//class Table: Tag() {
//    fun tr(block: TR.() -> Unit) {
//        children.add(TR().apply(block))
//    }
//}
//
//class TR: Tag() {
//    fun td(block: TD.() -> Unit) {
//        children.add(TD().apply(block))
//    }
//}
//
//class TD: Tag() {
//    var text: String = ""
//}
//
//class Html: Tag() {
//    fun table(block: Table.() -> Unit) {
//        children.add(Table().apply(block))
//    }
//}
//
//fun html(block: Html.() -> Unit) {
//    Html().apply(block)
//}
//""")
//
//        System.setOut(console)
//
//        when (result) {
//            is ResultWithDiagnostics.Failure -> {
//                println("Ошибки компиляции кода")
//                result.reports.forEach { println(it.render()) }
//            }
//            is ResultWithDiagnostics.Success -> {
//                val returnValue = result.value.returnValue
//                if (returnValue is ResultValue.Error) {
//                    println("Ошибки выполнения кода")
//                    println(returnValue.error.message)
//                } else {
//                    println("Выполнено успешно")
//                }
//            }
//        }
    }

    fun eval(code: String): ResultWithDiagnostics<EvaluationResult> {
        Main.code.text = code
        return host.eval(Main.code, compilationConfiguration, null)
    }
}
