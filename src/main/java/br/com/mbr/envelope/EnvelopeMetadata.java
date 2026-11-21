package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EnvelopeMetadata(
        String topic,
        @JsonProperty("schema_name") String schemaName,
        @JsonProperty("schema_version") String schemaVersion
) {
    public EnvelopeMetadata {
        topic = validarNaoVazio(topic, "metadata.topic");
        schemaName = validarNaoVazio(schemaName, "metadata.schema_name");
        schemaVersion = validarNaoVazio(schemaVersion, "metadata.schema_version");
    }

    private static String validarNaoVazio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new EnvelopeException("Campo obrigatorio ausente ou vazio: " + campo);
        }
        return valor.trim();
    }
}
