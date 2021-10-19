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

import com.epam.drill.agent.autotest.*
import com.epam.drill.agent.java.impl.*
import com.epam.drill.agent.strategy.manager.*
import com.epam.drill.logger.*

internal object ChromeDevTool : IChromeDevTool {

    private val logger = Logging.logger(HttpRequest::class.java.name)

    private val chromeDevTool: IChromeDevTool?
        get() = StrategyManager.getImplementation()

    override fun addHeaders(headers: Map<String, String>) {
        chromeDevTool?.addHeaders(headers)
    }

    override fun connect(capabilities: Map<*, *>?, sessionId: String?, remoteHost: String?) {
        chromeDevTool?.connect(capabilities, sessionId, remoteHost)
    }

    override fun close() {
        chromeDevTool?.close()
    }
}
