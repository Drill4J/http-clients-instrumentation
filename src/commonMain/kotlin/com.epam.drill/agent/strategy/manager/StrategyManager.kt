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
package com.epam.drill.agent.strategy.manager

import com.epam.drill.agent.strategy.*
import com.epam.drill.agent.strategy.Marker
import com.epam.drill.logger.*
import com.epam.drill.logger.api.*
import org.objectweb.asm.*
import kotlin.jvm.*

abstract class StrategyManager {

    companion object {
        @JvmStatic
        protected val logger = Logging.logger { StrategyManager::class.simpleName }

        @JvmStatic
        protected val systemStrategies: MutableSet<IStrategy> = mutableSetOf()

        @JvmStatic
        val allStrategies: MutableMap<String, MutableSet<IStrategy>> = mutableMapOf()

        @JvmStatic
        protected val strategies: MutableSet<IStrategy> = mutableSetOf()

        @JvmStatic
        protected val implementations: MutableSet<Marker> = mutableSetOf()

        internal inline fun <reified T : Marker> getImplementation(
        ): T? = (implementations.firstOrNull { it is T } as? T).also {
            it ?: logger.warn { "Implementation for '${T::class.simpleName}' doesn't set, specify it in '${this::class.simpleName}'" }
        }
    }

    abstract fun intiStrategies(config: Map<String, String> = emptyMap(), implementationSet: Set<Marker> = emptySet())

    fun process(
        ctClass: ClassReader,
        classFileBuffer: ByteArray,
        loader: Any?,
        protectionDomain: Any?,
    ): ByteArray? {
        for (strategy in strategies) {
            if (strategy.permit(ctClass))
                return strategy.transform(ctClass.className!!, classFileBuffer, loader, protectionDomain)
        }
        return null
    }

}

