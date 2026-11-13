package br.edu.ufvjm.locadora.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

// Fase 7 (Docker): o build do React é copiado para classpath:/static/ e servido pelo próprio
// Spring, na mesma origem da API (cookie de sessão e XSRF-TOKEN sem CORS).
// Em dev (Vite em :5173) a pasta static/ não existe e este handler não responde nada útil.
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResolver());
    }

    // Arquivo existente => devolve o arquivo. Caminho de API ou com extensão (.js, .css,
    // .png...) que não existe => 404 de verdade. Qualquer outro caminho é rota do React
    // Router (ex.: /clientes/3) => index.html, e o JavaScript decide o que mostrar.
    static class SpaFallbackResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource pedido = super.getResource(resourcePath, location);
            if (pedido != null) {
                return pedido;
            }
            if (ehApi(resourcePath) || temExtensao(resourcePath)) {
                return null;
            }
            Resource index = location.createRelative("index.html");
            return index.exists() && index.isReadable() ? index : null;
        }

        private static boolean ehApi(String caminho) {
            return caminho.equals("api") || caminho.startsWith("api/")
                    || caminho.equals("actuator") || caminho.startsWith("actuator/");
        }

        private static boolean temExtensao(String caminho) {
            return caminho.substring(caminho.lastIndexOf('/') + 1).contains(".");
        }
    }
}
