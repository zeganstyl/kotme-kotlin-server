import com.kotme.Main
import com.kotme.model.User
import com.kotme.module
import com.kotme.routes.hashPassword
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.assertEquals

interface TestUtils {
    fun module(block: TestApplicationEngine.() -> Unit) {
        withTestApplication {
            application.module(true)
            block(this)
        }
    }

    fun dropDB() {
        if (TransactionManager.isInitialized()) {
            transaction {
                SchemaUtils.drop(*Main.tables)
            }
        }
    }

    fun createUserRoot(): Int = transaction {
        User.new {
            name = "root"
            login = "root"
            password = hashPassword("root")
        }.id.value
    }

    fun TestApplicationEngine.getHttp(uri: String, status: HttpStatusCode, content: String) {
        handleRequest(HttpMethod.Get, uri).apply {
            assertEquals(status, response.status())
            assertEquals(content, response.content)
        }
    }

    fun TestApplicationEngine.postFormUrlEncoded(
        uri: String,
        body: List<Pair<String, String>>,
        status: HttpStatusCode,
        content: String = ""
    ) {
        handleRequest(HttpMethod.Post, uri) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(body.formUrlEncode())
        }.apply {
            assertEquals(status, response.status())
            assertEquals(content, response.content)
        }
    }
}