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

import nl.riebie.mcclans.clan.RankImpl;

/**
 * Created by Kippers on 28/03/2016.
 */
public class RankPojo {

    public int rankID = -1;
    public int clanID = -1;
    public String rankName;
    public String permissions;
    public boolean changeable = true;

    public static RankPojo from(int clanID, RankImpl rank) {
        RankPojo rankPojo = new RankPojo();
        rankPojo.rankID = rank.getID();
        rankPojo.clanID = clanID;
        rankPojo.rankName = rank.getName();
        rankPojo.changeable = rank.isChangeable();
        rankPojo.permissions = rank.getPermissionsAsString();
        return rankPojo;
    }

}
