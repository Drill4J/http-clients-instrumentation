package com.epam.drill.agent.instrument.http.java

import ITransformer
import com.alibaba.ttl.threadpool.agent.internal.javassist.*
import com.epam.drill.agent.instrument.*
import com.epam.drill.agent.instrument.util.*
import com.epam.drill.logger.*
import java.security.*

object JavaHttpUrlConnection : ITransformer {

    private val logger = Logging.logger { JavaHttpUrlConnection::class.qualifiedName }

    override fun transform(
        className: String,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?,
    ): ByteArray? = addAndTransform(classFileBuffer, loader, protectionDomain, JavaHttpUrlConnection::transform)

    override fun transform(
        ctClass: CtClass,
        pool: ClassPool,
        classLoader: ClassLoader?,
        protectionDomain: ProtectionDomain?,
    ): ByteArray? {
        runCatching {
            ctClass.constructors.forEach {
                it.insertAfter(
                    """
                    if (${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::isSendCondition.name}()) {
                        try {
                            java.util.Iterator iterator = ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::getHeaders.name}().entrySet().iterator();             
                            while (iterator.hasNext()) {
                                java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                                this.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                            }
                        } catch (Exception e) {};
                    }
                    """
                )
            }
            ctClass.getDeclaredMethod("getContent").insertAfter("""
                    java.util.Map allHeaders = new java.util.HashMap();
                    java.util.Iterator iterator = getHeaderFields().keySet().iterator();
                    while (iterator.hasNext()) { 
                        String key = (String) iterator.next();
                        String value = getHeaderField(key);
                        allHeaders.put(key, value);
                    }
                    ${ClientsCallback::class.qualifiedName}.INSTANCE.${ClientsCallback::storeHeaders.name}(allHeaders);
            """.trimIndent())

        }.onFailure {
            logger.error(it) { "Error while instrumenting the class ${ctClass.name}" }
        }
        return ctClass.toBytecode()
    }
}
