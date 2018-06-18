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

import nl.riebie.mcclans.api.enums.KillDeathFactor;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.QueryGenerator;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.PreparedStatement;
import java.util.UUID;

public class DatabaseSaver extends DataSaver {

    private final static DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();

    protected void saveClan(ClanImpl clan) throws Exception {
        PreparedStatement query = getInsertClanQuery(clan);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    protected void saveClanPlayer(ClanPlayerImpl cp) throws Exception {
        PreparedStatement query = getInsertClanPlayerQuery(cp);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    protected void saveRank(int clanID, RankImpl rank) throws Exception {
        PreparedStatement query = getInsertRankQuery(clanID, rank);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    protected void saveClanAlly(int clanID, int clanIDAlly) throws Exception {
        PreparedStatement query = getInsertClanAllyQuery(clanID, clanIDAlly);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    @Override
    protected void saveStarted() throws Exception {
        databaseConnectionOwner.startTransaction();
        DatabaseHandler.getInstance().truncateDatabase();

        // Store data version
        PreparedStatement query = getInsertDataVersionQuery(DatabaseHandler.CURRENT_DATA_VERSION);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    @Override
    protected void saveFinished() throws Exception {
        databaseConnectionOwner.commitTransaction();
    }

    @Override
    protected void saveCancelled() {
        databaseConnectionOwner.cancelTransaction();
    }

    public static PreparedStatement getInsertDataVersionQuery(int dataVersion) {
        return QueryGenerator.createInsertQuery(
                "mcc_dataversion",
                databaseConnectionOwner.getConnection()
        ).value(
                "dataversion",
                dataVersion
        ).create();
    }

    public static PreparedStatement getInsertClanPlayerQuery(ClanPlayerImpl clanPlayer) {
        int clanPlayerID = clanPlayer.getID();
        UUID uuid = clanPlayer.getUUID();
        String name = clanPlayer.getName();
        int clanID = -1;
        int rankID = -1;
        if (clanPlayer.getClan() != null) {
            clanID = clanPlayer.getClan().getID();
            rankID = clanPlayer.getRank().getID();
        }
        int killsHigh = clanPlayer.getKillDeath().getKills(KillDeathFactor.HIGH);
        int killsMedium = clanPlayer.getKillDeath().getKills(KillDeathFactor.MEDIUM);
        int killsLow = clanPlayer.getKillDeath().getKills(KillDeathFactor.LOW);
        int deathsHigh = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.HIGH);
        int deathsMedium = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.MEDIUM);
        int deathsLow = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.LOW);

        boolean ffProtection = clanPlayer.isFfProtected();
        long lastOnlineTime = clanPlayer.getLastOnline().getTime();

        return QueryGenerator.createInsertQuery("mcc_clanplayers", databaseConnectionOwner.getConnection()).value("clanplayer_id", clanPlayerID)
                .value("uuid_most_sig_bits", uuid.getMostSignificantBits()).value("uuid_least_sig_bits", uuid.getLeastSignificantBits())
                .value("playername", name).value("clan_id", clanID).value("rank_id", rankID).value("kills_high", killsHigh)
                .value("kills_medium", killsMedium).value("kills_low", killsLow).value("deaths_high", deathsHigh)
                .value("deaths_medium", deathsMedium).value("deaths_low", deathsLow).value("last_online_time", lastOnlineTime)
                .value("ff_protection", ffProtection).create();

    }

    public static PreparedStatement getUpdateClanPlayerQuery(ClanPlayerImpl clanPlayer) {
        int clanPlayerID = clanPlayer.getID();
        UUID uuid = clanPlayer.getUUID();
        String name = clanPlayer.getName();
        int clanID = -1;
        int rankID = -1;
        if (clanPlayer.getClan() != null) {
            clanID = clanPlayer.getClan().getID();
            rankID = clanPlayer.getRank().getID();
        }
        int killsHigh = clanPlayer.getKillDeath().getKills(KillDeathFactor.HIGH);
        int killsMedium = clanPlayer.getKillDeath().getKills(KillDeathFactor.MEDIUM);
        int killsLow = clanPlayer.getKillDeath().getKills(KillDeathFactor.LOW);
        int deathsHigh = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.HIGH);
        int deathsMedium = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.MEDIUM);
        int deathsLow = clanPlayer.getKillDeath().getDeaths(KillDeathFactor.LOW);

        boolean ffProtection = clanPlayer.isFfProtected();
        long lastOnlineTime = clanPlayer.getLastOnline().getTime();

        return QueryGenerator.createUpdateQuery("mcc_clanplayers", databaseConnectionOwner.getConnection())
                .value("uuid_most_sig_bits", uuid.getMostSignificantBits()).value("uuid_least_sig_bits", uuid.getLeastSignificantBits())
                .value("playername", name).value("clan_id", clanID).value("rank_id", rankID).value("kills_high", killsHigh)
                .value("kills_medium", killsMedium).value("kills_low", killsLow).value("deaths_high", deathsHigh)
                .value("deaths_medium", deathsMedium).value("deaths_low", deathsLow).value("last_online_time", lastOnlineTime)
                .value("ff_protection", ffProtection).where("clanplayer_id", clanPlayerID).create();

    }

