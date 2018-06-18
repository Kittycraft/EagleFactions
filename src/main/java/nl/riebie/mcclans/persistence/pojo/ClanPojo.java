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

import nl.riebie.mcclans.clan.ClanImpl;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Created by Kippers on 28/03/2016.
 */
public class ClanPojo {

    public int clanID = -1;
    public String clanTag;
    public String clanName;
    public int ownerID = -1;
    public String tagColorId;
    public boolean allowAllyInvites = true;
    public boolean ffProtection = true;
    public long creationTime = 0;

    public String homeWorld;
    public double homeX = 0;
    public double homeY = 0;
    public double homeZ = 0;
    public float homeYaw = 0;
    public float homePitch = 0;

    public int homeSetTimes = 0;
    public long homeSetTimeStamp = -1;

    public String bankId;

    public static ClanPojo from(ClanImpl clan) {
        ClanPojo clanPojo = new ClanPojo();
        clanPojo.clanID = clan.getID();
        clanPojo.clanTag = clan.getTag();
        clanPojo.clanName = clan.getName();
        clanPojo.ownerID = clan.getOwner().getID();
        clanPojo.tagColorId = clan.getTagColor().getId();
        clanPojo.allowAllyInvites = clan.isAllowingAllyInvites();
        clanPojo.ffProtection = clan.isFfProtected();
        clanPojo.creationTime = clan.getCreationDate().getTime();

        Location<World> homeLocation = clan.getHome();

        String homeWorld = null;
        double homeX = 0;
        double homeY = 0;
        double homeZ = 0;
        float homeYaw = 0;
        float homePitch = 0;

        if (homeLocation != null) {
            homeWorld = homeLocation.getExtent().getUniqueId().toString();
            homeX = homeLocation.getX();
            homeY = homeLocation.getY();
            homeZ = homeLocation.getZ();
            // TODO SPONGE vector or something
//			homeYaw = homeLocation.getYaw();
//			homePitch = homeLocation.getPitch();
        }

        clanPojo.homeWorld = homeWorld;
        clanPojo.homeX = homeX;
        clanPojo.homeY = homeY;
        clanPojo.homeZ = homeZ;
        clanPojo.homeYaw = homeYaw;
        clanPojo.homePitch = homePitch;
        clanPojo.homeSetTimes = clan.getHomeSetTimes();
        clanPojo.homeSetTimeStamp = clan.getHomeSetTimeStamp();
        clanPojo.bankId = clan.getBankId();
        return clanPojo;
    }

}
