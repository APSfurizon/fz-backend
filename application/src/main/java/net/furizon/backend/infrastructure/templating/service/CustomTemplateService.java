package net.furizon.backend.infrastructure.templating.service;

import gg.jte.TemplateEngine;
import gg.jte.ContentType;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.admin.AdminConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomTemplateService {
    @NotNull private final AdminConfig adminConfig;
    @NotNull private final TemplateEngine templateEngine;

    public CustomTemplateService(@NotNull AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
        final Path templatesPath = Paths.get(adminConfig.getCustomJteTemplatesLocation());
        log.info("Loading custom templates from '{}' using jte jar '{}'",
                templatesPath, this.adminConfig.getJteRuntimeJarLocation());
        this.templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(templatesPath),
                templatesPath,
                ContentType.Html,
                this.getClass().getClassLoader()
        );
        this.templateEngine.setClassPath(List.of(
                templatesPath.toString(),
                this.adminConfig.getJteRuntimeJarLocation()
        ));
    }

    public String renderTemplate(String templateName, Map<String, Object> params) {
        return renderTemplate(templateName, params, new StringOutput());
    }

    public String renderTemplate(String templateName, Map<String, Object> params, TemplateOutput output) {
        templateEngine.render(templateName, params, output);
        return output.toString();
    }

}