    public static PreparedStatement getDeleteClanPlayerQuery(int clanPlayerID) {
        return QueryGenerator.createDeleteQuery("mcc_clanplayers", databaseConnectionOwner.getConnection()).where("clanplayer_id", clanPlayerID)
                .create();
    }

    public static PreparedStatement getInsertClanQuery(ClanImpl clan) {
        int clanID = clan.getID();
        String tag = clan.getTag();
        String name = clan.getName();
        int ownerID = clan.getOwner().getID();
        String tagColorId = clan.getTagColor().getId();
        boolean allowAllyInvites = clan.isAllowingAllyInvites();
        boolean ffProtection = clan.isFfProtected();
        long creationTime = clan.getCreationDate().getTime();
        Location<World> clanHome = clan.getHome();
        String clanHomeWorld = null;
        double clanHomeX = 0;
        double clanHomeY = 0;
        double clanHomeZ = 0;
        float clanHomeYaw = 0;
        float clanHomePitch = 0;
        if (clanHome != null) {
            clanHomeWorld = clanHome.getExtent().getUniqueId().toString();
            clanHomeX = clanHome.getX();
            clanHomeY = clanHome.getY();
            clanHomeZ = clanHome.getZ();
            // TODO SPONGE vector3d or something
//            clanHomeYaw = clanHome.getYaw();
//            clanHomePitch = clanHome.getPitch();
        }
        int homeSetTimes = clan.getHomeSetTimes();
        long homeLastSetTimeStamp = clan.getHomeSetTimeStamp();
        String bankId = clan.getBankId();

        return QueryGenerator.createInsertQuery("mcc_clans", databaseConnectionOwner.getConnection()).value("clan_id", clanID).value("clantag", tag)
                .value("clanname", name).value("clanplayer_id_owner", ownerID).value("tagcolor", tagColorId)
                .value("allow_ally_invites", allowAllyInvites).value("clanhome_world", clanHomeWorld).value("clanhome_x", clanHomeX)
                .value("clanhome_y", clanHomeY).value("clanhome_z", clanHomeZ).value("clanhome_yaw", clanHomeYaw)
                .value("clanhome_pitch", clanHomePitch).value("clanhome_set_times", homeSetTimes)
                .value("clanhome_set_timestamp", homeLastSetTimeStamp).value("ff_protection", ffProtection).value("creation_time", creationTime)
                .value("bank_id", bankId).create();
    }

