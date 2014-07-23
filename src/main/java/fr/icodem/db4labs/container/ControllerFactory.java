package fr.icodem.db4labs.container;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.util.Callback;

@Singleton
public class ControllerFactory implements Callback<Class<?>, Object> {

    @Inject
    private AppContainer container;

    @Override
    public Object call(Class<?> aClass) {
        return container.getInstance(aClass);
    }
}
