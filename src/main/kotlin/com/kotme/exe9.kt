package com.kotme

// Ready
fun exe9(code: String): EvalResult {
    val rand = rand()
    return Main.eval(code + """
class USBToken
class USBTypeC
class WiFiAdapter
class BluetoothAdapter
class USBHardDrive
class USBFlashcard

val token$rand = USBToken()
arrayOf(USBTypeC(), WiFiAdapter(), BluetoothAdapter(), USBHardDrive(), USBFlashcard()).forEach {
    if (checkUSBToken<Any>(it) != null) {
        throw Exception("Возвращаемое значение не подходит: ${'$'}{it.javaClass.simpleName}")
    }
}

if (checkUSBToken<USBToken>(token$rand) == null) {
    throw Exception("Возвращаемое значение не подходит: null")
}
""")
}
