package br.com.skillmatch.config;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class CorsConfig {

    @Inject
    Router router;

    public void init(@Observes StartupEvent ev) {
        
        // 1. Define os métodos permitidos
        Set<HttpMethod> allowedMethods = Set.of(
            HttpMethod.GET, 
            HttpMethod.POST, 
            HttpMethod.PUT, 
            HttpMethod.DELETE, 
            HttpMethod.OPTIONS
        );

        // 2. Define os cabeçalhos permitidos
        Set<String> allowedHeaders = Set.of(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Methods", 
            "Access-Control-Allow-Headers", 
            "Content-Type", 
            "Authorization", 
            "Accept"
        );
        
        // 3. Cria o CorsHandler usando o método recomendado
        // CUIDADO: Usar "*" permite QUALQUER origem (bom para DEV)
        CorsHandler corsHandler = CorsHandler.create()
            .addOrigin("*") 
            .allowedMethods(allowedMethods)
            .allowedHeaders(allowedHeaders);
        
        // 4. Aplica o handler em todas as rotas (/*)
        router.route().handler(corsHandler);
    }
}