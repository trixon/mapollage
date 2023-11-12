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
package se.trixon.mapollage.ui.options;

import java.awt.BorderLayout;
import javafx.scene.Scene;
import javax.swing.JPanel;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.mapollage.Options;

final class MainPanel extends JPanel {

    private final MainPanelController mController;
    private final FxPanel mFxPanel;
    private final Options mOptions = Options.getInstance();
    private OptionsPanel mOptionsPanel;

    MainPanel(MainPanelController controller) {
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                mOptionsPanel = new OptionsPanel();
                mOptionsPanel.setPadding(FxHelper.getUIScaledInsets(8));
                var scene = new Scene(mOptionsPanel);
                setScene(scene);
            }
        };

        mFxPanel.initFx(null);
        mFxPanel.setPreferredSize(null);

        setLayout(new BorderLayout());
        add(mFxPanel, BorderLayout.CENTER);
    }

    void load() {
    }

    void store() {
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
