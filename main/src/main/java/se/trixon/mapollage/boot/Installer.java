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

import java.io.IOException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import se.trixon.mapollage.core.StorageManager;

public class Installer extends ModuleInstall {

    private final StorageManager mStorageManager = StorageManager.getInstance();

    @Override
    public void restored() {
        try {
            mStorageManager.load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
