/*
 * Copyright 2017 mk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mk.gdx.firebase.callbacks;

import mk.gdx.firebase.storage.FileMetadata;

/**
 * Handles response after uploading data to Firebase storage.
 */
public interface UploadCallback
{
    /**
     * Calls when everything was done without issues.
     *
     * @param fileMetadata Metadata of just uploaded file.
     */
    void onSuccess(FileMetadata fileMetadata);

    /**
     * Calls when something goes wrong.
     *
     * @param e Exception with description what was wrong.
     */
    void onFail(Exception e);
}
