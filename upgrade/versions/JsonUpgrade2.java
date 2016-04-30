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

import nl.riebie.mcclans.persistence.upgrade.interfaces.JsonUpgrade;
import nl.riebie.mcclans.persistence.upgrade.json.FieldAdd;
import nl.riebie.mcclans.persistence.upgrade.json.FieldRemove;
import nl.riebie.mcclans.persistence.upgrade.json.FieldRename;

import java.util.List;
import java.util.UUID;

/**
 * Created by Kippers on 19/03/2016.
 */
public class JsonUpgrade2 extends JsonUpgrade {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    protected List<FieldAdd> getFieldAdditions(List<FieldAdd> fieldAdditions) {
        fieldAdditions.add(new FieldAdd("clans", "bankId", () -> UUID.randomUUID().toString()));
        return fieldAdditions;
    }

    @Override
    protected List<FieldRemove> getFieldRemovals(List<FieldRemove> fieldRemovals) {
        return fieldRemovals;
    }

    @Override
    protected List<FieldRename> getFieldRenames(List<FieldRename> fieldRenames) {
        return fieldRenames;
    }
}
