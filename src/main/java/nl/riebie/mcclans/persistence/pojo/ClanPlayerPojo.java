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

package nl.riebie.mcclans.persistence.pojo;

import nl.riebie.mcclans.api.enums.KillDeathFactor;
import nl.riebie.mcclans.player.ClanPlayerImpl;

/**
 * Created by Kippers on 28/03/2016.
 */
public class ClanPlayerPojo {

    public String playerName;
    public int clanPlayerID = -1;
    public long uuidMostSigBits = -1;
    public long uuidLeastSigBits = -1;
    public int clanID = -1;
    public int rankID = -1;
    public int killsHigh = 0;
    public int killsMedium = 0;
    public int killsLow = 0;
    public int deathsHigh = 0;
    public int deathsMedium = 0;
    public int deathsLow = 0;
    public boolean ffProtection = true;
    public long lastOnlineTime = 0;

    public static ClanPlayerPojo from(ClanPlayerImpl cp) {
        ClanPlayerPojo clanPlayerPojo = new ClanPlayerPojo();
        clanPlayerPojo.clanPlayerID = cp.getID();
        clanPlayerPojo.uuidMostSigBits = cp.getUUID().getMostSignificantBits();
        clanPlayerPojo.uuidLeastSigBits = cp.getUUID().getLeastSignificantBits();
        clanPlayerPojo.playerName = cp.getName();

        int clanID = -1;
        int rankID = -1;
        if (cp.getClan() != null) {
            clanID = cp.getClan().getID();
            rankID = cp.getRank().getID();
        }

        clanPlayerPojo.clanID = clanID;
        clanPlayerPojo.rankID = rankID;
        clanPlayerPojo.killsHigh = cp.getKillDeath().getKills(KillDeathFactor.HIGH);
        clanPlayerPojo.killsMedium = cp.getKillDeath().getKills(KillDeathFactor.MEDIUM);
        clanPlayerPojo.killsLow = cp.getKillDeath().getKills(KillDeathFactor.LOW);
        clanPlayerPojo.deathsHigh = cp.getKillDeath().getDeaths(KillDeathFactor.HIGH);
        clanPlayerPojo.deathsMedium = cp.getKillDeath().getDeaths(KillDeathFactor.MEDIUM);
        clanPlayerPojo.deathsLow = cp.getKillDeath().getDeaths(KillDeathFactor.LOW);
        clanPlayerPojo.ffProtection = cp.isFfProtected();
        clanPlayerPojo.lastOnlineTime = cp.getLastOnline().getTime();
        return clanPlayerPojo;
    }

}
