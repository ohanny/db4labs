package fr.icodem.db4labs.dbtools.transaction;

import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import fr.icodem.db4labs.database.ConnectionProvider;

import java.sql.Connection;

// todo use connection per thread
public class TransactionInterceptor implements MethodInterceptor {

    @Inject
    private ConnectionProvider connectionProvider;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Connection connection = connectionProvider.getConnection();
        Object result = null;
        try {
            result = invocation.proceed();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
        return result;
    }
}
