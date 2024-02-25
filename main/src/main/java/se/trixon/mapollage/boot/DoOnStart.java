/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.mapollage.boot;

import java.util.prefs.BackingStoreException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.dialogs.NbOptionalDialog;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.PrefsHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class DoOnStart implements Runnable {

    static {
        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");

        try {
            var key = "laf";
            var defaultLAF = !SystemUtils.IS_OS_MAC ? "com.formdev.flatlaf.FlatLightLaf" : "com.formdev.flatlaf.themes.FlatMacLightLaf";
            var preferences = NbPreferences.root().node("laf");
            PrefsHelper.putIfAbsent(preferences, key, defaultLAF);

            var nightMode = StringUtils.containsIgnoreCase(preferences.get(key, ""), "dark");
            OutputLineMode.setNightMode(nightMode);
            if (nightMode) {
                FxHelper.setDarkThemeEnabled(nightMode);
                var color = FxHelper.getFillColorForDarkTheme();
                MaterialIcon.setDefaultColor(color);
                se.trixon.almond.util.icons.material.swing.MaterialIcon.setDefaultColor(FxHelper.colorToColor(color));
            }
//            Options.getInstance().setNightMode(nightMode);

            preferences = NbPreferences.root().node("org/netbeans/swing/laf/flatlaf");
            PrefsHelper.putIfAbsent(preferences, "accentColor", "#ff453a");
        } catch (BackingStoreException ex) {
            //Exceptions.printStackTrace(ex);
        }

        NbOptionalDialog.setPreferences(NbPreferences.forModule(NbOptionalDialog.class).node("optionalDialogState"));
    }

    @Override
    public void run() {
    }

}
