package pl.masslany.podkop.test

object IntegrationTestAuth {
    @Volatile
    var loggedIn: Boolean = false

    fun reset() {
        loggedIn = false
    }
}
