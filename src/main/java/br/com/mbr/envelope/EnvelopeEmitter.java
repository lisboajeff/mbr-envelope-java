package br.com.mbr.envelope;

/**
 * Emissor de envelopes configurado uma unica vez com a identidade do servico.
 */
public final class EnvelopeEmitter {

    private final String serviceName;
    private final String processId;

    public EnvelopeEmitter(String serviceName, String processId) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new EnvelopeException("Campo obrigatorio ausente ou vazio: trace.service_name");
        }
        if (processId == null || processId.isBlank()) {
            throw new EnvelopeException("Campo obrigatorio ausente ou vazio: trace.process_id");
        }
        this.serviceName = serviceName.trim();
        this.processId = processId.trim();
    }

    public String serviceName() {
        return serviceName;
    }

    public String processId() {
        return processId;
    }

    public <T> Envelope<T> iniciar(EnvelopeDestino destino, String responsibility, T data) {
        if (destino == null) {
            throw new EnvelopeException("Destino obrigatorio");
        }
        return EnvelopeFactory.criarInicial(
                destino.topic(),
                destino.schemaName(),
                destino.schemaVersion(),
                serviceName,
                processId,
                responsibility,
                data
        );
    }

    public <T, U> Envelope<U> avancar(
            Envelope<T> anterior,
            EnvelopeDestino destino,
            String responsibility,
            U novosDados
    ) {
        if (destino == null) {
            throw new EnvelopeException("Destino obrigatorio");
        }
        return EnvelopeFactory.avancarStep(
                anterior,
                destino.topic(),
                destino.schemaName(),
                destino.schemaVersion(),
                serviceName,
                processId,
                responsibility,
                novosDados
        );
    }
}
