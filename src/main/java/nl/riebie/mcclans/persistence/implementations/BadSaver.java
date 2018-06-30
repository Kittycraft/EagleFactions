package nl.riebie.mcclans.persistence.implementations;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
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

public class BadSaver extends DataSaver
{
    private File saveDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "recent");
    private File tempDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "temp");

    private JsonWriter writer;

    public BadSaver() {
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
    }

    @Override
    protected void savePlayer(FactionPlayer player) throws Exception {
    }

    @Override
    protected void saveGroup(String factionName, Group group) throws Exception {
    }

    @Override
    protected void saveFactionRelation(FactionRelation relation) throws Exception {
    }

    @Override
    protected void saveFactionClaim(FactionClaim claim) throws Exception {
    }

    @Override
    protected void saveStarted() throws Exception {
        File factionsFile = new File(saveDataFolder, "quickSave.json");

        File tempFactionsFile = new File(tempDataFolder, "quickSave.json");

        FileUtils.copyFile(factionsFile, tempFactionsFile);

        writer = new JsonWriter(new FileWriter(factionsFile));

        writer.beginObject();
        writer.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        writer.name("cache").beginObject();
        Gson gson = new Gson();
        gson.toJson(FactionsCache.getInstance(), FactionsCache.class, writer);
        writer.endObject();
    }

    @Override
    protected void saveFinished() throws Exception {
        writer.close();
        File tempFile = new File(tempDataFolder, "quickSave.json");
        tempFile.delete();
    }

    @Override
    protected void saveCancelled() {
        File factionsFile = new File(saveDataFolder, "quickSave.json");

        File tempFactionsFile = new File(tempDataFolder, "quickSave.json");

        try {
            FileUtils.copyFile(tempFactionsFile, factionsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