    public static PreparedStatement getUpdateClanQuery(ClanImpl clan) {
        int clanID = clan.getID();
        String tag = clan.getTag();
        String name = clan.getName();
        int ownerID = clan.getOwner().getID();
        String tagColorId = clan.getTagColor().getId();
        boolean allowAllyInvites = clan.isAllowingAllyInvites();
        boolean ffProtection = clan.isFfProtected();
        long creationTime = clan.getCreationDate().getTime();
        Location<World> clanHome = clan.getHome();
        String clanHomeWorld = null;
        double clanHomeX = 0;
        double clanHomeY = 0;
        double clanHomeZ = 0;
        float clanHomeYaw = 0;
        float clanHomePitch = 0;
        if (clanHome != null) {
            clanHomeWorld = clanHome.getExtent().getUniqueId().toString();
            clanHomeX = clanHome.getX();
            clanHomeY = clanHome.getY();
            clanHomeZ = clanHome.getZ();
            // TODO SPONGE vector3d or something
//            clanHomeYaw = clanHome.getYaw();
//            clanHomePitch = clanHome.getPitch();
        }
        int homeSetTimes = clan.getHomeSetTimes();
        long homeLastSetTimeStamp = clan.getHomeSetTimeStamp();
        String bankId = clan.getBankId();

        return QueryGenerator.createUpdateQuery("mcc_clans", databaseConnectionOwner.getConnection()).value("clantag", tag).value("clanname", name)
                .value("clanplayer_id_owner", ownerID).value("tagcolor", tagColorId)
                .value("allow_ally_invites", allowAllyInvites)
                .value("clanhome_world", clanHomeWorld).value("clanhome_x", clanHomeX).value("clanhome_y", clanHomeY).value("clanhome_z", clanHomeZ)
                .value("clanhome_yaw", clanHomeYaw).value("clanhome_pitch", clanHomePitch).value("clanhome_set_times", homeSetTimes)
                .value("clanhome_set_timestamp", homeLastSetTimeStamp).value("ff_protection", ffProtection).value("creation_time", creationTime)
                .value("bank_id", bankId).where("clan_id", clanID).create();
    }

    public static PreparedStatement getDeleteClanQuery(int clanID) {
        return QueryGenerator.createDeleteQuery("mcc_clans", databaseConnectionOwner.getConnection()).where("clan_id", clanID).create();
    }

    public static PreparedStatement getInsertRankQuery(int clanID, RankImpl rank) {
        int rankID = rank.getID();
        String name = rank.getName();
        boolean changeable = rank.isChangeable();

        String permissions = rank.getPermissionsAsString();

        return QueryGenerator.createInsertQuery("mcc_ranks", databaseConnectionOwner.getConnection()).value("rank_id", rankID)
                .value("clan_id", clanID).value("rankname", name).value("permissions", permissions).value("changeable", changeable).create();
    }

    public static PreparedStatement getUpdateRankQuery(RankImpl rank) {
        int rankID = rank.getID();
        String name = rank.getName();
        boolean changeable = rank.isChangeable();

        String permissions = "";
        int i = 0;
        for (String perm : rank.getPermissions()) {
            if (i != 0) {
                permissions += ",";
            }
            permissions += perm;
            i++;
        }

        return QueryGenerator.createUpdateQuery("mcc_ranks", databaseConnectionOwner.getConnection()).value("rankname", name)
                .value("permissions", permissions).value("changeable", changeable).where("rank_id", rankID).create();
    }

    public static PreparedStatement getDeleteRankQuery(int rankID) {
        return QueryGenerator.createDeleteQuery("mcc_ranks", databaseConnectionOwner.getConnection()).where("rank_id", rankID).create();
    }

    public static PreparedStatement getInsertClanAllyQuery(int clanID, int clanID_ally) {
        return QueryGenerator.createInsertQuery("mcc_clans_allies", databaseConnectionOwner.getConnection()).value("clan_id", clanID)
                .value("clan_id_ally", clanID_ally).create();
    }

    public static PreparedStatement getDeleteClanAllyQuery(int clanID, int clanID_ally) {
        return QueryGenerator.createDeleteQuery("mcc_clans_allies", databaseConnectionOwner.getConnection()).where("clan_id", clanID)
                .and("clan_id_ally", clanID_ally).create();
    }
}
