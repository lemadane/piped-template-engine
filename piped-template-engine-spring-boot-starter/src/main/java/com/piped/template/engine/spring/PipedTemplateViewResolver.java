package com.piped.template.engine.spring;

import com.piped.template.engine.TemplateEngine;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class PipedTemplateViewResolver extends AbstractTemplateViewResolver {

    public PipedTemplateViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    protected Class<?> requiredViewClass() {
        return PipedTemplateView.class;
    }

    @Override
    protected AbstractUrlBasedView instantiateView() {
        return new PipedTemplateView();
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        PipedTemplateView view = (PipedTemplateView) super.buildView(viewName);
        view.setTemplateEngine(getApplicationContext().getBean(TemplateEngine.class));
        return view;
    }
}
