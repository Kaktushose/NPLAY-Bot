package de.kaktushose.nrtv.discord.core.config;

import de.kaktushose.nrtv.discord.util.Logging;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class ConfigFile {

    private Yaml yaml;
    private File file;
    private DatabaseConfig dataBaseConfig;

    public ConfigFile(String path) {
        yaml = new Yaml();
        file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
           Logging.getLogger().error("An error has occurred while creating the databaseConfig file", new ExceptionInInitializerError(e));
        }
    }

    public DatabaseConfig loadConfig() {
        try (final InputStream inputStream = new FileInputStream(file)) {
            dataBaseConfig = yaml.load(inputStream);
        } catch (IOException | ClassCastException e) {
            Logging.getLogger().error("An error has occurred while loading the databaseConfig file", e);
        }
        dataBaseConfig.setConfigFile(this);
        return dataBaseConfig;
    }

    void saveConfig() {
        try (final FileWriter fileWriter = new FileWriter(file)) {
            yaml.dump(dataBaseConfig, fileWriter);
        } catch (IOException e) {
            Logging.getLogger().error("An error has occurred while saving the databaseConfig file", e);
        }
    }
    

}
