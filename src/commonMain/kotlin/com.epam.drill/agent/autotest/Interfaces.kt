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
package com.epam.drill.agent.autotest

import com.epam.drill.agent.strategy.*


interface IThreadStorage : Marker {

    fun getTestName(): String?

    fun memorizeTestName(testName: String?)

    fun sessionId(): String?

    @Deprecated("EPMDJ-8435 Should be remove with proxy")
    fun proxyUrl(): String?
}

interface IDevToolStorage : Marker {
    fun addHeaders(headers: Map<*, *>)
    fun setDevTool(devTool: IChromeDevTool)
    fun getDevTool(): IChromeDevTool?
    fun isHeadersAdded(): Boolean
}

interface IChromeDevTool : Marker {
    fun addHeaders(headers: Map<String, String>)
    fun connect(capabilities: Map<*, *>?, sessionId: String?, remoteHost: String?)
    fun close()
}
