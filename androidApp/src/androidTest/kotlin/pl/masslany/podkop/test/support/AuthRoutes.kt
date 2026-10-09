package pl.masslany.podkop.test.support

object AuthRoutes {
    const val AUTH_PATH = "/api/v3/auth"
    const val REFRESH_TOKEN_PATH = "/api/v3/refresh-token"
    const val NOTIFICATIONS_STATUS_PATH = "/api/v3/notifications/status"

    // Unsigned JWT with payload {"exp":4102444800}: it only expires in 2100, so the app never refreshes it.
    const val APP_TOKEN = "eyJhbGciOiJub25lIn0.eyJleHAiOjQxMDI0NDQ4MDB9."
    const val REFRESH_TOKEN = "integration-refresh-token"

    fun MockApiServer.appToken() {
        postJson(
            path = AUTH_PATH,
            body = """{"data":{"token":"$APP_TOKEN"}}""",
        )
    }

    /**
     * What a signed-in app requests on its own: the notification status it polls, and a token
     * refresh, which startup runs if it sees the session before it has its app token. Token
     * responses are never captured, as they hold real tokens, so that one follows the app's RefreshDto.
     */
    fun MockApiServer.signedInSession() {
        postJson(
            path = REFRESH_TOKEN_PATH,
            body = """{"data":{"token":"$APP_TOKEN","refresh_token":"$REFRESH_TOKEN"}}""",
        )
        getJson(
            path = NOTIFICATIONS_STATUS_PATH,
            body = sample("notifications-status-user").toString(),
        )
    }
}
