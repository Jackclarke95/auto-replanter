package net.jackclarke95.autoreplanter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading and saving of configuration files for the Auto Replanter mod.
 * <p>
 * This class handles the serialization and deserialization of
 * {@link AutoReplanterConfig}
 * objects to and from JSON format. The configuration file is stored in the
 * Minecraft
 * config directory and is automatically created with default values if it
 * doesn't exist.
 * </p>
 * <p>
 * The configuration file is located at:
 * {@code <minecraft-config-dir>/autoreplanter.json}
 * </p>
 * 
 * @author jackclarke95
 * @since 1.0.0
 * @see AutoReplanterConfig
 */
public class ConfigManager {

    /** GSON instance configured for pretty-printing JSON output. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Path to the configuration file in the Minecraft config directory. */
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autoreplanter.json");

    /**
     * Loads the configuration from the config file.
     * <p>
     * If the config file exists, it will be read and parsed. If the file doesn't
     * exist
     * or cannot be read, a new config file will be created with default values.
     * </p>
     * 
     * @return the loaded configuration, or a new configuration with default values
     *         if loading failed
     * @see #saveConfig(AutoReplanterConfig)
     */
    public static AutoReplanterConfig loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String configData = Files.readString(CONFIG_PATH);
                AutoReplanterConfig loadedConfig = GSON.fromJson(configData, AutoReplanterConfig.class);

                // Validate and fix invalid enum values
                if (loadedConfig.sneakMode == null) {
                    loadedConfig.sneakMode = SneakMode.ALWAYS;
                    // Save the corrected config
                    saveConfig(loadedConfig);
                }

                return loadedConfig;
            }
        } catch (Exception e) {
            System.err.println("Failed to load Auto Replanter config: " + e.getMessage());
            System.err.println("Creating new config file with default values.");
        }

        // Create and save default config
        AutoReplanterConfig defaultConfig = new AutoReplanterConfig();
        saveConfig(defaultConfig);
        return defaultConfig;
    }

    /**
     * Saves the given configuration to the config file.
     * <p>
     * The configuration is serialized to JSON format and written to the config
     * file.
     * If the parent directories don't exist, they will be created automatically.
     * </p>
     * 
     * @param config the configuration object to save
     * @throws RuntimeException if the config file cannot be written (wrapped
     *                          IOException)
     * @see #loadConfig()
     */
    public static void saveConfig(AutoReplanterConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}