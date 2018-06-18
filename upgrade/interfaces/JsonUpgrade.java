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

package nl.riebie.mcclans.persistence.upgrade.interfaces;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.upgrade.json.FieldAdd;
import nl.riebie.mcclans.persistence.upgrade.json.FieldRemove;
import nl.riebie.mcclans.persistence.upgrade.json.FieldRename;
import nl.riebie.mcclans.utils.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kippers on 19/03/2016.
 */
public abstract class JsonUpgrade extends DataUpgrade {

    protected static final File recentDataFolder = new File(MCClans.getPlugin().getDataFolder(), "recent");
    protected static final File tempDataFolder = new File(MCClans.getPlugin().getDataFolder(), "temp");

    private List<String> fileNames = Arrays.asList("clans", "clanPlayers", "ranks", "allies");

    protected abstract List<FieldAdd> getFieldAdditions(List<FieldAdd> fieldAdditions);

    protected abstract List<FieldRemove> getFieldRemovals(List<FieldRemove> fieldRemovals);

    protected abstract List<FieldRename> getFieldRenames(List<FieldRename> fieldRenames);

    @Override
    public void upgrade() {
        for (String fileName : fileNames) {
            upgrade(fileName);
        }
    }

    private void upgrade(String fileName) {
        List<FieldAdd> fieldAdds = getFieldAdditions(new ArrayList<>());
        List<FieldRemove> fieldRemoves = getFieldRemovals(new ArrayList<>());
        List<FieldRename> fieldRenames = getFieldRenames(new ArrayList<>());

        try {
            File file = new File(recentDataFolder, fileName + ".json");
            File tempFile = new File(tempDataFolder, fileName + ".json");
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();

            JsonReader reader = new JsonReader(new FileReader(file));
            JsonWriter writer = new JsonWriter(new FileWriter(tempFile));

            String name = "";
            endWhile:
            while (true) {
                JsonToken token = reader.peek();
                endCase:
                switch (token) {
                    case BEGIN_ARRAY:
                        reader.beginArray();
                        writer.beginArray();
                        break;
                    case END_ARRAY:
                        reader.endArray();
                        writer.endArray();
                        break;
                    case BEGIN_OBJECT:
                        reader.beginObject();
                        writer.beginObject();

                        if (name.equals("list")) {
                            for (FieldAdd fieldAdd : fieldAdds) {
                                if (fileName.equals(fieldAdd.fileName)) {
                                    writer.name(fieldAdd.fieldName);
                                    fieldAdd.addValueToWriter(writer);
                                }
                            }
                        }
                        break;
                    case END_OBJECT:
                        reader.endObject();
                        writer.endObject();
                        break;
                    case NAME:
                        name = reader.nextName();

                        for (FieldRemove fieldRemove : fieldRemoves) {
                            if (fileName.equals(fieldRemove.fileName) && name.equals(fieldRemove.fieldName)) {
                                reader.skipValue();
                                break endCase;
                            }
                        }

                        for (FieldRename fieldRename : fieldRenames) {
                            if (fileName.equals(fieldRename.fileName) && name.equals(fieldRename.fieldName)) {
                                name = fieldRename.newFieldName;
                            }
                        }

                        writer.name(name);
                        break;
                    case STRING:
                        String s = reader.nextString();
                        writer.value(s);
                        break;
                    case NUMBER:
                        String n = reader.nextString();

                        if (name.equals("dataVersion")) {
                            n = String.valueOf(getVersion());
                        }

                        writer.value(new BigDecimal(n));
                        break;
                    case BOOLEAN:
                        boolean b = reader.nextBoolean();
                        writer.value(b);
                        break;
                    case NULL:
                        reader.nextNull();
                        writer.nullValue();
                        break;
                    case END_DOCUMENT:
                        break endWhile;
                }
            }

            writer.close();
            reader.close();

            FileUtils.moveFile(tempFile, file);
            tempFile.delete();
        } catch (IOException e) {
            throw new WrappedDataException(e);
        }
    }
}
