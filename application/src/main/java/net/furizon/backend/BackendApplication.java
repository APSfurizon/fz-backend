package net.furizon.backend;

import net.furizon.backend.infrastructure.security.session.Session;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {
    public record Asd(Long session){}
    public static void main(String[] args) {
        List<Asd> asd = new ArrayList<>();
        asd.add(new Asd(4L));
        asd.add(new Asd(15L));
        asd.add(new Asd(7L));
        asd.add(new Asd(18L));
        asd.add(new Asd(9L));
        asd.add(new Asd(13L));
        asd.add(new Asd(1L));
        asd.add(new Asd(12L));
        asd.add(new Asd(20L));
        asd.stream().sorted(Comparator.comparing(Asd::session).reversed()).skip(5 - 1).forEach(session -> {System.out.println(session);});

        //SpringApplication.run(BackendApplication.class, args);
    }
}
