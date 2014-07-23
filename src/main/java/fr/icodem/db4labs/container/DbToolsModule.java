package fr.icodem.db4labs.container;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import fr.icodem.db4labs.app.Config;
import fr.icodem.db4labs.controller.TaskProcessingController;
import fr.icodem.db4labs.dbtools.navigation.NavigationHandler;
import fr.icodem.db4labs.dbtools.navigation.StageHandler;
import fr.icodem.db4labs.dbtools.service.LoadDatabaseService;
import fr.icodem.db4labs.dbtools.service.SaveDatabaseService;
import fr.icodem.db4labs.dbtools.service.ShutdownService;
import fr.icodem.db4labs.dbtools.transaction.TransactionInterceptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import fr.icodem.db4labs.dbtools.validation.ValidatorResultsHandler;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

public class DbToolsModule extends AbstractModule implements TypeListener {

    private AppContainer container;
    private EventBus eventBus;

    public DbToolsModule(AppContainer container) {
        this.container = container;
        this.eventBus = new EventBus("Application container");
    }

    @Override
    protected void configure() {
        // transationnal interceptor
        TransactionInterceptor txInterceptor = new TransactionInterceptor();
        requestInjection(txInterceptor);
        bindInterceptor(Matchers.annotatedWith(Transactionnal.class),
                        Matchers.any(),
                        txInterceptor);

        bind(AppContainer.class).toInstance(container);
        bind(Config.class);
        bind(EventBus.class).toInstance(eventBus);

        bind(TaskProcessingController.class).asEagerSingleton();

        bind(NavigationHandler.class).asEagerSingleton();
        bind(StageHandler.class).asEagerSingleton();
        bind(ValidatorResultsHandler.class).asEagerSingleton();

        bind(LoadDatabaseService.class).asEagerSingleton();
        bind(SaveDatabaseService.class).asEagerSingleton();
        bind(ShutdownService.class).asEagerSingleton();

        // post construct
        bindListener(Matchers.any(), this);
    }

    @Override
    public <I> void hear(TypeLiteral<I> iTypeLiteral, TypeEncounter<I> encounter) {
        encounter.register(new InjectionListener<I>() {
            @Override
            public void afterInjection(final I injectee) {
                // post construct
                for (Method m : injectee.getClass().getDeclaredMethods()) {
                    if (m.isAnnotationPresent(PostConstruct.class) &&
                        m.getReturnType().equals(Void.TYPE) && m.getParameterTypes().length == 0) {
                        try {
                            m.invoke(injectee);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // event bus registration
                for (Method m : injectee.getClass().getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Subscribe.class)) {
                        eventBus.register(injectee);
                        break;
                    }
                }

            }
        });
    }
}
