package io.succinct.piped.template.engine.spring;

import io.succinct.piped.template.engine.TemplateEngine;
import io.succinct.piped.template.engine.spring.routing.PageDataLoader;
import io.succinct.piped.template.engine.spring.routing.PageLoader;
import io.succinct.piped.template.engine.spring.routing.PipedFileRouteHandlerMapping;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TemplateEngine.class)
@EnableConfigurationProperties(PipedTemplateProperties.class)
public class PipedTemplateAutoConfiguration {

    private final PipedTemplateProperties properties;

    public PipedTemplateAutoConfiguration(PipedTemplateProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateEngine pipedTemplateEngine() {
        return new TemplateEngine(Path.of(properties.getPrefix()));
    }

    @Bean
    @ConditionalOnMissingBean
    public PipedTemplateViewResolver pipedTemplateViewResolver() {
        PipedTemplateViewResolver resolver = new PipedTemplateViewResolver();
        resolver.setPrefix("");
        resolver.setSuffix(properties.getSuffix());
        resolver.setContentType(properties.getContentType());
        resolver.setOrder(properties.getOrder());
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public PipedFileRouteHandlerMapping pipedFileRouteHandlerMapping(
            TemplateEngine pipedTemplateEngine,
            ApplicationContext applicationContext) {
        PipedFileRouteHandlerMapping mapping = new PipedFileRouteHandlerMapping(pipedTemplateEngine);

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(PageLoader.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof PageDataLoader loader) {
                PageLoader annotation = applicationContext.findAnnotationOnBean(beanName, PageLoader.class);
                if (annotation != null) {
                    mapping.registerPageDataLoader(annotation.value(), loader);
                }
            }
        }
        return mapping;
    }
}
