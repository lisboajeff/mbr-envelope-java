package br.com.mbr.envelope;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fabrica oficial de criacao e evolucao de envelopes MBR sem pre-certificacao.
 */
public final class EnvelopeFactory {

    private EnvelopeFactory() {
    }

    public static <T> Envelope<T> criarInicial(
            String topic,
            String schemaName,
            String schemaVersion,
            String serviceName,
            String processId,
            String responsibility,
            T data
    ) {
        EnvelopeMetadata metadata = new EnvelopeMetadata(topic, schemaName, schemaVersion);
        Instant agora = Instant.now();
        TraceStep primeiroStep = new TraceStep(
                1,
                UUID.randomUUID().toString(),
                validarTexto(serviceName, "trace.service_name"),
                validarTexto(processId, "trace.process_id"),
                agora,
                validarTexto(responsibility, "trace.responsibility"),
                null
        );
        return new Envelope<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                agora,
                metadata,
                List.of(primeiroStep),
                data
        );
    }

    public static <T, U> Envelope<U> avancarStep(
            Envelope<T> anterior,
            String novoTopic,
            String novoSchemaName,
            String novoSchemaVersion,
            String serviceName,
            String processId,
            String responsibility,
            U novosDados
    ) {
        if (anterior == null) {
            throw new EnvelopeException("Envelope anterior obrigatorio");
        }
        for (TraceStep s : anterior.trace()) {
            if (s.schemaValidation() == null || !s.schemaValidation().validated()) {
                throw new EnvelopeException("Step anterior " + s.step() + " nao esta certificado");
            }
        }
        EnvelopeMetadata metadata = new EnvelopeMetadata(novoTopic, novoSchemaName, novoSchemaVersion);
        Instant agora = Instant.now();
        int proximoStepNum = anterior.trace().getLast().step() + 1;
        List<TraceStep> novoTrace = new ArrayList<>(anterior.trace());
        novoTrace.add(new TraceStep(
                proximoStepNum,
                UUID.randomUUID().toString(),
                validarTexto(serviceName, "trace.service_name"),
                validarTexto(processId, "trace.process_id"),
                agora,
                validarTexto(responsibility, "trace.responsibility"),
                null
        ));
        return new Envelope<>(
                UUID.randomUUID(),
                anterior.processInstanceId(),
                agora,
                metadata,
                novoTrace,
                novosDados
        );
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new EnvelopeException("Campo obrigatorio ausente ou vazio: " + campo);
        }
        return valor.trim();
    }
}
