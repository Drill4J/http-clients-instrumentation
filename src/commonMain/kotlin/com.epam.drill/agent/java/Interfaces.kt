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
package com.epam.drill.agent.java

import com.epam.drill.agent.strategy.*


interface IHttpRequest : Marker {
    fun parse(buffers: Array<*>)
    fun storeDrillHeaders(headers: Map<String, String>?)
    fun loadDrillHeaders(): Map<String, String>?
}

interface IPluginExtension : Marker {
    fun processServerRequest()
    fun processServerResponse()
}

interface IBasicResponseHeaders : Marker {
    fun adminAddressHeader(): String?
    fun retrieveAdminAddress(): String?
    fun idHeaderConfigKey(): String?
    fun idHeaderConfigValue(): String?
}