package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ContextoLogEnvelope(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("process_instance_id") UUID processInstanceId,
        String topic,
        @JsonProperty("schema_name") String schemaName,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("step_atual") int stepAtual
) {
}
