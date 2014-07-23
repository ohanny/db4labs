package fr.icodem.db4labs.dbtools.service;

import com.google.common.eventbus.Subscribe;
import fr.icodem.db4labs.event.ShutdownEvent;

public class ShutdownService {

/*
    @Inject
    private H2Database h2Database;
*/

    @Subscribe
    public void shutdown(ShutdownEvent event) {

/*
        try {
            h2Database.saveDatabase(null);

            System.out.println(event);
            h2Database.cleanAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }
}
