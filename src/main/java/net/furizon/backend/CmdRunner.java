package net.furizon.backend;

import net.furizon.backend.service.pretix.PretixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CmdRunner implements CommandLineRunner {

    @Autowired
    private PretixService pretixService;

    @Override
    public void run(String... args) throws Exception {
        pretixService.updatePretixSettings();
        pretixService.reloadEverything();
    }
}
