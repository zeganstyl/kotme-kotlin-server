import com.kotme.Message
import com.kotme.common.ID
import com.kotme.common.PATH
import com.kotme.model.User
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test

class SignUpTests: TestUtils {
    private fun testSignUp(name: String?, login: String?, password: String?, status: HttpStatusCode, message: String) {
        dropDB()
        module {
            postFormUrlEncoded(
                PATH.api_signup,
                listOf(
                    ID.name to name,
                    ID.login to login,
                    ID.password to password
                ).mapNotNull { if (it.second == null) null else it.first to it.second!! },
                status,
                message
            )
        }
    }

    @Test
    fun `sign up no name`() = testSignUp(null, "root", "root", HttpStatusCode.BadRequest, "Request parameter name is missing")
    @Test
    fun `sign up name fail`() = testSignUp("", "root", "root", HttpStatusCode.BadRequest, Message.nameIsEmpty)
    @Test
    fun `sign up no login`() = testSignUp("root", null, "root", HttpStatusCode.BadRequest, "Request parameter login is missing")
    @Test
    fun `sign up login fail`() {
        dropDB()
        module {
            transaction {
                User.new {
                    name = "root"
                    login = "root"
                    password = "root"
                }
            }
            postFormUrlEncoded(
                PATH.api_signup,
                listOf(
                    ID.name to "root",
                    ID.login to "root",
                    ID.password to "root"
                ),
                HttpStatusCode.BadRequest,
                Message.loginAlreadyExists
            )
        }
    }

    @Test
    fun `sign up no pass`() = testSignUp("root", "root", null, HttpStatusCode.BadRequest, "Request parameter password is missing")
    @Test
    fun `sign up pass fail`() = testSignUp("root", "root", "", HttpStatusCode.BadRequest, Message.incorrectPassword)

    @Test
    fun `sign up ok`() = testSignUp("root", "root", "root", HttpStatusCode.OK, "")
}