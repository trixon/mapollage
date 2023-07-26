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

/**
 *
 * @author Patrik Karlström
 */
public interface OperationListener {

    void onOperationError(String message);

    void onOperationFailed(String message);

    void onOperationFinished(String message, int placemarkCount);

    void onOperationInterrupted();

    void onOperationLog(String message);

    void onOperationProcessingStarted();

    void onOperationProgress(String message);

    void onOperationProgress(int value, int max);

    void onOperationStarted();
}