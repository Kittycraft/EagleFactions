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
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;
import nl.riebie.mcclans.persistence.pojo.AllyPojo;
import nl.riebie.mcclans.persistence.pojo.ClanPlayerPojo;
import nl.riebie.mcclans.persistence.pojo.ClanPojo;
import nl.riebie.mcclans.persistence.pojo.RankPojo;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import nl.riebie.mcclans.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

public class JsonSaver extends DataSaver {

    private File saveDataFolder = new File(MCClans.getPlugin().getDataFolder(), "recent");
    private File tempDataFolder = new File(MCClans.getPlugin().getDataFolder(), "temp");

    private JsonWriter clansWriter;
    private JsonWriter clanPlayersWriter;
    private JsonWriter ranksWriter;
    private JsonWriter alliesWriter;

    public JsonSaver() {
        saveDataFolder.mkdirs();
        tempDataFolder.mkdirs();
    }

    public void useBackupLocation() {
        String backupName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(System.currentTimeMillis());

        File backupFolder = new File(MCClans.getPlugin().getDataFolder(), "backup");
        backupFolder.mkdirs();
        saveDataFolder = getBackupLocation(backupFolder, backupName, 0);
        saveDataFolder.mkdirs();

        int amountOfBackups = backupFolder.listFiles().length;
        int maxAmountOfBackups = Config.getInteger(Config.MAXIMUM_AMOUNT_OF_BACKUPS_BEFORE_REMOVING_OLDEST);
        if (amountOfBackups > maxAmountOfBackups) {
            removeOldestBackups(backupFolder, maxAmountOfBackups);
            MCClans.getPlugin().getLogger().info("Removed " + (amountOfBackups - maxAmountOfBackups) + " old backup(s)", true);
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
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return new Long(o2.lastModified()).compareTo(o1.lastModified());
                }
            });
            for (int index = maxBackups; index < amountOfBackups; index++) {
                FileUtils.removeFolder(files[index]);
            }
        }
    }

    @Override
    protected void saveClan(ClanImpl clan) throws Exception {
        ClanPojo clanPojo = ClanPojo.from(clan);
        Gson gson = new Gson();
        gson.toJson(clanPojo, ClanPojo.class, clansWriter);
    }

    @Override
    protected void saveClanPlayer(ClanPlayerImpl cp) throws Exception {
        ClanPlayerPojo clanPlayerPojo = ClanPlayerPojo.from(cp);
        Gson gson = new Gson();
        gson.toJson(clanPlayerPojo, ClanPlayerPojo.class, clanPlayersWriter);
    }

    @Override
    protected void saveRank(int clanID, RankImpl rank) throws Exception {
        RankPojo rankPojo = RankPojo.from(clanID, rank);
        Gson gson = new Gson();
        gson.toJson(rankPojo, RankPojo.class, ranksWriter);
    }

    @Override
    protected void saveClanAlly(int clanID, int clanIDAlly) throws Exception {
        AllyPojo allyPojo = AllyPojo.from(clanID, clanIDAlly);
        Gson gson = new Gson();
        gson.toJson(allyPojo, AllyPojo.class, alliesWriter);
    }

    @Override
    protected void saveStarted() throws Exception {
        File clansFile = new File(saveDataFolder, "clans.json");
        File clanPlayersFile = new File(saveDataFolder, "clanPlayers.json");
        File ranksFile = new File(saveDataFolder, "ranks.json");
        File alliesFile = new File(saveDataFolder, "allies.json");

        File tempClansFile = new File(tempDataFolder, "clans.json");
        File tempClanPlayersFile = new File(tempDataFolder, "clanPlayers.json");
        File tempRanksFile = new File(tempDataFolder, "ranks.json");
        File tempAlliesFile = new File(tempDataFolder, "allies.json");

        FileUtils.copyFile(clansFile, tempClansFile);
        FileUtils.copyFile(clanPlayersFile, tempClanPlayersFile);
        FileUtils.copyFile(ranksFile, tempRanksFile);
        FileUtils.copyFile(alliesFile, tempAlliesFile);

        clansWriter = new JsonWriter(new FileWriter(clansFile));
        clanPlayersWriter = new JsonWriter(new FileWriter(clanPlayersFile));
        ranksWriter = new JsonWriter(new FileWriter(ranksFile));
        alliesWriter = new JsonWriter(new FileWriter(alliesFile));

        clansWriter.beginObject();
        clansWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        clansWriter.name("list").beginArray();

        clanPlayersWriter.beginObject();
        clanPlayersWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        clanPlayersWriter.name("list").beginArray();

        ranksWriter.beginObject();
        ranksWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        ranksWriter.name("list").beginArray();

        alliesWriter.beginObject();
        alliesWriter.name("dataVersion").value(DatabaseHandler.CURRENT_DATA_VERSION);
        alliesWriter.name("list").beginArray();
    }

    @Override
    protected void saveFinished() throws Exception {
        clansWriter.endArray();
        clansWriter.endObject();
        clansWriter.close();

        clanPlayersWriter.endArray();
        clanPlayersWriter.endObject();
        clanPlayersWriter.close();

        ranksWriter.endArray();
        ranksWriter.endObject();
        ranksWriter.close();

        alliesWriter.endArray();
        alliesWriter.endObject();
        alliesWriter.close();

        File tempClansFile = new File(tempDataFolder, "clans.json");
        File tempClanPlayersFile = new File(tempDataFolder, "clanPlayers.json");
        File tempRanksFile = new File(tempDataFolder, "ranks.json");
        File tempAlliesFile = new File(tempDataFolder, "allies.json");

        tempClansFile.delete();
        tempClanPlayersFile.delete();
        tempRanksFile.delete();
        tempAlliesFile.delete();
    }

    @Override
    protected void saveCancelled() {
        File clansFile = new File(saveDataFolder, "clans.json");
        File clanPlayersFile = new File(saveDataFolder, "clanPlayers.json");
        File ranksFile = new File(saveDataFolder, "ranks.json");
        File alliesFile = new File(saveDataFolder, "allies.json");

        File tempClansFile = new File(tempDataFolder, "clans.json");
        File tempClanPlayersFile = new File(tempDataFolder, "clanPlayers.json");
        File tempRanksFile = new File(tempDataFolder, "ranks.json");
        File tempAlliesFile = new File(tempDataFolder, "allies.json");

        try {
            FileUtils.copyFile(tempClansFile, clansFile);
            FileUtils.copyFile(tempClanPlayersFile, clanPlayersFile);
            FileUtils.copyFile(tempRanksFile, ranksFile);
            FileUtils.copyFile(tempAlliesFile, alliesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}