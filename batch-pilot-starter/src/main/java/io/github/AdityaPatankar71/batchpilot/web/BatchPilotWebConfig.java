package io.github.AdityaPatankar71.batchpilot.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the compiled Angular console at {@code /batch-pilot}.
 *
 * <p>Static assets are read from the classpath ({@code static/batch-pilot/},
 * supplied by the {@code batch-pilot-ui} jar). Unknown non-asset paths fall back
 * to {@code index.html} so Angular client-side routing works on deep links and
 * refreshes. The REST API under {@code /batch-pilot/api/**} is mapped by
 * {@link BatchPilotController} and takes precedence over this resource handler.
 */
@Configuration(proxyBeanMethods = false)
public class BatchPilotWebConfig implements WebMvcConfigurer {

    private static final String UI_LOCATION = "classpath:/static/batch-pilot/";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Normalise the bare context path to the trailing-slash root.
        registry.addRedirectViewController("/batch-pilot", "/batch-pilot/");
        // Serve the SPA shell for the root; the resource handler handles everything else.
        registry.addViewController("/batch-pilot/").setViewName("forward:/batch-pilot/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/batch-pilot/**")
                .addResourceLocations(UI_LOCATION)
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    /**
     * Returns the requested asset when it exists; otherwise serves the SPA shell
     * ({@code index.html}) for extension-less routes, and a normal 404 for
     * missing files (e.g. a stale {@code .js} request).
     */
    private static final class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (!resourcePath.isEmpty() && !resourcePath.endsWith("/")) {
                Resource requested = location.createRelative(resourcePath);
                if (requested.exists() && requested.isReadable()) {
                    return requested;
                }
                if (resourcePath.contains(".")) {
                    // Looks like a real file request that is genuinely missing.
                    return null;
                }
            }
            // Root or extension-less route: serve the SPA shell for client-side routing.
            Resource index = location.createRelative("index.html");
            return (index.exists() && index.isReadable()) ? index : null;
        }
    }
}
