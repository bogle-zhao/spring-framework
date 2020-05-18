/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * https://blog.csdn.net/andy_zhang2007/article/details/85474729
 *
 * 加载资源的策略接口(比如：类路径或文件系统资源)。
 * 一个org.springframework.context。ApplicationContext需要提供这个功能，
 * 加上扩展的org.springframework.core.io.support.ResourcePatternResolver支持。
 * Strategy interface for loading resources (e.. class path or file system
 * resources). An {@link org.springframework.context.ApplicationContext}
 * is required to provide this functionality, plus extended
 * {@link org.springframework.core.io.support.ResourcePatternResolver} support.
 *
 * DefaultResourceLoader是一个独立的实现，可以在ApplicationContext之外使用，也可以由ResourceEditor使用。
 * <p>{@link DefaultResourceLoader} is a standalone implementation that is
 * usable outside an ApplicationContext, also used by {@link ResourceEditor}.
 *
 * 当在ApplicationContext中运行时，可以使用特定上下文的资源加载策略从字符串填充资源和资源数组类型的Bean属性。
 * <p>Bean properties of type Resource and Resource array can be populated
 * from Strings when running in an ApplicationContext, using the particular
 * context's resource loading strategy.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
public interface ResourceLoader {

	// 从 classpath 上加载资源的伪URL前缀: classpath:
	/** Pseudo URL prefix for loading from the class path: "classpath:" */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	/**
	 * 给定资源路径，返回相应的资源Resource对象。所返回的Resource对象，
	 * 也可以叫做资源句柄，必须是可重用的资源描述符，允许多次在其上
	 * 调用方法Resource#getInputStream()。
	 * 另外 :
	 * 1. 必须支持全路径URL : 比如 "file:C:/test.dat".
	 * 2. 必须支持classpath伪URL : 比如 "classpath:test.dat".
	 * 3. 应该支持相对文件路径：比如 "WEB-INF/test.dat". (实现相关)
	 * Return a Resource handle for the specified resource location.
	 * <p>The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p><ul>
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
	 * (This will be implementation-specific, typically provided by an
	 * ApplicationContext implementation.)
	 * </ul>
	 *
	 * 注意 ： 所返回的资源句柄并不意味着对应的资源是已经存在的，所传入的参数
	 * 只是一个资源路径，并不代表相应的资源已经存在；使用者必须调用方法Resource#exists
	 * 来判断对应资源的存在性。
	 * <p>Note that a Resource handle does not imply an existing resource;
	 * you need to invoke {@link Resource#exists} to check for existence.
	 * @param location the resource location	资源路径
	 * @return a corresponding Resource handle (never {@code null})	相应的资源句柄，总是不为null(哪怕对应的资源不存在)
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	Resource getResource(String location);

	/**
	 * 暴露当前ResourceLoader所使用的ClassLoader给外部。
	 * Expose the ClassLoader used by this ResourceLoader.
	 * <p>Clients which need to access the ClassLoader directly can do so
	 * in a uniform manner with the ResourceLoader, rather than relying
	 * on the thread context ClassLoader.
	 * @return the ClassLoader
	 * (only {@code null} if even the system ClassLoader isn't accessible)
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable
	ClassLoader getClassLoader();

}
