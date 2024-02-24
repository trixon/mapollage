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

    private static final String KEY_THUMBNAIL_BORDER_SIZE = "thumbnail_border_size";
    private static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";
    private final int DEFAULT_THUMBNAIL_BORDER_SIZE = 3;
    private final int DEFAULT_THUMBNAIL_SIZE = 1000;
    private final Property<Integer> mThumbnailBorderSizeProperty = new SimpleObjectProperty<>();
    private final Property<Integer> mThumbnailSizeProperty = new SimpleObjectProperty<>();

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        mPreferences = NbPreferences.forModule(getClass());

        mThumbnailBorderSizeProperty.setValue(getInt(KEY_THUMBNAIL_BORDER_SIZE, DEFAULT_THUMBNAIL_BORDER_SIZE));
        mThumbnailSizeProperty.setValue(getInt(KEY_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE));

        initListeners();
    }

    public int getThumbnailBorderSize() {
        return mThumbnailBorderSizeProperty.getValue();
    }

    public int getThumbnailSize() {
        return mThumbnailSizeProperty.getValue();
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

        mThumbnailBorderSizeProperty.addListener(changeListener);
        mThumbnailSizeProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_THUMBNAIL_BORDER_SIZE, getThumbnailBorderSize());
        put(KEY_THUMBNAIL_SIZE, getThumbnailSize());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
