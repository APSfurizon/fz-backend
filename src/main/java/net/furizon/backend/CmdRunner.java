package net.furizon.backend;

import net.furizon.backend.service.pretix.PretixService;
import net.furizon.backend.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CmdRunner implements CommandLineRunner {

    @Autowired
    private PretixService pretixService;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        pretixService.setupClient();
        pretixService.reloadEverything();
        try {
            userService.register("woffo@woffo.ovh", "pisolino");
        } catch (Throwable e ){
            e.printStackTrace(System.out);
        }
    }
}
