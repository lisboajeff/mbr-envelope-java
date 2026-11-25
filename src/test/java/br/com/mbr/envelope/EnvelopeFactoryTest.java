package br.com.mbr.envelope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvelopeFactoryTest {

    @Test
    void deveCriarEnvelopeInicialSemSchemaValidation() throws Exception {
        Envelope<Map<String, Object>> env = EnvelopeFactory.criarInicial(
                "topic.extracao.ativos.iniciar",
                "extracao-solicitada",
                "1.0.0",
                "infraestrutura.mbr-dispatcher",
                "pod-1",
                "disparo_agendado",
                Map.of("ticker", "PETR4")
        );

        assertEquals(1, env.trace().size());
        assertEquals(1, env.trace().getFirst().step());
        assertNull(env.trace().getFirst().schemaValidation());

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(env);
        assertFalse(json.contains("schema_validation"));
    }

    @Test
    void deveRejeitarAvancoSemCertificacaoAnteriorEAvancarQuandoCertificado() {
        Envelope<Map<String, Object>> inicial = EnvelopeFactory.criarInicial(
                "topic.a", "s1", "1.0.0",
                "infraestrutura.mbr-dispatcher", "pod-1", "disparo",
                Map.of("k", "v")
        );

        assertThrows(EnvelopeException.class, () -> EnvelopeFactory.avancarStep(
                inicial, "topic.b", "s2", "1.0.0",
                "extracao.extrator", "pod-2", "extrair", Map.of()
        ));

        TraceStep certificado = new TraceStep(
                1,
                inicial.trace().getFirst().spanId(),
                inicial.trace().getFirst().serviceName(),
                inicial.trace().getFirst().processId(),
                inicial.trace().getFirst().executedAt(),
                inicial.trace().getFirst().responsibility(),
                new SchemaValidation(true, "s1", "1.0.0", Instant.now(), "infraestrutura.validador-central", "val-1")
        );
        Envelope<Map<String, Object>> envCertificado = new Envelope<>(
                inicial.eventId(),
                inicial.processInstanceId(),
                inicial.timestamp(),
                inicial.metadata(),
                List.of(certificado),
                inicial.data()
        );

        Envelope<Map<String, Object>> proximo = EnvelopeFactory.avancarStep(
                envCertificado, "topic.b", "s2", "1.0.0",
                "extracao.extrator", "pod-2", "extrair", Map.of("y", 2)
        );
        assertEquals(inicial.processInstanceId(), proximo.processInstanceId());
        assertEquals(2, proximo.trace().size());
        assertEquals(2, proximo.trace().get(1).step());
        assertNull(proximo.trace().get(1).schemaValidation());

        EnvelopeFactory.validarParaCertificacao(proximo);
        EnvelopeFactory.validarTraceParaCertificacao(proximo.trace());
        assertEquals(2, EnvelopeFactory.contextoLog(proximo).stepAtual());
        assertThrows(EnvelopeException.class, () -> EnvelopeFactory.extrairData(proximo));
        assertEquals("v", EnvelopeFactory.extrairData(envCertificado).get("k"));
    }

    @Test
    void emitterDeveIniciarCertificarEAvancarComDestinoEMapData() {
        EnvelopeEmitter emitter = new EnvelopeEmitter("ativos.extrator", "pod-1");
        EnvelopeDestino d1 = new EnvelopeDestino("topic.a", "s1", "1.0.0");
        EnvelopeDestino d2 = new EnvelopeDestino("topic.b", "s2", "1.0.0");

        Envelope<Map<String, Object>> env = emitter.iniciar(d1, "iniciar", Map.of("k", "v"));
        Envelope<Map<String, Object>> cert = EnvelopeFactory.certificarUltimoStep(
                env, "infraestrutura.validador-central", "val-1"
        );
        Envelope<Map<String, Object>> prox = emitter.avancar(cert, d2, "avancar", Map.of("n", 42));
        Envelope<String> mapeado = prox.mapData(m -> String.valueOf(m.get("n")));

        assertEquals(2, prox.trace().size());
        assertEquals("ativos.extrator", prox.trace().get(1).serviceName());
        assertEquals("42", mapeado.data());
    }
}
