package net.furizon.backend.infrastructure.templating.service;

import gg.jte.TemplateEngine;
import gg.jte.ContentType;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.admin.AdminConfig;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Service
public class CustomTemplateService {
    private final AdminConfig adminConfig;
    private final TemplateEngine templateEngine;

    public CustomTemplateService(AdminConfig adminConfig) {
        final Path templatesPath = Paths.get(adminConfig.getCustomTemplatesLocation());
        this.adminConfig = adminConfig;
        this.templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(templatesPath),
                templatesPath,
                ContentType.Html);
    }

    public String renderTemplate(String templateName, Map<String, Object> params) {
        return renderTemplate(templateName, params, new StringOutput());
    }

    public String renderTemplate(String templateName, Map<String, Object> params, TemplateOutput output) {
        templateEngine.render(templateName, params, output);
        return output.toString();
    }

}
