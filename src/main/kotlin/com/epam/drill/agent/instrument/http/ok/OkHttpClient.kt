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
package com.epam.drill.agent.instrument.http.ok

import ITransformer
import com.alibaba.ttl.threadpool.agent.internal.javassist.*
import com.epam.drill.agent.instrument.*
import com.epam.drill.agent.instrument.util.*
import com.epam.drill.logger.*
import java.security.*

object OkHttpClient : ITransformer {

    private val logger = Logging.logger { OkHttpClient::class.qualifiedName }


    override fun transform(
        className: String,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?,
    ): ByteArray? = addAndTransform(classFileBuffer, loader, protectionDomain, OkHttpClient::transform)


    override fun transform(
        ctClass: CtClass,
        pool: ClassPool,
        classLoader: ClassLoader?,
        protectionDomain: ProtectionDomain?,
    ): ByteArray? {
        kotlin.runCatching {
            ctClass.getDeclaredMethod("writeRequestHeaders").insertBefore(
                """
                if (${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::isSendCondition.name}()) {
                    okhttp3.Request.Builder builder = $1.newBuilder();
                    java.util.Iterator iterator = ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::getHeaders.name}().entrySet().iterator();             
                    while (iterator.hasNext()) {
                        java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                        builder.addHeader((String) entry.getKey(), (String) entry.getValue());
                    }
                    $1 = builder.build();
                }
            """.trimIndent()
            )
            ctClass.getDeclaredMethod("openResponseBody").insertBefore(
                """
                    java.util.Map allHeaders = new java.util.HashMap();
                    java.util.Iterator iterator = $1.headers().names().iterator();
                    while (iterator.hasNext()) { 
                        String key = (String) iterator.next();
                        String value = $1.headers().get(key);
                        allHeaders.put(key, value);
                    }
                    ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::storeHeaders.name}(allHeaders);
                """.trimIndent()
            )
        }.onFailure {
            logger.error(it) { "Error while instrumenting the class ${ctClass.name}" }
        }

        return ctClass.toBytecode()
    }


}