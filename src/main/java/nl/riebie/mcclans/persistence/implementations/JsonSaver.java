/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.persistence.implementations;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.Config;
import io.github.aquerr.eaglefactions.entities.*;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.FileUtils;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;
import nl.riebie.mcclans.persistence.pojo.FactionGroupPojo;
import nl.riebie.mcclans.persistence.pojo.FactionPlayerPojo;
import nl.riebie.mcclans.persistence.pojo.FactionPojo;
import nl.riebie.mcclans.persistence.pojo.FactionRelationPojo;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class JsonSaver extends DataSaver {

    private File saveDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "recent");
    private File tempDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "temp");

    private JsonWriter factionsWriter;
    private JsonWriter factionPlayersWriter;
    private JsonWriter factionGroupsWriter;
    private JsonWriter factionRelationsWriter;
    private JsonWriter factionClaimsWriter;

    public JsonSaver() {
        saveDataFolder.mkdirs();
        tempDataFolder.mkdirs();
    }

    public void useBackupLocation() {
        String backupName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(System.currentTimeMillis());

        File backupFolder = new File(EagleFactions.getPlugin().getDataFolder(), "backup");
        backupFolder.mkdirs();
        saveDataFolder = getBackupLocation(backupFolder, backupName, 0);
        saveDataFolder.mkdirs();

        int amountOfBackups = backupFolder.listFiles().length;
        int maxAmountOfBackups = Config.getInteger(Config.MAXIMUM_AMOUNT_OF_BACKUPS_BEFORE_REMOVING_OLDEST);
        if (amountOfBackups > maxAmountOfBackups) {
            removeOldestBackups(backupFolder, maxAmountOfBackups);
            EagleFactions.getPlugin().getLogger().info("Removed " + (amountOfBackups - maxAmountOfBackups) + " old backup(s)", true);
        }
    }

    private File getBackupLocation(File backupFolder, String backupName, int iteration) {
        File backupLocation;
        if (iteration == 0) {
            backupLocation = new File(backupFolder, backupName);
        } else {
            backupLocation = new File(backupFolder, backupName + " [" + iteration + "]");
        }

        if (backupLocation.exists()) {
            return getBackupLocation(backupFolder, backupName, ++iteration);
        } else {
            return backupLocation;
        }
    }

    private void removeOldestBackups(File directory, int maxBackups) {
        File[] files = directory.listFiles();
        int amountOfBackups = files.length;
        if (amountOfBackups == 0) {
            return;
        }
        if (amountOfBackups > maxBackups) {
            Arrays.sort(files, (o1, o2) -> new Long(o2.lastModified()).compareTo(o1.lastModified()));
            for (int index = maxBackups; index < amountOfBackups; index++) {
                FileUtils.removeFolder(files[index]);
            }
        }
    }

    @Override
    protected void saveFaction(Faction faction) throws Exception {
        FactionPojo factionPojo = FactionPojo.from(faction);
        Gson gson = new Gson();
        gson.toJson(factionPojo, FactionPojo.class, factionsWriter);
    }

    @Override
    protected void savePlayer(FactionPlayer player) throws Exception {
        FactionPlayerPojo factionPlayerPojo = FactionPlayerPojo.from(player);
        Gson gson = new Gson();
        gson.toJson(factionPlayerPojo, FactionPlayerPojo.class, factionPlayersWriter);
    }

    @Override
    protected void saveGroup(String factionName, Group group) throws Exception {
        FactionGroupPojo factionGroupPojo = FactionGroupPojo.from(group, factionName);
        Gson gson = new Gson();
        gson.toJson(factionGroupPojo, FactionGroupPojo.class, factionGroupsWriter);
    }

    @Override
    protected void saveFactionRelation(FactionRelation relation) throws Exception {
        FactionRelationPojo factionRelationPojo = FactionRelationPojo.from(relation);
        Gson gson = new Gson();
        gson.toJson(factionRelationPojo, FactionRelationPojo.class, factionRelationsWriter);
    }

    @Override
    protected void saveFactionClaim(FactionClaim claim) throws Exception {
        Gson gson = new Gson();
        gson.toJson(claim, FactionClaim.class, factionClaimsWriter);
    }

    @Override
    protected void saveStarted() throws Exception {
        File factionsFile = new File(saveDataFolder, "factions.json");
        File factionPlayersFile = new File(saveDataFolder, "factionPlayers.json");
        File factionGroupsFile = new File(saveDataFolder, "factionGroups.json");
        File factionRelationsFile = new File(saveDataFolder, "factionRelations.json");
        File factionClaimsFile = new File(saveDataFolder, "factionClaims.json");

        File tempFactionsFile = new File(tempDataFolder, "factions.json");
        File tempFactionPlayersFile = new File(tempDataFolder, "factionPlayers.json");
        File tempFactionGroupsFile = new File(tempDataFolder, "factionGroups.json");
        File tempFactionRelationsFile = new File(tempDataFolder, "factionRelations.json");
        File tempFactionClaimsFile = new File(tempDataFolder, "factionClaims.json");

        FileUtils.copyFile(factionsFile, tempFactionsFile);
        FileUtils.copyFile(factionPlayersFile, tempFactionPlayersFile);
        FileUtils.copyFile(factionGroupsFile, tempFactionGroupsFile);
        FileUtils.copyFile(factionRelationsFile, tempFactionRelationsFile);
        FileUtils.copyFile(factionClaimsFile, tempFactionClaimsFile);

        factionsWriter = new JsonWriter(new FileWriter(factionsFile));
        factionPlayersWriter = new JsonWriter(new FileWriter(factionPlayersFile));
        factionGroupsWriter = new JsonWriter(new FileWriter(factionGroupsFile));
        factionRelationsWriter = new JsonWriter(new FileWriter(factionRelationsFile));
        factionClaimsWriter = new JsonWriter(new FileWriter(factionClaimsFile));

        factionsWriter.beginObject();
        factionsWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        factionsWriter.name("list").beginArray();

        factionPlayersWriter.beginObject();
        factionPlayersWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        factionPlayersWriter.name("list").beginArray();

        factionGroupsWriter.beginObject();
        factionGroupsWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        factionGroupsWriter.name("list").beginArray();

        factionRelationsWriter.beginObject();
        factionRelationsWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        factionRelationsWriter.name("list").beginArray();

        factionClaimsWriter.beginObject();
        factionClaimsWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        factionClaimsWriter.name("list").beginArray();
    }

    @Override
    protected void saveFinished() throws Exception {
        factionsWriter.endArray();
        factionsWriter.endObject();
        factionsWriter.close();

        factionPlayersWriter.endArray();
        factionPlayersWriter.endObject();
        factionPlayersWriter.close();

        factionGroupsWriter.endArray();
        factionGroupsWriter.endObject();
        factionGroupsWriter.close();

        factionRelationsWriter.endArray();
        factionRelationsWriter.endObject();
        factionRelationsWriter.close();

        factionClaimsWriter.endArray();
        factionClaimsWriter.endObject();
        factionClaimsWriter.close();

        File tempFactionsFile = new File(tempDataFolder, "factions.json");
        File tempFactionPlayersFile = new File(tempDataFolder, "factionPlayers.json");
        File tempFactionGroupsFile = new File(tempDataFolder, "factionGroups.json");
        File tempFactionRelationsFile = new File(tempDataFolder, "factionRelations.json");
        File tempFactionClaimsFile = new File(tempDataFolder, "factionClaims.json");

        tempFactionsFile.delete();
        tempFactionPlayersFile.delete();
        tempFactionGroupsFile.delete();
        tempFactionRelationsFile.delete();
        tempFactionClaimsFile.delete();
    }

    @Override
    protected void saveCancelled() {
        File factionsFile = new File(saveDataFolder, "factions.json");
        File factionPlayersFile = new File(saveDataFolder, "factionPlayers.json");
        File factionGroupsFile = new File(saveDataFolder, "factionGroups.json");
        File factionRelationsFile = new File(saveDataFolder, "factionRelations.json");
        File factionClaimsFile = new File(saveDataFolder, "factionClaims.json");

        File tempFactionsFile = new File(tempDataFolder, "factions.json");
        File tempFactionPlayersFile = new File(tempDataFolder, "factionPlayers.json");
        File tempFactionGroupsFile = new File(tempDataFolder, "factionGroups.json");
        File tempFactionRelationsFile = new File(tempDataFolder, "factionRelations.json");
        File tempFactionClaimsFile = new File(tempDataFolder, "factionClaims.json");

        try {
            FileUtils.copyFile(tempFactionClaimsFile, factionClaimsFile);
            FileUtils.copyFile(tempFactionGroupsFile, factionGroupsFile);
            FileUtils.copyFile(tempFactionPlayersFile, factionPlayersFile);
            FileUtils.copyFile(tempFactionRelationsFile, factionRelationsFile);
            FileUtils.copyFile(tempFactionsFile, factionsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}