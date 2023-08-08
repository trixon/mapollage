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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
        position = 0,
        categoryName = "#OptionsCategory_Name_Main",
        iconBase = "se/trixon/mapollage/ui/options/mapollage32.png",
        keywords = "#OptionsCategory_Keywords_Main",
        keywordsCategory = "Main"
)
@org.openide.util.NbBundle.Messages({"OptionsCategory_Name_Main=Main", "OptionsCategory_Keywords_Main=main"})
public final class MainPanelController extends OptionsPanelController {

    private boolean mChanged;
    private MainPanel mPanel;
    private final PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            getPanel().store();
            mChanged = false;
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public boolean isChanged() {
        return mChanged;
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void update() {
        getPanel().load();
        mChanged = false;
    }

    void changed() {
        if (!mChanged) {
            mChanged = true;
            mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private MainPanel getPanel() {
        if (mPanel == null) {
            mPanel = new MainPanel(this);
        }
        return mPanel;
    }

}
