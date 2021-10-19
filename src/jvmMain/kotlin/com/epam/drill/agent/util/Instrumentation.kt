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
package com.epam.drill.agent.util

import com.alibaba.ttl.threadpool.agent.internal.javassist.*
import com.epam.drill.logger.*
import java.io.*

inline fun getCtClass(
    classBytes: ByteArray,
    loader: ClassLoader?,
    function: (ClassPool, CtClass) -> ByteArray?, //TODO noinline ???
): ByteArray? {
    val classPool = ClassPool(true)
    if (loader == null) {
        classPool.appendClassPath(LoaderClassPath(ClassLoader.getSystemClassLoader()))
    } else {
        classPool.appendClassPath(LoaderClassPath(loader))
    }

    val clazz = classPool.makeClass(ByteArrayInputStream(classBytes), false)
    clazz.defrost()

    return function(classPool, clazz)
}

inline fun CtMethod.wrapCatching(
    insert: CtMethod.(String) -> Unit,
    code: String,
) {
    runCatching {
        insert(
            """
            try {
                $code
            } catch (Throwable e) {
                ${InstrumentationErrorLogger::class.java.name}.INSTANCE.${InstrumentationErrorLogger::error.name}(e, "Error in the injected code. Method name: $name.");
            }
        """.trimIndent()
        )
    }.onFailure { InstrumentationErrorLogger.warn(it, "Can't insert code. Method name: $name") }
}

object InstrumentationErrorLogger {
    private val logger = Logging.logger("instrumentation")

    fun error(exception: Throwable, message: String) {
        logger.error(exception) { message }
    }

    fun warn(exception: Throwable, message: String) {
        logger.warn(exception) { message }
    }
}

