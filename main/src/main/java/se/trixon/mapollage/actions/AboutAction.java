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
package se.trixon.mapollage.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import se.trixon.almond.nbp.core.ModuleHelper;
import se.trixon.almond.nbp.dialogs.NbAbout;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.AboutModel;
import se.trixon.mapollage.Mapollage;

@ActionID(
        category = "Help",
        id = "se.trixon.mapollage.actions.AboutAction"
)
public final class AboutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        var aboutModel = new AboutModel(SystemHelper.getBundle(Mapollage.class, "about"), SystemHelper.getResourceAsImageIcon(Mapollage.class, "about_logo.png"));
        aboutModel.setAppDate(ModuleHelper.getBuildTime(Mapollage.class));

        new NbAbout(aboutModel).display();
    }
}
