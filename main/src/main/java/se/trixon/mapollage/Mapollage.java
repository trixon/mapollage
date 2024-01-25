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
package se.trixon.mapollage;

import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Mapollage {

    public static final String KEY_INFO = "info";
    private static final int ICON_SIZE_TOOLBAR = 32;
    private static final GlobalState sGlobalState = new GlobalState();

    static {
        sGlobalState.addListener(gsce -> {
            var io = IOProvider.getDefault().getIO(Dict.INFORMATION.toString(), false);
            var outputHelper = new OutputHelper(Dict.INFORMATION.toString(), io, false);

            io.select();
            try (var out = io.getOut()) {
                out.reset();
                outputHelper.println(OutputLineMode.INFO, gsce.getValue());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, KEY_INFO);
    }

    public static void displaySystemInformation() {
        sGlobalState.put(KEY_INFO, SystemHelper.getSystemInfo());
    }

    public static GlobalState getGlobalState() {
        return sGlobalState;
    }

    public static int getIconSizeToolBar() {
        return FxHelper.getUIScaled(ICON_SIZE_TOOLBAR);
    }

}
