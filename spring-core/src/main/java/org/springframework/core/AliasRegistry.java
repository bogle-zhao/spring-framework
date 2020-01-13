/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

/**
 * 顾名思义，AliasRegistry “别名注册表"是管理"别名"的"注册表”。Spring使用此概念抽象容器对bean别名的管理功能。
 * 此抽象对应的接口是AliasRegistry,定义在包org.springframework.core中。
 * 此接口是org.springframework.beans.factory.support.BeanDefinitionRegistry的super接口，
 * 用于让BeanDefinitionRegistry"bean定义注册表"具有bean名称别名管理功能。
 *
 * SimpleAliasRegistry是Spring对接口AliasRegistry的简单实现,
 * Spring的bean容器实现DefaultListableBeanFactory也继承自SimpleAliasRegistry从而具备bean名称别名管理能力。
 * SimpleAliasRegistry内部使用了一个数据结构ConcurrentHashMap<String, String>来管理bean名称和别名的映射:key表示别名，value表示bean名称。
 * ————————————————
 * 原文链接：https://blog.csdn.net/andy_zhang2007/article/details/86829658
 *
 * Common interface for managing aliases. Serves as super-interface for
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
//定义对 alias 的简单增删改等操作
public interface AliasRegistry {

	/**
	 * 为一个给定的名称name(可以是bean名称，也可能是一个bean名称别名),注册它的一个别名 alias
	 * Given a name, register an alias for it.
	 * @param name the canonical name
	 * @param alias the alias to be registered
	 * @throws IllegalStateException if the alias is already in use
	 * and may not be overridden
	 */
	void registerAlias(String name, String alias);

	/**
	 * 删除指定的别名 alias
	 * Remove the specified alias from this registry.
	 * @param alias the alias to remove
	 * @throws IllegalStateException if no such alias was found
	 */
	void removeAlias(String alias);

	/**
	 * 确定是否将此给定名称定义为别名
	 * Determine whether this given name is defines as an alias
	 * (as opposed to the name of an actually registered component).
	 * @param name the name to check
	 * @return whether the given name is an alias
	 */
	boolean isAlias(String name);

	/**
	 * 获取指定名称name的所有别名
	 * Return the aliases for the given name, if defined.
	 * @param name the name to check for aliases
	 * @return the aliases, or an empty array if none
	 */
	String[] getAliases(String name);

}
