package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceJpa.class);

    private final CurrencyRepository repository;
    private final BcbClientService bcbClientService;

    public CurrencyServiceJpa(CurrencyRepository repository, BcbClientService bcbClientService) {
        this.repository = repository;
        this.bcbClientService = bcbClientService;
    }

    @Value("${server.port:8100}")
    private String serverPort;

    // Data fixa (dia util com cotacao no BCB), formato MM-DD-AAAA
    @Value("${app.bcb.data-cotacao:03-14-2025}")
    private String dataCotacao;

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {
        String source = sourceCurrency == null ? "" : sourceCurrency.trim().toUpperCase();
        String target = targetCurrency == null ? "" : targetCurrency.trim().toUpperCase();
        String environment = "Currency API running in Port: " + serverPort;

        Double rate = getRateFromBcb(source, target);
        if (rate != null) {
            log.info("Cotacao {}->{} obtida do BCB: {}", source, target, rate);
            return new CurrencyResponse(source, target, rate, environment);
        }

        // Fallback: banco de dados local
        log.warn("Cotacao {}->{} indisponivel no BCB. Usando banco local.", source, target);
        var currency = repository.findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() -> new EntityNotFoundException("Cotação não encontrada"));
        return CurrencyResponse.fromEntity(currency, environment);
    }

    private Double getRateFromBcb(String source, String target) {
        try {
            Double brlSource = quoteInBrl(source);
            Double brlTarget = quoteInBrl(target);
            if (brlSource == null || brlTarget == null) return null;
            return brlSource / brlTarget;
        } catch (Exception e) {
            // inclui timeout, erro de rede e CallNotPermittedException (circuito aberto)
            log.warn("Falha ao consultar BCB ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private Double quoteInBrl(String currency) {
        if ("BRL".equals(currency)) return 1.0;
        if (!currency.matches("^[A-Z]{3}$")) return null;
        Double quote = bcbClientService.getQuoteInBrl(currency, dataCotacao);
        return (quote == null || quote <= 0) ? null : quote;
    }
}
