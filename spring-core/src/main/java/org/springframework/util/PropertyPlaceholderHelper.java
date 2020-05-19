/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * https://blog.csdn.net/weixin_39471249/article/details/79473772
 *
 * 占位符解析器
 * <br/>
 * 用于处理具有占位符值的字符串的实用程序类。占位符的形式如${name}
 * 使用PropertyPlaceholderHelper，这些占位符可以替换用户提供的值
 * <br/>
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * {@code ${name}}. Using {@code PropertyPlaceholderHelper} these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}


	private final String placeholderPrefix;

	private final String placeholderSuffix;

	private final String simplePrefix;

	//默认值分割符
	@Nullable
	private final String valueSeparator;
	//忽略不可解析的占位符，如设置为false，碰到不可解析的占位符，抛出异常
	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * 创建一个新的PropertyPlaceholderHelper，它使用提供的前缀和后缀。将忽略无法解析的占位符。
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Unresolvable placeholders are ignored.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true);
	}

	/**
	 * 创建一个新的PropertyPlaceholderHelper，它使用提供的前缀和后缀。
	 *
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any 占位符变量和相关联的默认值(如果有的话)之间的分隔字符
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false}) 指示是否应该忽略不可解析占位符(true)或导致异常(false)
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	/**
	 * Replaces all placeholders of format {@code ${name}} with the corresponding
	 * property from the supplied {@link Properties}.
	 * @param value the value containing the placeholders to be replaced
	 * @param properties the {@code Properties} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		return replacePlaceholders(value, properties::getProperty);
	}

	/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		//替换占位符逻辑
		return parseStringValue(value, placeholderResolver, new HashSet<>());
	}

	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {

		StringBuilder result = new StringBuilder(value);

		//检查这个字符串时候有 ${ 前缀
		int startIndex = value.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			//如果有 ${ 前缀，再检查是否有 } 后缀
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex != -1) {
				//拿到占位符，如classpath:spring${key}.xml,这个占位符是key
				String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				//将当前的占位符存到set集合中，如果set集合有了，就会添加失败
				//就会报错，循环引用错误，比如${a},这个a的值依然是${a}
				//这样就陷入了无限解析了，根本停不下来
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// Recursive invocation, parsing placeholders contained in the placeholder key.
				//对占位符进行解析，如：${${a}},所以要继续解析
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				// Now obtain the value for the fully resolved key...
				//调用这个解析器查找占位符对应的值，这个方法的代码在下面11步给出
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);
				if (propVal == null && this.valueSeparator != null) {
					//如果为null，那么查找这个propVal是否为：分割的字符串
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						//如果propVal为key:Context,那么这个值应为key
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						//如果propVal为key:Context,那么就是Context
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						//跟上面的一样去系统属性中查找
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						//如果为空，那么就设置为defaultValue，如key:Context
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					//这个值可能也有站位符，继续递归解析
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					//得到了占位符对应的值后替换掉占位符
					result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if (logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					//继续查找是否还有后续的占位符
					startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				//如果propValue为null，那么就说明这个占位符没有值，如果设置为忽略
				//不能解析的占位符，那么继续后续的占位符，否则报错
				else if (this.ignoreUnresolvablePlaceholders) {
					// Proceed with unprocessed value.
					startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in value \"" + value + "\"");
				}
				//解析成功就删除set集合中对应的占位符
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				startIndex = -1;
			}
		}

		return result.toString();
	}

	/**
	 * 解析{{{文字}}} ，为了解析这种情况的，所以才会有withinNestedPlaceholder变量
	 * @param buf
	 * @param startIndex
	 * @return
	 */
	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		//获取前缀后面一个字符的索引
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		//如果前缀后面还有字符的话
		while (index < buf.length()) {
			//判断源字符串在index处是否与后缀匹配
			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				//如果匹配到后缀,但此时前缀数量>后缀,则继续匹配后缀
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				//判断源字符串在index处是否与前缀匹配,若匹配,说明前缀后面还是前缀,则把前缀长度累加到index上,继续循环寻找后缀
				//withinNestedPlaceholder确保前缀和后缀成对出现后
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			else {
				//如果index出既不能和suffix又不能和simplePrefix匹配,则自增,继续循环
				index++;
			}
		}
		return -1;
	}


	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 */
	@FunctionalInterface
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		@Nullable
		String resolvePlaceholder(String placeholderName);
	}

}
