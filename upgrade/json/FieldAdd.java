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

package nl.riebie.mcclans.persistence.upgrade.json;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by Kippers on 28/03/2016.
 */
public class FieldAdd {

    public final String fileName;
    public final String fieldName;
    public final Object fieldValue;

    public FieldAdd(String fileName, String fieldName, long fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName, boolean fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName, double fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName, Number fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName, String fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName, CustomValue fieldValue) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldAdd(String fileName, String fieldName) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.fieldValue = null;
    }

    public void addValueToWriter(JsonWriter jsonWriter) throws IOException {
        if (fieldValue instanceof Long) {
            jsonWriter.value((long) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            jsonWriter.value((boolean) fieldValue);
        } else if (fieldValue instanceof Double) {
            jsonWriter.value((double) fieldValue);
        } else if (fieldValue instanceof Number) {
            jsonWriter.value((Number) fieldValue);
        } else if (fieldValue instanceof String) {
            jsonWriter.value((String) fieldValue);
        } else if (fieldValue instanceof CustomValue) {
            jsonWriter.value(((CustomValue) fieldValue).getCustomValue());
        } else {
            jsonWriter.nullValue();
        }
    }

    public interface CustomValue {

        String getCustomValue();
    }
}
