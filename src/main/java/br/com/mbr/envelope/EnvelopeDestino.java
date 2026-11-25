package br.com.mbr.envelope;

/**
 * Metadados de destino para emissao ou evolucao de envelopes MBR.
 */
public record EnvelopeDestino(
        String topic,
        String schemaName,
        String schemaVersion
) {
    public EnvelopeDestino {
        if (topic == null || topic.isBlank()
                || schemaName == null || schemaName.isBlank()
                || schemaVersion == null || schemaVersion.isBlank()) {
            throw new EnvelopeException("Destino do envelope exige topic, schemaName e schemaVersion");
        }
        topic = topic.trim();
        schemaName = schemaName.trim();
        schemaVersion = schemaVersion.trim();
    }
}
