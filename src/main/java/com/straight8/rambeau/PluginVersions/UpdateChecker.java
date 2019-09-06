package com.straight8.rambeau.PluginVersions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;

public class UpdateChecker {
    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=70927";

    private final PluginVersions plugin;

    private String currentVersion;
    
    private final BiConsumer<VersionResponse, String> versionResponse;

    public UpdateChecker(PluginVersions plugin, BiConsumer<VersionResponse, String> consumer) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.versionResponse = consumer;
    }

    public void check() {
        plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                HttpURLConnection httpURLConnection = (HttpsURLConnection) new URL(SPIGOT_URL).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/5.0");

                String fetchedVersion = Resources.toString(httpURLConnection.getURL(), Charset.defaultCharset());

                boolean latestVersion = fetchedVersion.equalsIgnoreCase(this.currentVersion);

                plugin.getServer().getScheduler().runTask(this.plugin, () -> this.versionResponse.accept(latestVersion ? VersionResponse.LATEST : VersionResponse.FOUND_NEW, latestVersion ? this.currentVersion : fetchedVersion));
            } catch (IOException exception) {
                exception.printStackTrace();
                plugin.getServer().getScheduler().runTask(this.plugin, () -> this.versionResponse.accept(VersionResponse.UNAVAILABLE, null));
            }
        });
    }
    
    public static enum VersionResponse {
        LATEST,
        FOUND_NEW,
        UNAVAILABLE
    }

}