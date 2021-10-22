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
package com.epam.drill.agent.instrument.util

import com.alibaba.ttl.threadpool.agent.internal.javassist.*
import java.io.*
import java.security.*


internal inline fun addAndTransform(
    classBytes: ByteArray,
    loader: Any?,
    protectionDomain: Any?,
    function: (CtClass, ClassPool, ClassLoader?, ProtectionDomain?) -> ByteArray?, //TODO noinline ???
): ByteArray? {
    val classPool = ClassPool(true)
    if (loader == null) {
        classPool.appendClassPath(LoaderClassPath(ClassLoader.getSystemClassLoader()))
    } else {
        classPool.appendClassPath(LoaderClassPath(loader as? ClassLoader))
    }

    val clazz = classPool.makeClass(ByteArrayInputStream(classBytes), false)
    clazz.defrost()

    return function(clazz, classPool, loader as? ClassLoader, protectionDomain as? ProtectionDomain)
}
