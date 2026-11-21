package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Envelope<T>(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("process_instance_id") UUID processInstanceId,
        Instant timestamp,
        EnvelopeMetadata metadata,
        List<TraceStep> trace,
        T data
) {
    public Envelope {
        if (eventId == null || processInstanceId == null || timestamp == null || metadata == null) {
            throw new EnvelopeException("Campos obrigatorios do cabecalho do envelope nao podem ser nulos");
        }
        if (trace == null || trace.isEmpty()) {
            throw new EnvelopeException("trace deve conter pelo menos 1 step");
        }
        trace = List.copyOf(trace);
    }
}
