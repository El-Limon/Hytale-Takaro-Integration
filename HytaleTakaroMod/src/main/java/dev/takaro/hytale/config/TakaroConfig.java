package dev.takaro.hytale.config;

import java.io.*;
import java.util.Properties;

public class TakaroConfig {
    private final Properties properties;
    private final File configFile;

    public TakaroConfig(File configFile) {
        this.configFile = configFile;
        this.properties = new Properties();
        load();
    }

    /**
     * Re-read the properties file from disk.
     *
     * <p>Called before every identify attempt so an operator who corrects a stale
     * REGISTRATION_TOKEN does not have to restart the server (F4).
     */
    public synchronized void reload() {
        try {
            if (configFile.isFile()) {
                Properties fresh = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    fresh.load(fis);
                }
                properties.clear();
                properties.putAll(fresh);
            }
        } catch (IOException e) {
            // Keep the values already in memory rather than losing the connection over it.
        }
    }

    private void load() {
        if (!configFile.exists()) {
            createDefault();
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefault() {
        properties.setProperty("IDENTITY_TOKEN", "MyHytaleServer");
        properties.setProperty("REGISTRATION_TOKEN", "");
        properties.setProperty("COMMAND_PREFIX", "!");
        properties.setProperty("COMMAND_RESPONSE", "[cyan]Command[-] [green]{prefix}{command}[-]");
        properties.setProperty("TAKARO_DEBUG", "false");
        properties.setProperty("LOG_FORWARD_LEVEL", "INFO");
        properties.setProperty("LOG_FORWARD_MAX_PER_MIN", "120");
        properties.setProperty("EVENT_QUEUE_SIZE", "1000");
        properties.setProperty("CATALOG_INCLUDE_DEBUG", "false");

        // HytaleCharts integration defaults
        properties.setProperty("HYTALECHARTS_SECRET", "");
        properties.setProperty("HYTALECHARTS_DEBUG", "false");
        properties.setProperty("HYTALECHARTS_PROMO_ON_LOGIN", "false");
        properties.setProperty("HYTALECHARTS_PROMO_ENABLED", "false");
        properties.setProperty("HYTALECHARTS_PROMO_INTERVAL_MINUTES", "15");
        properties.setProperty("HYTALECHARTS_PROMO_PREFIX", "[hytalecharts.com] ");
        properties.setProperty("HYTALECHARTS_PROMO_MESSAGE", "Vote for our server on HytaleCharts!");
        properties.setProperty("HYTALECHARTS_PROMO_URL", "https://hytalecharts.com");

        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("# Takaro Integration Configuration\n");
                writer.write("# \n");
                writer.write("# Production Takaro Configuration:\n");
                writer.write("# IDENTITY_TOKEN: Choose a name for your server (e.g., MyHytaleServer, SurvivalServer, etc.)\n");
                writer.write("# REGISTRATION_TOKEN: Get this from Takaro dashboard (Settings → Game Servers → Add Server → Generic)\n");
                writer.write("# COMMAND_PREFIX: Prefix for Takaro commands (default: !). Change to ., -, or any custom prefix (do not use / - reserved by Hytale).\n");
                writer.write("# COMMAND_RESPONSE: Private message sent when command received. Use {prefix} and {command} placeholders. Supports color codes like [cyan]text[-].\n");
                writer.write("# \n");
                writer.write("IDENTITY_TOKEN=MyHytaleServer\n");
                writer.write("REGISTRATION_TOKEN=\n");
                writer.write("COMMAND_PREFIX=!\n");
                writer.write("COMMAND_RESPONSE=[cyan]Command[-] [green]{prefix}{command}[-]\n");
                writer.write("\n");
                writer.write("# Diagnostics:\n");
                writer.write("# TAKARO_DEBUG: log every WebSocket frame (direction, type, requestId, action) at INFO.\n");
                writer.write("# LOG_FORWARD_LEVEL: minimum server log level forwarded to Takaro (OFF disables forwarding).\n");
                writer.write("# LOG_FORWARD_MAX_PER_MIN: cap on forwarded log records per minute (0 = unlimited).\n");
                writer.write("# EVENT_QUEUE_SIZE: how many game events to hold while Takaro is unreachable.\n");
                writer.write("TAKARO_DEBUG=false\n");
                writer.write("LOG_FORWARD_LEVEL=INFO\n");
                writer.write("LOG_FORWARD_MAX_PER_MIN=120\n");
                writer.write("EVENT_QUEUE_SIZE=1000\n");
                writer.write("# CATALOG_INCLUDE_DEBUG: include Debug_/Test_/Dev_ entries in listItems and listEntities.\n");
                writer.write("CATALOG_INCLUDE_DEBUG=false\n");
                writer.write("\n");
                writer.write("# HytaleCharts Integration (third party, OPT-IN - all of it is off below):\n");
                writer.write("# Setting HYTALECHARTS_SECRET sends this server's player list (usernames and\n");
                writer.write("# UUIDs) to hytalecharts.com every 5 minutes. Leave it blank to send nothing.\n");
                writer.write("# HYTALECHARTS_SECRET: Get this from hytalecharts.com (generate heartbeat secret)\n");
                writer.write("# HYTALECHARTS_DEBUG: Enable debug logging (true/false)\n");
                writer.write("# HYTALECHARTS_PROMO_ON_LOGIN: Send promo link when player joins (true/false)\n");
                writer.write("# HYTALECHARTS_PROMO_ENABLED: Enable periodic promo link broadcasts (true/false)\n");
                writer.write("# HYTALECHARTS_PROMO_INTERVAL_MINUTES: How often to broadcast promo link (in minutes)\n");
                writer.write("# HYTALECHARTS_PROMO_PREFIX: Prefix before promo message (set to empty string to disable)\n");
                writer.write("# HYTALECHARTS_PROMO_MESSAGE: Promo link message text\n");
                writer.write("# HYTALECHARTS_PROMO_URL: URL for the clickable link\n");
                writer.write("HYTALECHARTS_SECRET=\n");
                writer.write("HYTALECHARTS_DEBUG=false\n");
                writer.write("HYTALECHARTS_PROMO_ON_LOGIN=false\n");
                writer.write("HYTALECHARTS_PROMO_ENABLED=false\n");
                writer.write("HYTALECHARTS_PROMO_INTERVAL_MINUTES=15\n");
                writer.write("HYTALECHARTS_PROMO_PREFIX=[hytalecharts.com] \n");
                writer.write("HYTALECHARTS_PROMO_MESSAGE=Vote for our server on HytaleCharts!\n");
                writer.write("HYTALECHARTS_PROMO_URL=https://hytalecharts.com\n");
                writer.write("\n");
                writer.write("# Optional: Dev Takaro Configuration (for developers only):\n");
                writer.write("# DEV_ENABLED=true\n");
                writer.write("# DEV_IDENTITY_TOKEN=MyHytaleServerDev\n");
                writer.write("# DEV_REGISTRATION_TOKEN=your-dev-registration-token\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWsUrl() {
        // Hardcoded - users don't configure this
        return "wss://connect.takaro.io/";
    }

    public String getIdentityToken() {
        return properties.getProperty("IDENTITY_TOKEN", "");
    }

    public String getRegistrationToken() {
        return properties.getProperty("REGISTRATION_TOKEN", "");
    }

    // Hidden from default config - will be used when Hytale API is available
    public String getHytaleApiUrl() {
        return properties.getProperty("HYTALE_API_URL", "https://api.hytale.com");
    }

    public String getHytaleApiToken() {
        return properties.getProperty("HYTALE_API_TOKEN", "");
    }

    // Dev Takaro connection (optional)
    public boolean isDevEnabled() {
        return Boolean.parseBoolean(properties.getProperty("DEV_ENABLED", "false"));
    }

    public String getDevWsUrl() {
        return properties.getProperty("DEV_WS_URL", "wss://connect.next.takaro.dev/");
    }

    public String getDevIdentityToken() {
        return properties.getProperty("DEV_IDENTITY_TOKEN", "");
    }

    public String getDevRegistrationToken() {
        return properties.getProperty("DEV_REGISTRATION_TOKEN", "");
    }

    /** How many game events to hold while the connection is down (F13). */
    public int getEventQueueSize() {
        try {
            return Math.max(1, Integer.parseInt(properties.getProperty("EVENT_QUEUE_SIZE", "1000").trim()));
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    /** Include Debug_/Test_/Dev_ entries in listItems and listEntities (F10). */
    public boolean isCatalogIncludeDebug() {
        return Boolean.parseBoolean(properties.getProperty("CATALOG_INCLUDE_DEBUG", "false").trim());
    }

    /** Trace every WebSocket frame at INFO (F9). */
    public boolean isDebug() {
        return Boolean.parseBoolean(properties.getProperty("TAKARO_DEBUG", "false").trim());
    }

    /** Minimum level of a server log record that is forwarded to Takaro as a log event. */
    public String getLogForwardLevel() {
        return properties.getProperty("LOG_FORWARD_LEVEL", "INFO").trim();
    }

    /** Upper bound on forwarded log records per minute; 0 means unlimited. */
    public int getLogForwardMaxPerMin() {
        try {
            return Integer.parseInt(properties.getProperty("LOG_FORWARD_MAX_PER_MIN", "120").trim());
        } catch (NumberFormatException e) {
            return 120;
        }
    }

    public String getCommandPrefix() {
        return properties.getProperty("COMMAND_PREFIX", "!");
    }

    public String getCommandResponse() {
        return properties.getProperty("COMMAND_RESPONSE", "[cyan]Command[-] [green]{prefix}{command}[-]");
    }

    // HytaleCharts configuration
    public String getHytaleChartsSecret() {
        return properties.getProperty("HYTALECHARTS_SECRET", "");
    }

    public boolean getHytaleChartsDebug() {
        return Boolean.parseBoolean(properties.getProperty("HYTALECHARTS_DEBUG", "false"));
    }

    public boolean getHytaleChartsPromoOnLogin() {
        return Boolean.parseBoolean(properties.getProperty("HYTALECHARTS_PROMO_ON_LOGIN", "false"));
    }

    public boolean getHytaleChartsPromoEnabled() {
        return Boolean.parseBoolean(properties.getProperty("HYTALECHARTS_PROMO_ENABLED", "false"));
    }

    public int getHytaleChartsPromoIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("HYTALECHARTS_PROMO_INTERVAL_MINUTES", "15"));
    }

    public String getHytaleChartsPromoPrefix() {
        return properties.getProperty("HYTALECHARTS_PROMO_PREFIX", "[hytalecharts.com] ");
    }

    public String getHytaleChartsPromoMessage() {
        return properties.getProperty("HYTALECHARTS_PROMO_MESSAGE", "Vote for our server on HytaleCharts!");
    }

    public String getHytaleChartsPromoUrl() {
        return properties.getProperty("HYTALECHARTS_PROMO_URL", "https://hytalecharts.com");
    }
}
