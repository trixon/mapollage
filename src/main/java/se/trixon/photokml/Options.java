/*
 * Copyright 2017 Patrik Karlsson.
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
package se.trixon.photokml;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 *
 * @author Patrik Karlsson
 */
public class Options {

    public static final String KEY_DEFAULT_LAT = "deflat";
    public static final String KEY_DEFAULT_LON = "deflon";
    public static final String KEY_LOCALE = "locale";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private final Double DEFAULT_LAT = 57.6;
    private final Double DEFAULT_LON = 11.3;
    private final Preferences mPreferences = Preferences.userNodeForPackage(Options.class);

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
    }

    public Double getDefaultLat() {
        return mPreferences.getDouble(KEY_DEFAULT_LAT, DEFAULT_LAT);
    }

    public Double getDefaultLon() {
        return mPreferences.getDouble(KEY_DEFAULT_LON, DEFAULT_LON);
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(mPreferences.get(KEY_LOCALE, DEFAULT_LOCALE.toLanguageTag()));
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public void setDefaultLat(Double value) {
        mPreferences.putDouble(KEY_DEFAULT_LAT, value);
    }

    public void setDefaultLon(Double value) {
        mPreferences.putDouble(KEY_DEFAULT_LON, value);
    }

    public void setLocale(Locale locale) {
        mPreferences.put(KEY_LOCALE, locale.toLanguageTag());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
