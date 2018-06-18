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

package nl.riebie.mcclans.persistence;

import java.sql.PreparedStatement;

import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.persistence.implementations.DatabaseSaver;
import nl.riebie.mcclans.player.ClanPlayerImpl;

public class TaskForwarder {

    public static void sendInsertClanPlayer(ClanPlayerImpl clanPlayer) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getInsertClanPlayerQuery(clanPlayer));
        }
    }

    public static void sendUpdateClanPlayer(ClanPlayerImpl clanPlayer) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getUpdateClanPlayerQuery(clanPlayer));
        }
    }

    public static void sendDeleteClanPlayer(int clanPlayerID) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getDeleteClanPlayerQuery(clanPlayerID));
        }
    }

    public static void sendInsertClan(ClanImpl clan) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getInsertClanQuery(clan));
        }
    }

    public static void sendUpdateClan(ClanImpl clan) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getUpdateClanQuery(clan));
        }
    }

    public static void sendDeleteClan(int clanID) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getDeleteClanQuery(clanID));
        }
    }

    public static void sendInsertRank(int clanID, RankImpl rank) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getInsertRankQuery(clanID, rank));
        }
    }

    public static void sendUpdateRank(RankImpl rank) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getUpdateRankQuery(rank));
        }
    }

    public static void sendDeleteRank(int rankID) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getDeleteRankQuery(rankID));
        }
    }

    public static void sendInsertClanAlly(int clanID, int clanID_ally) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getInsertClanAllyQuery(clanID, clanID_ally));
        }
    }

    public static void sendDeleteClanAlly(int clanID, int clanID_ally) {
        if (TaskExecutor.getInstance().isRunning()) {
            queueTask(DatabaseSaver.getDeleteClanAllyQuery(clanID, clanID_ally));
        }
    }

    public static void queueTask(PreparedStatement query) {
        if (Config.getBoolean(Config.USE_DATABASE)) {
            MCClansDatabaseTask task = new MCClansDatabaseTask(query);
            TaskExecutor.getInstance().enqueue(task);
        }
    }
}