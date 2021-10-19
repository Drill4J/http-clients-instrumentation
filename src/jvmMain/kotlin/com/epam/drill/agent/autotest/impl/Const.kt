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
package com.epam.drill.agent.autotest.impl

import com.epam.drill.agent.autotest.impl.selenium.*

const val TEST_NAME_HEADER = "drill-test-name"
const val SESSION_ID_HEADER = "drill-session-id"

val TEST_NAME_VALUE_CALC_LINE = "${ThreadStorage::class.qualifiedName}.INSTANCE.${ThreadStorage::getTestName.name}()"
val TEST_NAME_CALC_LINE = "\"$TEST_NAME_HEADER\", $TEST_NAME_VALUE_CALC_LINE"
val SESSION_ID_VALUE_CALC_LINE = "${ThreadStorage::class.qualifiedName}.INSTANCE.${ThreadStorage::sessionId.name}()"
val SESSION_ID_CALC_LINE = "\"$SESSION_ID_HEADER\", $SESSION_ID_VALUE_CALC_LINE"
val IF_CONDITION = "$TEST_NAME_VALUE_CALC_LINE != null && $SESSION_ID_VALUE_CALC_LINE != null"
val IS_HEADER_ADDED = "${DevToolsClientThreadStorage::class.java.name}.INSTANCE.${DevToolsClientThreadStorage::isHeadersAdded.name}()"
