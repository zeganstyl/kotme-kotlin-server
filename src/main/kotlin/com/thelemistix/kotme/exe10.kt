package com.thelemistix.kotme

fun exe10(code: String): EvalResult {
    return Main.eval(code + """
html {
    table {
        tr {
            td { text = "999" }
            td { text = "111" }
        }
        tr {
            td { text = "42" }
            td { text = "66" }
        }
    }
}
""")
}
