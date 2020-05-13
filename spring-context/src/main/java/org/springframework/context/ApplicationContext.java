/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * 为应用程序提供配置的中央接口。
 * 在应用程序运行时，这是只读的，但如果实现支持这一点（重新读取配置），则可以重新加载配置信息。
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * 一个ApplicationContext提供如下功能
 * <p>An ApplicationContext provides:
 * <ul>
 *     用于访问应用程序组件的Bean工厂方法。继承自{@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 *
 * 以通用方式加载文件资源的能力。继承自{@link org.springframework.core.io.ResourceLoader}接口。
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 *
 * 发布事件注册侦听器的能力。继承自{@link ApplicationEventPublisher}接口
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 *
 * 解析消息的能力，支持国际化。继承自{@link MessageSource}接口
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 *
 * 通过上下文继承关系。子类的context将先初始化。例如，这意味着单个父上下文可以被整个web应用程序使用，而每个servlet都有独立于任何其他servlet的子上下文。
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * 除了标准的{@link org.springframework.beans.factory.BeanFactory}生命周期功能以外，ApplicationContext
 * 还检测到并调用{@link ApplicationContextAware}以及{@link ResourceLoaderAware},{@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * 返回此应用程序上下文的唯一id。
	 * Return the unique id of this application context.
	 * @return the unique id of the context, or {@code null} if none
	 */
	@Nullable
	String getId();

	/**
	 * 返回此上下文所属的已部署应用程序的名称。
	 * Return a name for the deployed application that this context belongs to.
	 * @return a name for the deployed application, or the empty String by default
	 */
	String getApplicationName();

	/**
	 * 为这个上下文返回一个友好的名称。
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * 返回第一次加载该上下文时的时间戳。
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * 返回父上下文，如果没有父，则返回{@code null}，这是上下文层次结构的根。
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or {@code null} if there is no parent
	 */
	@Nullable
	ApplicationContext getParent();

	/**
	 *
	 * 为这个上下文公开AutowireCapableBeanFactory功能。
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 *
	 * 应用程序代码通常不使用这种方法，除非用于初始化存在于应用程序上下文之外的bean实例，
	 * 并将Spring bean的生命周期(全部或部分)应用于这些实例。
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside of the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 *
	 * 另外，ConfigurableApplicationContext接口公开的内部BeanFactory也提供了对AutowireCapableBeanFactory接口的访问
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too.
	 *
	 * 本方法主要是作为ApplicationContext接口上的一个方便的、特定的工具。
	 * The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 *
	 * 从4.2开始，该方法将在应用程序上下文关闭后一致抛出IllegalStateException
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b>
	 *
	 * 在当前的Spring框架版本中，只有可刷新的应用程序上下文才会这样做;从4.2开始，所有应用程序上下文实现都必须遵守。
	 *
	 * In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 *
	 * @return the AutowireCapableBeanFactory for this context
	 *
	 * 如果上下文不支持AutowireCapableBeanFactory接口，或者还没有包含支持autowire的bean工厂(例如，如果从未调用refresh())，或者上下文已经关闭
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (e.g. if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
