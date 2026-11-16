package com.example.taskprioritizer.repository.impl

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException

object RepositoryErrorReporter {

    fun record(operation: String, exception: Exception) {
        if (exception is CancellationException) return

        // Conserva el lugar del error sin incluir mensajes que puedan
        // contener datos del usuario, documentos o credenciales.
        val report = IllegalStateException(
            "$operation failed (${exception.javaClass.simpleName})"
        ).apply {
            stackTrace = exception.stackTrace
        }

        try {
            FirebaseCrashlytics.getInstance().recordException(report)
        } catch (reportingException: Exception) {
            // Un problema del monitoreo no debe interrumpir la app.
            Log.w(
                "RepositoryErrorReporter",
                "Could not record a non-fatal repository error."
            )
        }
    }
}