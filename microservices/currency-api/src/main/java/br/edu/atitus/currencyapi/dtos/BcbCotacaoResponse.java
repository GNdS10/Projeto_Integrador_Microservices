package br.edu.atitus.currencyapi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcbCotacaoResponse(List<Cotacao> value) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cotacao(Double cotacaoCompra,
                          Double cotacaoVenda,
                          String dataHoraCotacao,
                          String tipoBoletim) {
    }
}
