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
package com.epam.drill.agent.autotest.http.ok

import com.epam.drill.agent.autotest.impl.*
import com.epam.drill.agent.strategy.http.ok.*
import com.epam.drill.agent.util.*
import com.epam.drill.kni.*

@Kni
actual object AutotestAgentOkHttpClientTransformer : OkHttpClient() {

    actual override fun transform(
        className: String,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?
    ): ByteArray? = getCtClass(classFileBuffer, loader as? ClassLoader) { _, ctClass ->
        val sendRequestHeader = kotlin.runCatching {
            ctClass.getDeclaredMethod("writeRequestHeaders")
        }.onFailure {
            logger.error(it) { "Error while instrumenting the class ${ctClass.name}" }
        }
        sendRequestHeader.getOrNull()?.insertBefore(
            """
                if ($IF_CONDITION) {
                ${Log::class.java.name}.INSTANCE.${Log::injectHeaderLog.name}($TEST_NAME_VALUE_CALC_LINE,$SESSION_ID_VALUE_CALC_LINE);
                    $1 = $1.newBuilder()
                            .addHeader($TEST_NAME_CALC_LINE)
                            .addHeader($SESSION_ID_CALC_LINE)
                            .build();
                }
            """.trimIndent()
        )
        return ctClass.toBytecode()
    }

}
