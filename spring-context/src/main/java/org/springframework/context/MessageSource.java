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

package org.springframework.context;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * MessageSource接口用于解决消息，支持参数化和国际化消息。
 * spring提供了两种开箱即用的实现，基于标准java.util.ResourceBundle的实现ResourceBundleMessageSource
 * 和在虚拟机没有重启的情况下可以重新加载消息定义的ReloadableResourceBundleMessageSource。
 *
 * Strategy interface for resolving messages, with support for the parameterization
 * and internationalization of such messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource},
 * built on top of the standard {@link java.util.ResourceBundle}
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource},
 * being able to reload message definitions without restarting the VM
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface MessageSource {

	/**
	 * 尝试解决消息，如果没有消息发现，则返回默认的消息
	 * @param code
	 * 需要寻找的消息代码，比如'calculator.noRateSet'。使用此类，鼓励使用相关类型全限定的类型名
	 * 作为base的name，这样可以避免冲突，确保最大的清晰。
	 * @param args
	 * 参数值，用于填充消息中的占位符，比如 "{0}", "{1,date}", "{2,time}"，没有则为null。
	 * @param defaultMessage
	 * 如果寻找失败，则返回默认的消息
	 * @param locale the locale in which to do the lookup
	 * 本地化参数
	 *
	 * Try to resolve the message. Return default message if no message was found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * @param defaultMessage a default message to return if the lookup fails
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	@Nullable
	String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale);

	/**
	 * 与上面方法不同的是，当消息不存在时，抛出NoSuchMessageException异常
	 *
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * 尝试解决MessageSourceResolvable中消息及消息中的参数。
	 *
	 * Try to resolve the message using all the attributes contained within the
	 * {@code MessageSourceResolvable} argument that was passed in.
	 * <p>NOTE: We must throw a {@code NoSuchMessageException} on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * {@code defaultMessage} property of the resolvable is {@code null} or not.
	 * @param resolvable the value object storing attributes required to resolve a message
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see java.text.MessageFormat
	 */
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
