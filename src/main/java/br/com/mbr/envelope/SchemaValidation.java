package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record SchemaValidation(
        boolean validated,
        @JsonProperty("schema_name") String schemaName,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("validated_at") Instant validatedAt,
        @JsonProperty("validator_service_name") String validatorServiceName,
        @JsonProperty("validator_process_id") String validatorProcessId
) {
}
