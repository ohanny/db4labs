package fr.icodem.db4labs.database;

import com.google.inject.Singleton;

@Singleton
public class DbDescriptorProvider {
    private DbDescriptor descriptor;

    public DbDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(DbDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
