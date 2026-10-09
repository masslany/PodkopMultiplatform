package pl.masslany.podkop.test.support

object AuthRoutes {
    const val AUTH_PATH = "/api/v3/auth"

    // Unsigned JWT with payload {"exp":4102444800}: it only expires in 2100, so the app never refreshes it.
    private const val APP_TOKEN = "eyJhbGciOiJub25lIn0.eyJleHAiOjQxMDI0NDQ4MDB9."

    fun MockApiServer.appToken() {
        postJson(
            path = AUTH_PATH,
            body = """{"data":{"token":"$APP_TOKEN"}}""",
        )
    }
}
