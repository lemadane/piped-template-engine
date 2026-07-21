package com.piped.template.engine.spring.routing;

import com.piped.template.engine.TemplateEngine;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import java.util.*;

public class PipedFileRouteHandlerMapping extends AbstractUrlHandlerMapping {

    private final TemplateEngine templateEngine;
    private final Map<String, PageDataLoader> dataLoaders = new HashMap<>();

    public PipedFileRouteHandlerMapping(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        setOrder(0);
    }

    public void registerPageDataLoader(String routePath, PageDataLoader loader) {
        dataLoaders.put(routePath, loader);
    }

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        discoverAndRegisterRoutes();
    }

    private void discoverAndRegisterRoutes() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:pte-routes/**/+page.pte");

            for (Resource resource : resources) {
                String uri = resource.getURI().toString();
                int routesIndex = uri.indexOf("pte-routes/");
                if (routesIndex != -1) {
                    String relativePath = uri.substring(routesIndex + "pte-routes/".length());
                    String urlPattern = convertToSpringUrlPattern(relativePath);
                    registerFileRoute(urlPattern, relativePath, resource);
                }
            }
        } catch (Exception e) {
            // Log or ignore if pte-routes directory is not present
        }
    }

    private String convertToSpringUrlPattern(String relativePath) {
        String path = relativePath.replace("+page.pte", "");
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = path.replaceAll("\\[([^\\]]+)\\]", "{$1}");
        return path.isEmpty() ? "/" : path;
    }

    private void registerFileRoute(String urlPattern, String relativePath, Resource resource) {
        HttpRequestHandler handler = (request, response) -> {
            try {
                Map<String, Object> model = new HashMap<>();

                @SuppressWarnings("unchecked")
                Map<String, String> pathVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                if (pathVars != null) {
                    model.putAll(pathVars);
                }

                request.getParameterMap().forEach((k, v) -> {
                    if (v != null && v.length > 0) {
                        model.put(k, v.length == 1 ? v[0] : v);
                    }
                });

                PageDataLoader loader = dataLoaders.get(urlPattern);
                if (loader != null) {
                    Map<String, Object> loaded = loader.load(request);
                    if (loaded != null) {
                        model.putAll(loaded);
                    }
                }

                String templateContent = new String(resource.getInputStream().readAllBytes());
                String html = templateEngine.renderString(templateContent, model);

                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(html);
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            }
        };

        registerHandler(urlPattern, handler);
    }
}
