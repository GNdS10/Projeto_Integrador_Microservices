package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbClient;
import br.edu.atitus.currencyapi.dtos.BcbCotacaoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Comparator;
import java.util.Objects;

/**
 * Consulta a cotacao (em BRL) de uma moeda na API PTAX do Banco Central.
 * Ordem dos aspectos: Cache (99) -> Retry (100) -> CircuitBreaker (101).
 * Retorna null quando o BCB nao tem cotacao (null nao e armazenado no cache).
 * Falhas de comunicacao lancam excecao (nao sao armazenadas no cache).
 */
@Service
public class BcbClientService {

    private static final Logger log = LoggerFactory.getLogger(BcbClientService.class);

    private final BcbClient client;

    @Value("${app.bcb.url:https://olinda.bcb.gov.br}")
    private String bcbUrl;

    public BcbClientService(BcbClient client) {
        this.client = client;
    }

    @Cacheable(value = "bcb-api", key = "#currency + '-' + #date", unless = "#result == null")
    @Retry(name = "bcb-api")
    @CircuitBreaker(name = "bcb-api")
    public Double getQuoteInBrl(String currency, String date) {
        log.info("Consultando cotacao no BCB: moeda={} data={}", currency, date);

        URI uri = URI.create(bcbUrl
                + "/olinda/servico/PTAX/versao/v1/odata/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)"
                + "?%40moeda='" + currency + "'&%40dataCotacao='" + date + "'&%24format=json");

        BcbCotacaoResponse response = client.getCotacao(uri);

        if (response == null || response.value() == null || response.value().isEmpty()) {
            log.info("BCB sem cotacao para moeda={} data={}", currency, date);
            return null;
        }
        return response.value().stream()
                .filter(c -> c.cotacaoVenda() != null)
                .max(Comparator.comparing(c -> Objects.toString(c.dataHoraCotacao(), "")))
                .map(BcbCotacaoResponse.Cotacao::cotacaoVenda)
                .orElse(null);
    }
}
