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

package nl.riebie.mcclans.persistence.upgrade.versions;

import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.QueryGenerator;
import nl.riebie.mcclans.persistence.query.DataType;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DatabaseUpgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseUpgrade2 extends DatabaseUpgrade {
    private static final String GET_CLANS_QUERY = "SELECT * FROM mcc_clans";

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void upgradeDatabase() {
        alterTable("mcc_clans").addColumn("bank_id", DataType.STRING);
        updateClanIdColumn();
    }

    private void updateClanIdColumn() {
        ResultSet clanResultSet = DatabaseConnectionOwner.getInstance().executeQuery(GET_CLANS_QUERY);
        if (clanResultSet != null) {
            try {
                while (clanResultSet.next()) {
                    int clanId = clanResultSet.getInt("clan_id");
                    UUID uuid = UUID.randomUUID();
                    updateQuery("mcc_clans").value("bank_id", uuid.toString()).where("clan_id", clanId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
