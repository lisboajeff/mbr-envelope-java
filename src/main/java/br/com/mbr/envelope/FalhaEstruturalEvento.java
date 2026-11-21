package br.com.mbr.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record FalhaEstruturalEvento(
        String codigo,
        String mensagem,
        List<String> violacoes,
        @JsonProperty("exchange_origem") String exchangeOrigem,
        @JsonProperty("routing_key") String routingKey,
        @JsonProperty("detectado_em") Instant detectadoEm,
        @JsonProperty("payload_original") Object payloadOriginal
) {
    public FalhaEstruturalEvento {
        violacoes = violacoes == null ? List.of() : List.copyOf(violacoes);
    }
}
