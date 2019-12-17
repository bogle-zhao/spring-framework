/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 可以被作为分层结构中的一部分的bean工厂实现
 *
 * Sub-interface implemented by bean factories that can be part
 * of a hierarchy.
 * 那些允许以配置的方式设置其父工厂的bean工厂对应的方法setParentBeanFactory可以在接口setParentBeanFactory
 * 中找到
 * <p>The corresponding {@code setParentBeanFactory} method for bean
 * factories that allow setting the parent in a configurable
 * fashion can be found in the ConfigurableBeanFactory interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 07.07.2003
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
 */
//继承 BeanFactory，也就是在 BeanFactory 定义的功能的基础上增加了对 parentFactory 的支持
public interface HierarchicalBeanFactory extends BeanFactory {

	/**
	 * 返回其父工厂，如果没有返回Null
	 * Return the parent bean factory, or {@code null} if there is none.
	 */
	@Nullable
	BeanFactory getParentBeanFactory();

	/**
	 * 返回当前bean工厂上下文是否存在给定bean名字的bean，忽略定义在其继承层次中的工厂上下文。
	 * Return whether the local bean factory contains a bean of the given name,
	 * ignoring beans defined in ancestor contexts.
	 * containsBean方法与此方法是二选一的，都忽略其继承层次中的bean定义，只在当前层次中查找
	 * <p>This is an alternative to {@code containsBean}, ignoring a bean
	 * of the given name from an ancestor bean factory.
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is defined in the local factory
	 * @see BeanFactory#containsBean
	 */
	boolean containsLocalBean(String name);

}
