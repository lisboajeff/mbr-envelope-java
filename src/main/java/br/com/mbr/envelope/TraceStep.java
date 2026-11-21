package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TraceStep(
        int step,
        @JsonProperty("span_id") String spanId,
        @JsonProperty("service_name") String serviceName,
        @JsonProperty("process_id") String processId,
        @JsonProperty("executed_at") Instant executedAt,
        String responsibility,
        @JsonProperty("schema_validation") SchemaValidation schemaValidation
) {
}
