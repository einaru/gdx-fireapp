/*
 * Copyright 2018 mk
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

package pl.mk5.gdx.fireapp.html.crash;

import pl.mk5.gdx.fireapp.distributions.CrashDistribution;

/**
 * At GWT platform crash distribution does not do anything.
 *
 * @see CrashDistribution
 */
public class Crash implements CrashDistribution {

    @Override
    public void log(String message) {
        // No supported at web platform
    }

    @Override
    public void initialize() {
        // No supported at web platform
    }

    @Override
    public void recordException(Throwable throwable) {
        // No supported at web platform
    }

    @Override
    public void setUserId(String userId) {
        // No supported at web platform
    }

    @Override
    public <T> void setCustomKey(String key, T value) {
        // No supported at web platform
    }
}
