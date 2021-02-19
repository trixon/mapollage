/*
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.mapollage;

import java.util.Locale;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    public static final String KEY_AUTO_OPEN = "auto_open";
    public static final String KEY_CLEAN_NS2 = "clean_ns2";
    public static final String KEY_CLEAN_SPACE = "clean_space";
    public static final String KEY_DEFAULT_LAT = "deflat";
    public static final String KEY_DEFAULT_LON = "deflon";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_LOG_KML = "log_kml";
    public static final String KEY_THUMBNAIL_BORDER_SIZE = "thumbnail_border_size";
    public static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";
    public static final String KEY_WORD_WRAP = "word_wrap";
    private static final boolean DEFAULT_CLEAN_NS2 = true;
    private static final boolean DEFAULT_CLEAN_SPACE = true;
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final boolean DEFAULT_LOG_KML = false;
    private static final boolean DEFAULT_WORD_WRAP = false;
    private static final String KEY_UI_NIGHTMODE = "ui.nightmode";
    private static final String KEY_UI_WORDWRAP = "ui.wordwrap";
    private final boolean DEFAULT_AUTO_OPEN = true;
    private final Double DEFAULT_LAT = 57.6;
    private final Double DEFAULT_LON = 11.3;
    private final int DEFAULT_THUMBNAIL_BORDER_SIZE = 3;
    private final int DEFAULT_THUMBNAIL_SIZE = 1000;
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty();
    private final BooleanProperty mWordWrapProperty = new SimpleBooleanProperty();

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        setPreferences(Preferences.userNodeForPackage(Mapollage.class));

        mNightModeProperty.set(is(KEY_UI_NIGHTMODE, true));
        mWordWrapProperty.set(is(KEY_UI_WORDWRAP, true));

        initListeners();
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

    public int getThumbnailBorderSize() {
        return mPreferences.getInt(KEY_THUMBNAIL_BORDER_SIZE, DEFAULT_THUMBNAIL_BORDER_SIZE);
    }

    public int getThumbnailSize() {
        return mPreferences.getInt(KEY_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE);
    }

    public boolean isAutoOpen() {
        return mPreferences.getBoolean(KEY_AUTO_OPEN, DEFAULT_AUTO_OPEN);
    }

    public boolean isCleanNs2() {
        return mPreferences.getBoolean(KEY_CLEAN_NS2, DEFAULT_CLEAN_NS2);
    }

    public boolean isCleanSpace() {
        return mPreferences.getBoolean(KEY_CLEAN_SPACE, DEFAULT_CLEAN_SPACE);
    }

    public boolean isLogKml() {
        return mPreferences.getBoolean(KEY_LOG_KML, DEFAULT_LOG_KML);
    }

    public boolean isNightMode() {
        return mNightModeProperty.get();
    }

    public boolean isWordWrap() {
        return mWordWrapProperty.get();
    }

    public BooleanProperty nightModeProperty() {
        return mNightModeProperty;
    }

    public void setAutoOpen(boolean value) {
        mPreferences.putBoolean(KEY_AUTO_OPEN, value);
    }

    public void setCleanNs2(boolean value) {
        mPreferences.putBoolean(KEY_CLEAN_NS2, value);
    }

    public void setCleanSpace(boolean value) {
        mPreferences.putBoolean(KEY_CLEAN_SPACE, value);
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

    public void setLogKml(boolean value) {
        mPreferences.putBoolean(KEY_LOG_KML, value);
    }

    public void setNightMode(boolean nightMode) {
        mNightModeProperty.set(nightMode);
    }

    public void setThumbnailBorderSize(int size) {
        mPreferences.putInt(KEY_THUMBNAIL_BORDER_SIZE, size);
    }

    public void setThumbnailSize(int size) {
        mPreferences.putInt(KEY_THUMBNAIL_SIZE, size);
    }

    public void setWordWrap(boolean value) {
        mPreferences.putBoolean(KEY_WORD_WRAP, value);
    }

    public BooleanProperty wordWrapProperty() {
        return mWordWrapProperty;
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            save();
        };

        mNightModeProperty.addListener(changeListener);
        mWordWrapProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_UI_NIGHTMODE, isNightMode());
        put(KEY_UI_WORDWRAP, isWordWrap());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
