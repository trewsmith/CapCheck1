package com.trewsmith.capcheck

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ErrorHandler {
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    suspend fun emitError(message: String) {
        _errors.emit(message)
    }
}
