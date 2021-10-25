package com.epam.drill.agent.instrument.http.apache

import ITransformer
import com.alibaba.ttl.threadpool.agent.internal.javassist.*
import com.epam.drill.agent.instrument.*
import com.epam.drill.agent.instrument.util.*
import com.epam.drill.logger.*
import org.objectweb.asm.*
import java.security.*

object ApacheClient : ITransformer {

    private val logger = Logging.logger { ApacheClient::class.qualifiedName }

    override fun permit(classReader: ClassReader): Boolean {
        return classReader.interfaces.any { "org/apache/http/HttpClientConnection" == it }
    }

    override fun transform(
        className: String,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?,
    ): ByteArray? = addAndTransform(classFileBuffer, loader, protectionDomain, ApacheClient::transform)


    override fun transform(
        ctClass: CtClass,
        pool: ClassPool,
        classLoader: ClassLoader?,
        protectionDomain: ProtectionDomain?,
    ): ByteArray? {
        runCatching {
            ctClass.getDeclaredMethod("sendRequestHeader").insertBefore(
                """
                    if (${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::isSendCondition.name}()) { 
                        try {
                            java.util.Map headers = ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::getHeaders.name}();
                            java.util.Iterator iterator = headers.entrySet().iterator();             
                            while (iterator.hasNext()) {
                                java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                                $1.setHeader((String) entry.getKey(), (String) entry.getValue());
                            }
                            ${Log::class.java.name}.INSTANCE.${Log::injectHeaderLog.name}(headers);
                        } catch (Exception e) {};
                    }
                """.trimIndent()
            )
            ctClass.getDeclaredMethod("receiveResponseEntity").insertBefore(
                """
                    if (${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::isResponseCallbackSet.name}()) {
                        java.util.Map allHeaders = new java.util.HashMap();
                        java.util.Iterator iterator = $1.headerIterator();
                        while (iterator.hasNext()) {
                            org.apache.http.Header header = (org.apache.http.Header) iterator.next();
                            allHeaders.put(header.getName(), header.getValue());
                        }
                        ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::storeHeaders.name}(allHeaders);
                    }
            """.trimIndent())
        }
        return ctClass.toBytecode()
    }
}
