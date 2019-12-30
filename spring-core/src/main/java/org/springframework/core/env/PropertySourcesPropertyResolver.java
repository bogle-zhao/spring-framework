/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

import org.springframework.lang.Nullable;

/**
 * {@link PropertyResolver} implementation that resolves property values against
 * an underlying set of {@link PropertySources}.
 *
 * 一个PropertyResolver实现类，用于解析一个PropertySources对象中的属性源集合中的属性。
 *
 * 这里PropertySourcesPropertyResolver继承自基类AbstractPropertyResolver，
 * AbstractPropertyResolver提供了很多方法实现，不过这里不做过多解析，而仅仅关注
 * 如何获取一个属性的值的逻辑。
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertySource
 * @see PropertySources
 * @see AbstractEnvironment
 */
public class PropertySourcesPropertyResolver extends AbstractPropertyResolver {

	// PropertySources形式存在的属性源集合，该工具的工作对象
	@Nullable
	private final PropertySources propertySources;


	/**
	 * Create a new resolver against the given property sources.
	 * @param propertySources the set of {@link PropertySource} objects to use
	 */
	public PropertySourcesPropertyResolver(@Nullable PropertySources propertySources) {
		this.propertySources = propertySources;
	}


	// 查看是否包含某个指定名称的属性，判断方法：
	// 1. 底层任何一个属性源包含该属性的话就认为是包含；
	// 2. 否则认为是不包含。
	@Override
	public boolean containsProperty(String key) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (propertySource.containsProperty(key)) {
					return true;
				}
			}
		}
		return false;
	}

	// 获取某个指定名称的属性的值，如果该属性不被包含的话，返回null
	// 不管该属性的值是什么类型，将它转换成字符串类型
	@Override
	@Nullable
	public String getProperty(String key) {
		return getProperty(key, String.class, true);
	}

	// 获取某个指定名称的属性的值，并将其转换成指定的类型，如果该属性不被包含的话，返回null
	@Override
	@Nullable
	public <T> T getProperty(String key, Class<T> targetValueType) {
		return getProperty(key, targetValueType, true);
	}

	// 获取某个指定名称的属性的值，如果该属性不被包含的话，返回null
	// 不管该属性的值是什么类型，将它转换成字符串类型
	@Override
	@Nullable
	protected String getPropertyAsRawString(String key) {
		return getProperty(key, String.class, false);
	}

	//参数说明，key是传进来的占位符，targetValueType指的是目标类型，这里肯定是String.class, resolveNestedPlaceholders表示是否要对嵌套的占位符进行解析，这里传的是false，不需要。
	@Nullable
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (logger.isTraceEnabled()) {
					logger.trace("Searching for key '" + key + "' in PropertySource '" +
							propertySource.getName() + "'");
				}
				//从系统属性中寻找值
				Object value = propertySource.getProperty(key);
				if (value != null) {
					//如果允许解析嵌入的${}，并且是String类型的就继续解析
					if (resolveNestedPlaceholders && value instanceof String) {
						value = resolveNestedPlaceholders((String) value);
					}
					logKeyFound(key, propertySource, value);
					return convertValueIfNecessary(value, targetValueType);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Could not find key '" + key + "' in any property source");
		}
		return null;
	}

	/**
	 * Log the given key as found in the given {@link PropertySource}, resulting in
	 * the given value.
	 * <p>The default implementation writes a debug log message with key and source.
	 * As of 4.3.3, this does not log the value anymore in order to avoid accidental
	 * logging of sensitive settings. Subclasses may override this method to change
	 * the log level and/or log message, including the property's value if desired.
	 * @param key the key found
	 * @param propertySource the {@code PropertySource} that the key has been found in
	 * @param value the corresponding value
	 * @since 4.3.1
	 */
	protected void logKeyFound(String key, PropertySource<?> propertySource, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Found key '" + key + "' in PropertySource '" + propertySource.getName() +
					"' with value of type " + value.getClass().getSimpleName());
		}
	}

}
