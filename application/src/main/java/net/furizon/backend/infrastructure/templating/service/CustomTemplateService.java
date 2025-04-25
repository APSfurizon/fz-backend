package net.furizon.backend.infrastructure.templating.service;

import gg.jte.TemplateEngine;
import gg.jte.ContentType;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.admin.AdminConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomTemplateService {
    @NotNull private final AdminConfig adminConfig;
    @NotNull private final TemplateEngine templateEngine;

    public CustomTemplateService(@NotNull AdminConfig adminConfig) throws MalformedURLException {
        final Path templatesPath = Paths.get(adminConfig.getCustomJteTemplatesLocation());
        final URL templatesUrl = templatesPath.toFile().toURI().toURL();
        log.info("Loading custom templates from {}", templatesUrl);
        this.adminConfig = adminConfig;
        this.templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(templatesPath),
                templatesPath,
                ContentType.Html,
                this.getClass().getClassLoader()
                //new URLClassLoader(new URL[] {
                //    templatesUrl,
                //    URI.create("file:/app/templates/jte/jte-runtime-3.1.15.jar").toURL()
                //})
        );
        log.error("DIOCANEEEE " + System.getProperty("java.class.path"));
        //this.templateEngine.setClassPath(List.of(templatesPath.toString(),
        //    "jar:nested:C:/Users/Stran/Desktop/shit/programming/Furizon/fz-backend/
        //    application/target/application-0.0.1-SNAPSHOT.jar/!BOOT-INF/lib/jte-runtime-3.1.15.jar!"));
        //    "C:/Users/Stran/.m2/repository/gg/jte/jte-runtime/3.1.15/jte-runtime-3.1.15.jar"));
        this.templateEngine.setClassPath(List.of(templatesPath.toString(),
                "/app/templates/jte/jte-runtime-3.1.15.jar"));
    }

    @PostConstruct
    public void init() {
        try {
            log.error("AAAAAAAAAAAAAAAAAAAA " + this.getClass().getClassLoader().getClass().getName());
            log.error("AAAAAAAAAAAAAAAA "
                    + Arrays.toString(((URLClassLoader) this.getClass().getClassLoader()).getURLs()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String renderTemplate(String templateName, Map<String, Object> params) {
        return renderTemplate(templateName, params, new StringOutput());
    }

    public String renderTemplate(String templateName, Map<String, Object> params, TemplateOutput output) {
        templateEngine.render(templateName, params, output);
        return output.toString();
    }

}
