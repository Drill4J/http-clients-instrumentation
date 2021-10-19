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
package com.epam.drill.agent.autotest.http.java

import com.epam.drill.agent.autotest.impl.*
import com.epam.drill.agent.strategy.http.java.*
import com.epam.drill.agent.util.*
import com.epam.drill.kni.*

@Kni
actual object AutotestAgentJavaHttpUrlConnectionTransformer : JavaHttpUrlConnection() {

    actual override fun transform(
        className: String,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?
    ): ByteArray? = getCtClass(classFileBuffer, loader as? ClassLoader) { _, ctClass ->
        val sendRequestHeader = runCatching { ctClass.constructors }.onFailure {
            logger.error(it) { "Error while instrumenting the class ${ctClass.name}" }
        }
        sendRequestHeader.getOrNull()?.forEach {
            it.insertAfter(
                """
                        if ($IF_CONDITION) {
                            try {
                                ${Log::class.java.name}.INSTANCE.${Log::injectHeaderLog.name}($TEST_NAME_VALUE_CALC_LINE, $SESSION_ID_VALUE_CALC_LINE);
                                this.setRequestProperty($TEST_NAME_CALC_LINE);
                                this.setRequestProperty($SESSION_ID_CALC_LINE);
                            } catch (Exception e) {};
                        }
                    """
            )
        }
        return ctClass.toBytecode()

    }

}
