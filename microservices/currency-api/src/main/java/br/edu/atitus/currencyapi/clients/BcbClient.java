package br.edu.atitus.currencyapi.clients;

import br.edu.atitus.currencyapi.dtos.BcbCotacaoResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.net.URI;

@HttpExchange
public interface BcbClient {

    @GetExchange
    BcbCotacaoResponse getCotacao(URI uri);
}
