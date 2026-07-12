package com.piped.template.engine.spring;

import com.piped.template.engine.TemplateEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
}
