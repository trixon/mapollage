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
package se.trixon.mapollage.ui.task;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.validation.ValidationSupport;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTab extends Tab {

    public static final int ICON_SIZE = FxHelper.getUIScaled(22);
    public static final String MULTILINE_DIVIDER = "* * * * *";
    protected static ValidationSupport sValidationSupport;
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Insets mTopInsets = FxHelper.getUIScaledInsets(8, 0, 0, 0);
    protected final ResourceBundle mBundle = NbBundle.getBundle(BaseTab.class);
    protected final String mHeaderPrefix = " + ";
    protected final Options mOptions = Options.getInstance();
    protected Task mTask;
    protected final TaskManager mTaskManager = TaskManager.getInstance();
    protected String mTitle;

    public static void setValidationSupport(ValidationSupport validationSupport) {
        BaseTab.sValidationSupport = validationSupport;
    }

    public BaseTab() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            onPreferenceChange(pce);
        });
    }

    public Locale getDateFormatLocale() {
        return mOptions.getLocale();
    }

    public String getTitle() {
        return mTitle;
    }

    public abstract void load(Task task);

    public void onPreferenceChange(PreferenceChangeEvent evt) {
    }

    public abstract void save();

    public void setTitle(String title) {
        mTitle = title;
    }

    protected void addTopMargin(Region... regions) {
        for (var region : regions) {
            GridPane.setMargin(region, mTopInsets);
        }
    }

    protected void addTopPadding(Region... regions) {
        for (var region : regions) {
            region.setPadding(mTopInsets);
        }
    }

    protected void append(StringBuilder sb, String key, String value) {
        sb.append(mHeaderPrefix).append(String.format("%s: %s\n", key, value));
    }

    protected void invalidSettings(String message) {
        //Message.error(this, Dict.INVALID_SETTING.toString(), String.format("<html><h3>%s</h3>%s", mTitle, message));
    }

    protected void optAppend(StringBuilder sb, boolean state, String string) {
        if (state) {
            sb.append(mHeaderPrefix).append(string).append("\n");
        }
    }

    protected void setGraphic(char c) {
        var glyph = mFontAwesome.create(c).size(ICON_SIZE).color(FxHelper.isDarkThemeEnabled() ? Color.LIGHTGRAY : Color.BLACK);
        glyph.setPadding(FxHelper.getUIScaledInsets(8));
        setGraphic(glyph);
    }

}
