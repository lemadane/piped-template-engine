package com.piped.template.engine.spring;

import com.piped.template.engine.TemplateEngine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractTemplateView;

import java.util.Map;

public class PipedTemplateView extends AbstractTemplateView {

    private TemplateEngine templateEngine;

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    protected void renderMergedTemplateModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String viewName = getUrl();
        String html = templateEngine.render(viewName, model);

        response.setContentType(getContentType());
        response.getWriter().write(html);
    }
}
