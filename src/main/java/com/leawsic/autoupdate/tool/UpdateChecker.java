package com.leawsic.autoupdate.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.ModInfo;
import com.leawsic.autoupdate.data.RemoteModList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpdateChecker {
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/%s/version?loaders=[\"fabric\"]&game_versions=[\"%s\"]";
    private static final String CURSEFORGE_API = "https://api.curseforge.com/v1/mods/%s/files";

    // URL to fetch the remote mod list JSON
    // TODO: change to real url
    private static final String REMOTE_MOD_LIST_URL = "https://example.com/modlist.json";

    public UpdateChecker() {}

    /**
     * Fetch remote mod list and compare with local mods to find updates
     */
    public CompletableFuture<Map<String, ModInfo>> checkForUpdates(List<ModInfo> localMods) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch the remote mod list
                RemoteModList remoteModList = fetchRemoteModList();

                // Create a map of local mods for easier lookup
                Map<String, ModInfo> localModMap = localMods.stream()
                        .collect(Collectors.toMap(ModInfo::getId, ModInfo -> ModInfo));

                // Map to store mods that need updates
                Map<String, ModInfo> modsToUpdate = new HashMap<>();

                // Compare remote mods with local mods
                for (ModInfo Mod : remoteModList.getModInfos()) {
                    ModInfo localMod = localModMap.get(Mod.getId());

                    // If mod exists locally and versions don't match, mark for update
                    if (localMod != null && !localMod.getVersion().equals(Mod.getVersion())) {
                        modsToUpdate.put(Mod.getId(), Mod);
                    }
                    // If mod doesn't exist locally, mark for download
                    else if (localMod == null) {
                        modsToUpdate.put(Mod.getId(), Mod);
                    }
                }

                return modsToUpdate;
            } catch (Exception e){
                AutoUpdate.LOGGER.error("{}", e.getMessage());
                return new HashMap<>();
            }
        });
    }

    /**
     * Fetch the remote mod list from the server
     */
    private RemoteModList fetchRemoteModList() throws IOException{
        URL url = new URL(REMOTE_MOD_LIST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            Gson gson = new Gson();
            return gson.fromJson(reader, RemoteModList.class);
        }
    }

    /**
     * Old method - kept for reference but not used anymore
     */
    @Deprecated
    private boolean checkModrinthUpdate(ModInfo mod) throws IOException {
        String minecraftVersion = MinecraftClient.getInstance().getGameVersion();
        String urlString = String.format(MODRINTH_API, mod.getId(), minecraftVersion);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //modrinth要求的
        connection.setRequestProperty("User-Agent",
                AutoUpdate.MOD_ID+'/'+AutoUpdate.getVersion().getFriendlyString());

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
            // TODO: complete compare-mod logic
            return false;
        }
    }

    public void notifyIfUpdatesAvailable(Map<String, ModInfo> updates) {
        int updateCount = updates.size();

        if (updateCount > 0) {
            MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("§a发现 " + updateCount + " 个模组更新！请检查自动更新菜单。"),
                    false
            );
        }
    }
}
