package com.example.sample

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

class OpenTelemetryReporter(
    private val tracer: Tracer
) {
    private val parentSpan = tracer.spanBuilder("NewSampleSession").startSpan().also {
        it.makeCurrent()
        it.end()
    }

    private var param = false
    private var eventNum = 0

    fun report(): String {
        tracer.spanBuilder("sample${++eventNum}")
            .setParent(Context.current())
            .startSpan()
            .also {
                it.setAllAttributes(Attributes.of(AttributeKey.booleanArrayKey("sample"), listOf(!param, false)))
                it.makeCurrent()
                it.end()
            }
            .end()
        return Context.current().toString()
    }
}