package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.common.logging.api.AppLogger

class FakeAppLogger : AppLogger {
    val debugMessages = mutableListOf<String>()
    val infoMessages = mutableListOf<String>()
    val warnMessages = mutableListOf<String>()
    val errorMessages = mutableListOf<String>()

    override fun debug(message: String) {
        debugMessages += message
    }

    override fun info(message: String) {
        infoMessages += message
    }

    override fun warn(message: String, throwable: Throwable?) {
        warnMessages += message
    }

    override fun error(message: String, throwable: Throwable?) {
        errorMessages += message
    }
}
