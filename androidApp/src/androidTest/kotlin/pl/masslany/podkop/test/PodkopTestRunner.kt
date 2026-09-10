package pl.masslany.podkop.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class PodkopTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context,
    ): Application =
        super.newApplication(cl, TestMainApplication::class.java.name, context)
}
