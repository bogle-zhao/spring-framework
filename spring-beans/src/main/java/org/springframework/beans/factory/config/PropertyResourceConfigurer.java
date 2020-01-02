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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * Spring PropertyResourceConfigurer是一个抽象基类，继承自PropertiesLoaderSupport，
 * 并实现了接口BeanFactoryPostProcessor。
 * 它抽象了容器BeanFactory后置处理阶段对容器中所有bean定义中的属性进行配置的一般逻辑，
 * 属性配置所使用的属性来源是基类PropertiesLoaderSupport方法所规定的那些属性。
 *
 * PropertyResourceConfigurer有两个实现子类:
 *
 * 		1.PropertyOverrideConfigurer
 * 			用于处理"beanName.property=value"这种风格的属性值覆盖，将属性对象中的属性"推送(push)"到bean定义中
 * 		2.PropertyPlaceholderConfigurer
 * 			用于处理bean定义中"${name}"这样的占位符解析,从属性对象中"拉取(pull)"到bean定义的属性值中
 *
 *
 * 	总结：
 * 	PropertyResourceConfigurer自身主要是抽象了对容器中所有bean定义的属性进行处理的一般逻辑，
 * 	实现在接口BeanFactoryPostProcessor所定义的方法postProcessBeanFactory中，这样容器启动时，
 * 	bean容器的后置处理阶段，所有bean定义的属性都会被当前PropertyResourceConfigurer进行处理，
 * 	处理时所使用的属性来源自当前PropertyResourceConfigurer基类PropertiesLoaderSupport所约定的那些属性，
 * 	至于做什么样的处理，由当前PropertyResourceConfigurer的具体实现子类自己提供实现。
 * ————————————————
 * 原文链接：https://blog.csdn.net/andy_zhang2007/article/details/86756564
 *
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targeted at system
 * administrators that override bean properties configured in the application context.
 *
 * <p>Two concrete implementations are provided in the distribution:
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * <li>{@link PropertyPlaceholderConfigurer} for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@link #convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport
		implements BeanFactoryPostProcessor, PriorityOrdered {

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	/**
	 * Set the order value of this object for sorting purposes.
	 * @see PriorityOrdered
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	/**
	 * 方法postProcessBeanFactory使用到了三个方法 :
	 *
	 * 1.mergeProperties()
	 * 		该方法由基类PropertiesLoaderSupport提供缺省实现,用于合并本地属性和外来属性为一个Properties对象;
	 * 2.convertProperties(mergedProps)
	 * 		该方法由PropertyResourceConfigurer自身提供缺省实现，用于对属性值做必要的转换处理，缺省不做任何处理；
	 * 3.processProperties(beanFactory, mergedProps)
	 * 		该方法由PropertyResourceConfigurer定义为抽象方法，所以需要由实现子类为其提供具体实现。不过其目的很明确，是对容器中每个bean定义中的属性进行处理。但具体处理是什么，就要看实现子类自身的设计目的了。比如实现子类PropertyOverrideConfigurer和实现子类PropertyPlaceholderConfigurer就分别有自己的bean定义属性处理逻辑。
	 *
	 * ————————————————
	 * 原文链接：https://blog.csdn.net/andy_zhang2007/article/details/86756564
	 *
	 * {@linkplain #mergeProperties Merge}, {@linkplain #convertProperties convert} and
	 * {@linkplain #processProperties process} properties against the given bean factory.
	 * @throws BeanInitializationException if any properties cannot be loaded
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			// 合并本地属性和外部指定的属性文件资源中的属性
			Properties mergedProps = mergeProperties();

			// 将属性的值做转换(仅在必要的时候做)
			// Convert the merged properties, if necessary.
			convertProperties(mergedProps);

			// 对容器中的每个bean定义进行处理，也就是替换每个bean定义中的属性中的占位符
			// Let the subclass process the properties.
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}

	/**
	 * Convert the given merged properties, converting property values
	 * if necessary. The result will then be processed.
	 * <p>The default implementation will invoke {@link #convertPropertyValue}
	 * for each property value, replacing the original with the converted value.
	 * @param props the Properties to convert
	 * @see #processProperties
	 */
	// 对指定属性对象中的属性值进行必要的转换
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * Convert the given property from the properties source to the value
	 * which should be applied.
	 * <p>The default implementation calls {@link #convertPropertyValue(String)}.
	 * @param propertyName the name of the property that the value is defined for
	 * @param propertyValue the original value from the properties source
	 * @return the converted value, to be used for processing
	 * @see #convertPropertyValue(String)
	 */
	// 对指定名称的属性的属性值进行必要的转换
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}

	/**
	 * Convert the given property value from the properties source to the value
	 * which should be applied.
	 * <p>The default implementation simply returns the original value.
	 * Can be overridden in subclasses, for example to detect
	 * encrypted values and decrypt them accordingly.
	 * @param originalValue the original value from the properties source
	 * (properties file or local "properties")
	 * @return the converted value, to be used for processing
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 * @see #convertProperty(String, String)
	 */
	// 对属性值的必要转换，这是一个缺省实现，不做任何转换直接返回原值，实现类可以覆盖该方法
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}


	/**
	 * Apply the given Properties to the given BeanFactory.
	 * @param beanFactory the BeanFactory used by the application context
	 * @param props the Properties to apply
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
//	这是一个抽象方法定义，约定对容器beanFactory中所有的bean定义进行属性处理，属性值解析来源是mergedProps,实现子类必须对该方法提供实现。
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
