import com.kotme.common.CodeCheckResult
import com.kotme.common.CodeCheckResultStatus
import com.kotme.common.PATH
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
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
}