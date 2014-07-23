package fr.icodem.db4labs.container;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import fr.icodem.db4labs.app.Config;
import fr.icodem.db4labs.database.*;
import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import fr.icodem.db4labs.event.NavigationEvent;
import fr.icodem.db4labs.event.ShutdownEvent;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;
import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResultsHandler;

import java.util.List;

public class AppContainer {

    // singleton
    private static AppContainer instance = new AppContainer();

    public static AppContainer getInstance() {
        return instance;
    }

    private AppContainer() {}

    // container fields
    private Injector injector;

    @Inject
    private EventBus eventBus;

    @Inject
    private ValidatorResultsHandler validatorResultsHandler;

    @Inject private ConnectionProvider connectionProvider;

    @Inject private DbDescriptorProvider dbDescriptorProvider;

    @Inject private Config config;

    private SqlExecutor sqlExecutor;

    // container methods
    public void start() throws Exception {
        injector = Guice.createInjector(new DbToolsModule(this));
        sqlExecutor = new SqlExecutor();

        // check config
        config.getDataPath();
    }

    public void stop() {
        post(new ShutdownEvent());
    }

    public <T>  T getInstance(Class<T> aClass) {
        if (injector == null) throw new IllegalStateException("Application container is not started");

        T obj = injector.getInstance(aClass);
        return obj;
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void navigateTo(String page, String title) {
        post(new NavigationEvent(page, title));
    }

    public void navigateBack() {
        post(new NavigationEvent(true));
    }

    public boolean validate(Validator validator, Node form,
                            MessageBinders messageBinders,
                            PersistentObject... dataList) {
        return validatorResultsHandler.handle(validator, form, messageBinders, dataList);
    }

    public void clearValidationMessages(Node form, MessageBinders messageBinders) {
        validatorResultsHandler.clearValidationMessages(form, messageBinders);
    }

    private void updateSqlExecutor() {
        sqlExecutor.setDb(dbDescriptorProvider.getDescriptor(), connectionProvider.getConnection());
    }

    public void commit() throws Exception {
        updateSqlExecutor();
        sqlExecutor.commit();
    }

    public PersistentObject selectByPK(String tableName, Object... params) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.selectByPK(tableName, params);
    }

    public ObservableList<PersistentObject> select(String tableName) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.select(tableName);
    }

    public ObservableList<PersistentObject> select(String tableName, WhereDescriptor where) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.select(tableName, where);
    }

    public PersistentObject selectUnique(String tableName, WhereDescriptor where) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.selectUnique(tableName, where);
    }

    public void insert(PersistentObject data) throws Exception {
        updateSqlExecutor();
        sqlExecutor.insert(data);
    }

    public void delete(PersistentObject data) throws Exception {
        updateSqlExecutor();
        sqlExecutor.delete(data);
    }

    public void delete(String table, WhereDescriptor where) throws Exception {
        updateSqlExecutor();
        sqlExecutor.delete(table, where);
    }

    public void update(PersistentObject data) throws Exception {
        updateSqlExecutor();
        sqlExecutor.update(data);
    }

    public int count(String tableName) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.count(tableName);
    }

    public int count(String tableName, WhereDescriptor where) throws Exception {
        updateSqlExecutor();
        return sqlExecutor.count(tableName, where);
    }

}
