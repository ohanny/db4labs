package fr.icodem.db4labs.app;

import com.google.inject.Singleton;

@Singleton
public class AppNameProvider {
    private AppName appName;

    public AppName getAppName() {
        return appName;
    }

    public void setAppName(AppName appName) {
        this.appName = appName;
    }
}
