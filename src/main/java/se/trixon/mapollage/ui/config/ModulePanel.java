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
package se.trixon.mapollage.ui.config;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.ImageIcon;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.IconColor;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class ModulePanel extends javax.swing.JPanel {

    public static final int ICON_SIZE = 48;
    public static final String MULTILINE_DIVIDER = "* * * * *";
    protected final ResourceBundle mBundle = BundleHelper.getBundle(ModulePanel.class, "Bundle");
    protected final String mHeaderPrefix = " + ";
    protected final Options mOptions = Options.getInstance();
    protected Profile mProfile;
    protected String mTitle;

    public ModulePanel() {
        mOptions.getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                onPreferenceChange(evt);
            }
        });
    }

    public Locale getDateFormatLocale() {
        return mOptions.getLocale();
    }

    public abstract StringBuilder getHeaderBuilder();

    public abstract ImageIcon getIcon();

    public IconColor getIconColor() {
        return AlmondOptions.getInstance().getIconColor();
    }

    public String getTitle() {
        return mTitle;
    }

    public abstract boolean hasValidSettings();

    public abstract void load(Profile profile);

    public void onPreferenceChange(PreferenceChangeEvent evt) {
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    protected void append(StringBuilder sb, String key, String value) {
        sb.append(mHeaderPrefix).append(String.format("%s: %s\n", key, value));
    }

    protected void invalidSettings(String message) {
        Message.error(this, Dict.INVALID_SETTING.toString(), String.format("<html><h3>%s</h3>%s", mTitle, message));
    }

    protected void optAppend(StringBuilder sb, boolean state, String string) {
        if (state) {
            sb.append(mHeaderPrefix).append(string).append("\n");
        }
    }
}
