package com.kotme

import java.io.File
import java.net.URLClassLoader
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.JvmDependencyFromClassLoader
import kotlin.script.experimental.jvm.JvmScriptCompilationConfigurationBuilder
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

// https://github.com/JetBrains/kotlin/blob/master/libraries/scripting/jvm-host-test/test/kotlin/script/experimental/jvmhost/test/ResolveDependenciesTest.kt
class JvmScriptTest {
    val code = SourceCodeImp()
    val host = BasicJvmScriptingHost()
    val compilationConfiguration = ScriptCompilationConfiguration {
        dependencies(JvmDependencyFromClassLoader { JvmScriptTest::class.java.classLoader })
        providedProperties.put(mapOf("obj" to KotlinType(ExternalObj::class)))
    }

    init {
//        val obj = ExternalObj()
//        obj.qwe = 34534
//
//        code.text = """
//obj.asd()
//println(obj.qwe)
//"""

        JvmScriptCompilationConfigurationBuilder()

        val roots = ClassLoader.getSystemClassLoader().getResources("")

        println(ClassLoader.getSystemClassLoader() as URLClassLoader)

        fun traverse(file: File) {
            println(file.path)
            if (file.isDirectory) {
                file.listFiles()!!.forEach { traverse(it) }
            }
        }

        roots.toList().forEach {
            traverse(File(it.toURI()))
        }

        val jarDir = File(ClassLoader.getSystemClassLoader().getResource(".")!!.path)
        println(jarDir.absolutePath)

//        host.eval(code, compilationConfiguration, ScriptEvaluationConfiguration {
//            providedProperties.put(mapOf("obj" to obj))
//        }).onFailure { result ->
//            result.reports.forEach {
//                println(it.render())
//            }
//        }
    }

    class ExternalObj {
        var qwe = 112

        fun asd() {
            qwe = 999
        }
    }
}