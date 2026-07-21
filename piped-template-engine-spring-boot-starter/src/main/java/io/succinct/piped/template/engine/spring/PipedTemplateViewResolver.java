package io.succinct.piped.template.engine.spring;

import io.succinct.piped.template.engine.TemplateEngine;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class PipedTemplateViewResolver
        extends AbstractTemplateViewResolver {

    public PipedTemplateViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    protected @NonNull Class<?> requiredViewClass() {
        return PipedTemplateView.class;
    }

    @Override
    protected @NonNull AbstractUrlBasedView instantiateView() {
        return new PipedTemplateView();
    }

    @Override
    protected @NonNull AbstractUrlBasedView buildView(
            final @NonNull String viewName)
            throws Exception {
        final var view = (PipedTemplateView) super.buildView(
                viewName);
        final var applicationContext = getApplicationContext();
        if (applicationContext != null) {
            view.setTemplateEngine(
                    applicationContext.getBean(
                            TemplateEngine.class));
        }
        return view;
    }

    @Override
    public @NonNull String getSuffix() {
        return super.getSuffix();
    }

    @Override
    public @NonNull String getPrefix() {
        return super.getPrefix();
    }
}
