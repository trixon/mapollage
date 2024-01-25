/*
 * Copyright 2022 Patrik Karlström.
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final String KEY_DEFAULT_LAT = "deflat";
    private static final String KEY_DEFAULT_LON = "deflon";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_THUMBNAIL_BORDER_SIZE = "thumbnail_border_size";
    private static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";
    private final Double DEFAULT_LAT = 57.6;
    private final Double DEFAULT_LON = 11.3;
    private final int DEFAULT_THUMBNAIL_BORDER_SIZE = 3;
    private final int DEFAULT_THUMBNAIL_SIZE = 1000;
    private final Property<Double> mDefaultLatProperty = new SimpleObjectProperty<>();
    private final Property<Double> mDefaultLonProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Locale> mLocaleProperty = new SimpleObjectProperty<>();
    private final Property<Integer> mThumbnailBorderSizeProperty = new SimpleObjectProperty<>();
    private final Property<Integer> mThumbnailSizeProperty = new SimpleObjectProperty<>();

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        mPreferences = NbPreferences.forModule(getClass());

        mDefaultLatProperty.setValue(getDouble(KEY_DEFAULT_LAT, DEFAULT_LAT));
        mDefaultLonProperty.setValue(getDouble(KEY_DEFAULT_LON, DEFAULT_LON));

        mThumbnailBorderSizeProperty.setValue(getInt(KEY_THUMBNAIL_BORDER_SIZE, DEFAULT_THUMBNAIL_BORDER_SIZE));
        mThumbnailSizeProperty.setValue(getInt(KEY_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE));
        mLocaleProperty.set(Locale.forLanguageTag(get(KEY_LOCALE, DEFAULT_LOCALE.toLanguageTag())));

        initListeners();
    }

    public Property<Double> defaultLatProperty() {
        return mDefaultLatProperty;
    }

    public Property<Double> defaultLonProperty() {
        return mDefaultLonProperty;
    }

    public Double getDefaultLat() {
        return mDefaultLatProperty.getValue();
    }

    public Double getDefaultLon() {
        return mDefaultLonProperty.getValue();
    }

    public Locale getLocale() {
        return localeProperty().get();
    }

    public int getThumbnailBorderSize() {
        return mThumbnailBorderSizeProperty.getValue();
    }

    public int getThumbnailSize() {
        return mThumbnailSizeProperty.getValue();
    }

    public ObjectProperty<Locale> localeProperty() {
        return mLocaleProperty;
    }

    public void setDefaultLat(Double value) {
        mDefaultLatProperty.setValue(value);
    }

    public void setDefaultLon(Double value) {
        mDefaultLonProperty.setValue(value);
    }

    public void setLocale(Locale locale) {
        mLocaleProperty.set(locale);
    }

    public void setThumbnailBorderSize(int size) {
        mThumbnailBorderSizeProperty.setValue(size);
    }

    public void setThumbnailSize(int size) {
        mThumbnailSizeProperty.setValue(size);
    }

    public Property<Integer> thumbnailBorderSizeProperty() {
        return mThumbnailBorderSizeProperty;
    }

    public Property<Integer> thumbnailSizeProperty() {
        return mThumbnailSizeProperty;
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            save();
        };

        mDefaultLatProperty.addListener(changeListener);
        mDefaultLonProperty.addListener(changeListener);
        mThumbnailBorderSizeProperty.addListener(changeListener);
        mThumbnailSizeProperty.addListener(changeListener);
        mLocaleProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_DEFAULT_LAT, getDefaultLat());
        put(KEY_DEFAULT_LON, getDefaultLon());
        put(KEY_THUMBNAIL_BORDER_SIZE, getThumbnailBorderSize());
        put(KEY_THUMBNAIL_SIZE, getThumbnailSize());
        put(KEY_LOCALE, getLocale().toLanguageTag());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
