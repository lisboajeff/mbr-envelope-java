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

    public static void validarParaCertificacao(Envelope<?> envelope) {
        if (envelope == null) {
            throw new EnvelopeException("Envelope obrigatorio");
        }
        validarTraceParaCertificacao(envelope.trace());
    }

    public static void validarTraceParaCertificacao(List<TraceStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new EnvelopeException("trace deve conter pelo menos 1 step");
        }
        for (int i = 0; i < steps.size() - 1; i++) {
            TraceStep s = steps.get(i);
            if (s.schemaValidation() == null || !s.schemaValidation().validated()) {
                throw new EnvelopeException("Step anterior " + s.step() + " sem certificacao");
            }
        }
        TraceStep ultimo = steps.getLast();
        if (ultimo.schemaValidation() != null) {
            throw new EnvelopeException("Ultimo step " + ultimo.step() + " nao pode estar pre-certificado");
        }
    }

    public static ContextoLogEnvelope contextoLog(Envelope<?> envelope) {
        if (envelope == null) {
            throw new EnvelopeException("Envelope obrigatorio");
        }
        return new ContextoLogEnvelope(
                envelope.eventId(),
                envelope.processInstanceId(),
                envelope.metadata().topic(),
                envelope.metadata().schemaName(),
                envelope.metadata().schemaVersion(),
                envelope.trace().getLast().step()
        );
    }

    public static void validarCertificado(Envelope<?> envelope) {
        if (envelope == null) {
            throw new EnvelopeException("Envelope obrigatorio");
        }
        for (TraceStep s : envelope.trace()) {
            if (s.schemaValidation() == null || !s.schemaValidation().validated()) {
                throw new EnvelopeException("Step " + s.step() + " nao esta certificado");
            }
        }
    }

    public static <T> T extrairData(Envelope<T> envelope) {
        validarCertificado(envelope);
        return envelope.data();
    }

    public static <T> Envelope<T> certificarUltimoStep(
            Envelope<T> envelope,
            String validatorServiceName,
            String validatorProcessId
    ) {
        validarParaCertificacao(envelope);
        String serviceName = validarTexto(validatorServiceName, "schema_validation.validator_service_name");
        String processId = validarTexto(validatorProcessId, "schema_validation.validator_process_id");

        List<TraceStep> novoTrace = new ArrayList<>(envelope.trace());
        TraceStep ultimo = novoTrace.getLast();
        SchemaValidation validacao = new SchemaValidation(
                true,
                envelope.metadata().schemaName(),
                envelope.metadata().schemaVersion(),
                Instant.now(),
                serviceName,
                processId
        );
        novoTrace.set(novoTrace.size() - 1, new TraceStep(
                ultimo.step(),
                ultimo.spanId(),
                ultimo.serviceName(),
                ultimo.processId(),
                ultimo.executedAt(),
                ultimo.responsibility(),
                validacao
        ));
        return new Envelope<>(
                envelope.eventId(),
                envelope.processInstanceId(),
                envelope.timestamp(),
                envelope.metadata(),
                novoTrace,
                envelope.data()
        );
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new EnvelopeException("Campo obrigatorio ausente ou vazio: " + campo);
        }
        return valor.trim();
    }
}
