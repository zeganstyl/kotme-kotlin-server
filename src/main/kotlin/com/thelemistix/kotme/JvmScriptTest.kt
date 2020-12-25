package com.thelemistix.kotme

import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.JvmDependencyFromClassLoader
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
        val obj = ExternalObj()
        obj.qwe = 34534

        code.text = """
obj.asd()
println(obj.qwe)
"""

        host.eval(code, compilationConfiguration, ScriptEvaluationConfiguration {
            providedProperties.put(mapOf("obj" to obj))
        }).onFailure { result ->
            result.reports.forEach {
                println(it.render())
            }
        }
    }

    class ExternalObj {
        var qwe = 112

        fun asd() {
            qwe = 999
        }
    }
}