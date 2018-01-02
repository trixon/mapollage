/*
 * Copyright 2018 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.mapollage.profile;

import java.util.LinkedHashMap;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileInfo {

    private int mMaxLength = Integer.MIN_VALUE;
    private String mTitle;
    private LinkedHashMap<String, String> mValues;

    public ProfileInfo() {
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public String getTitle() {
        return mTitle;
    }

    public LinkedHashMap<String, String> getValues() {
        return mValues;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setValues(LinkedHashMap<String, String> values) {
        mValues = values;
        values.keySet().forEach((key) -> {
            mMaxLength = Math.max(mMaxLength, key.length());
        });
    }
}
