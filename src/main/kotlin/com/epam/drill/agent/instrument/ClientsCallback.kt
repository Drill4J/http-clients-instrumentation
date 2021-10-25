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
package com.epam.drill.agent.instrument

import kotlinx.atomicfu.*

object ClientsCallback {

    private const val DRILL_HEADER_PREFIX = "drill-"
    private const val SESSION_ID_HEADER = "${DRILL_HEADER_PREFIX}session-id"

    private val _requestCallback = atomic<(() -> Map<String, String>)?>(null)
    private val _responceCallback = atomic<((Map<String, String>) -> Unit)?>(null)

    fun initRequestCallback(callback: () -> Map<String, String>) {
        _requestCallback.value = callback
    }

    fun initResponseCallback(callback: (Map<String, String>) -> Unit) {
        _responceCallback.value = callback
    }


    fun getHeaders(): Map<String, String> = _requestCallback.value?.invoke() ?: emptyMap()

    fun storeHeaders(headers: Map<String, String>) = _responceCallback.value?.invoke(headers)

    fun isRequestCallbackSet() = _requestCallback.value != null

    fun isResponseCallbackSet() = _responceCallback.value != null

    //todo
    fun isSendCondition(): Boolean = getHeaders().run {
        return isRequestCallbackSet() && isNotEmpty() && get(SESSION_ID_HEADER) != null && any {
            it.key.startsWith(DRILL_HEADER_PREFIX) && it.key != SESSION_ID_HEADER
        }
    }
}
