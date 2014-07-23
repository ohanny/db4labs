package fr.icodem.db4labs.app;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Singleton
public class Config {
    private Path configPath;
    private Path dataPath;

    public Path getConfigPath() throws IOException {
        if (configPath == null) {
            String homePath = System.getProperties().getProperty("user.home");
            configPath = Paths.get(homePath).resolve(".db4labs");
            if (!Files.exists(configPath)) {
                Files.createDirectory(configPath);
            }
        }
        return configPath;
    }

    public Path getDataPath() throws IOException {
        if (dataPath == null) {
            Path cfgFilePath = getConfigPath().resolve("config.properties");
            Properties props = new Properties();
            try {
                props.load(Files.newInputStream(cfgFilePath));
                String dataPathStr = props.getProperty("data.path");
                if (dataPathStr != null) {
                    dataPath = Paths.get(dataPathStr);
                    if (!Files.exists(dataPath)) Files.createDirectories(dataPath);
                } else {
                    dataPath = configPath;
                }
            } catch (IOException e) {
                dataPath = configPath;
            }
        }
        return dataPath;
    }
}
