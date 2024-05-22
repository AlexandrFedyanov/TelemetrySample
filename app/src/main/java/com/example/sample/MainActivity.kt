package com.example.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.*
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes

class MainActivity : AppCompatActivity() {

    private val openTelemetry by lazy {
        val telemetryEndpoint = "http://sample/tracing/v1/traces"
        val resource = Resource.getDefault().merge(
            Resource.create(
                Attributes.of(
                    ResourceAttributes.SERVICE_NAME,
                    "sample",
                    ResourceAttributes.HOST_NAME,
                    telemetryEndpoint
                )
            )
        )

        val sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .addSpanProcessor(
                BatchSpanProcessor.builder(
                    OtlpHttpSpanExporter.builder()
                        .setEndpoint(telemetryEndpoint)
                        .build()
                ).build()
            )
            .setResource(resource)
            .build()

        OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(W3CBaggagePropagator.getInstance()))
            .build()
    }

    private val openTelemetryReporter by lazy {
        OpenTelemetryReporter(
            openTelemetry.getTracer("sampleTracer", "1.0")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.test)
        val errorText = findViewById<TextView>(R.id.errorText)

        button.setOnClickListener {
            try {
                repeat(200) { openTelemetryReporter.report() }
                errorText.text = "no errors found"
            } catch (e: Throwable) {
                errorText.text = e.toString()
            }
        }

    }

}