import com.auth0.jwt.JWT
import com.kotme.JwtConfig
import com.kotme.common.CodeCheckResult
import com.kotme.common.CodeCheckResultStatus
import com.kotme.common.PATH
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.experimental.and
import kotlin.test.assertEquals

class ApplicationTest: TestUtils {
    val json = Json {}

    @Test
    fun testRoot() = module {
        getHttp("/", HttpStatusCode.OK, "KOTme is running")
    }

    fun checkCodeAnonym(exercise: Int, body: String, block: CodeCheckResult.() -> Unit) = module {
        handleRequest(HttpMethod.Post, "${PATH.api_code}/$exercise") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(body)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            block(json.decodeFromString(CodeCheckResult.serializer(), response.content ?: ""))
        }
    }

    @Test
    fun `check code exe1 compile error`() = checkCodeAnonym(1, "fun main {}") {
        assertEquals(CodeCheckResultStatus.CompileErrors, status)
        assert(errors.isNotEmpty())
        assertEquals(consoleLog, "")
    }

    @Test
    fun `check code exe1 no console output`() = checkCodeAnonym(1, "fun main() {}") {
        assertEquals(CodeCheckResultStatus.RuntimeErrors, status)
        assert(errors.isNotEmpty())
        assertEquals(consoleLog, "")
    }

    @Test
    fun `check code exe1 success`() = checkCodeAnonym(1, "fun main() { print(\"Hello Kotlin!\") }") {
        assertEquals(CodeCheckResultStatus.Success, status)
        assert(errors.isEmpty())
        assertEquals(consoleLog, "Hello Kotlin!")
    }

    @Test
    fun `token unauthorized`() {
        dropDB()
        module {
            handleRequest(HttpMethod.Get, PATH.api_token) {
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `token success`() {
        dropDB()
        module {
            val id = createUserRoot()
            handleRequest(HttpMethod.Get, PATH.api_token) {
                addHeader(HttpHeaders.Authorization, "Basic ${"root:root".encodeBase64()}")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                JWT.decode(response.content ?: "").also { jwt ->
                    assertEquals(JwtConfig.issuer, jwt.issuer)
                    assertEquals(id.toString(), jwt.claims["id"]?.asString() ?: "")
                }
            }
        }
    }

    companion object {
        private val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private val BASE64_MASK: Byte = 0x3f
        private val BASE64_PAD = '='

        private val BASE64_INVERSE_ALPHABET = IntArray(256) {
            BASE64_ALPHABET.indexOf(it.toChar())
        }

        fun String.encodeBase64(): String = buildPacket {
            writeText(this@encodeBase64)
        }.encodeBase64()

        fun ByteArray.encodeBase64(): String = buildPacket {
            writeFully(this@encodeBase64)
        }.encodeBase64()

        fun ByteReadPacket.encodeBase64(): String = buildString {
            val data = ByteArray(3)
            while (remaining > 0) {
                val read = readAvailable(data)
                data.clearFrom(read)

                val padSize = (data.size - read) * 8 / 6
                val chunk = ((data[0].toInt() and 0xFF) shl 16) or
                        ((data[1].toInt() and 0xFF) shl 8) or
                        (data[2].toInt() and 0xFF)

                for (index in data.size downTo padSize) {
                    val char = (chunk shr (6 * index)) and BASE64_MASK.toInt()
                    append(char.toBase64())
                }

                repeat(padSize) { append(BASE64_PAD) }
            }
        }

        fun String.decodeBase64String(): String =
            io.ktor.utils.io.core.String(decodeBase64Bytes(), charset = Charsets.UTF_8)

        fun String.decodeBase64Bytes(): ByteArray = buildPacket {
            writeText(dropLastWhile { it == BASE64_PAD })
        }.decodeBase64Bytes().readBytes()

        fun ByteReadPacket.decodeBase64Bytes(): Input = buildPacket {
            val data = ByteArray(4)

            while (remaining > 0) {
                val read = readAvailable(data)

                val chunk = data.foldIndexed(0) { index, result, current ->
                    result or (current.fromBase64().toInt() shl ((3 - index) * 6))
                }

                for (index in data.size - 2 downTo (data.size - read)) {
                    val origin = (chunk shr (8 * index)) and 0xff
                    writeByte(origin.toByte())
                }
            }
        }

        internal fun ByteArray.clearFrom(from: Int) {
            (from until size).forEach { this[it] = 0 }
        }

        internal fun Int.toBase64(): Char = BASE64_ALPHABET[this]
        internal fun Byte.fromBase64(): Byte = BASE64_INVERSE_ALPHABET[toInt() and 0xff].toByte() and BASE64_MASK
    }
}