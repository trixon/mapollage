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
import java.util.Locale;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Mapollage {

    private static final int ICON_SIZE_TOOLBAR = 32;

    public static int getIconSizeToolBar() {
        return FxHelper.getUIScaled(ICON_SIZE_TOOLBAR);
    }

    public static void displaySystemInformation() {
        String s = "%s\n%s".formatted(
                Dict.SYSTEM.toString().toUpperCase(Locale.ENGLISH),
                SystemHelper.getSystemInfo()
        );

        var io = IOProvider.getDefault().getIO(Dict.INFORMATION.toString(), false);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        io.getOut().println(s);
        io.getOut().close();
    }

}
