/**
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.agent.autotest.impl.selenium

@Deprecated("EPMDJ-8435 Should be remove with proxy")
object WebDriverThreadStorage {
    private val remoteWebDriver = InheritableThreadLocal<Any>()

    fun set(obj: Any) = remoteWebDriver.set(obj)

    fun addCookies() {
        runCatching {
            remoteWebDriver.get()?.let {
                it.javaClass.getDeclaredMethod("addDrillCookies").invoke(it)
            }
        }.getOrNull()
    }
}