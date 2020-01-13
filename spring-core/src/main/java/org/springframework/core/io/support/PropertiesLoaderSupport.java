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

package org.springframework.core.io.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * Spring PropertiesLoaderSupport是一个抽象基类，它抽象了从不同渠道加载属性的通用逻辑，
 * 以及这些属性应用优先级上的一些考虑。它所提供的这些功能主要供实现子类使用。Spring框架中，
 * PropertiesLoaderSupport的实现子类有PropertiesFactoryBean,PropertyResourceConfigurer等。
 *
 * 首先，它将属性分成两类：本地属性(也叫缺省属性)和外来属性。这里本地属性指的是直接以Properties对象形式设置进来的属性。
 * 外来属性指的是通过外部资源形式设置进来需要加载的那些属性。
 *
 * 然后，对于本地属性和外来属性之间的的使用优先级，PropertiesLoaderSupport通过属性localOverride来标识。
 * 如果localOverride为false,表示外部属性优先级高，这也是缺省设置。如果localOverride为true,表示本地属性优先级高。
 *
 * 另外，PropertiesLoaderSupport还有一个属性fileEncoding用来表示从属性文件加载属性时使用的字符集。
 *
 * 总结：
 * PropertiesLoaderSupport所实现的功能并不多，主要是设置要使用的本地属性和外部属性文件资源路径，
 * 最终通过mergeProperties方法将这些属性合并成一个Properties对象，本地属性和外部属性之间的优先级关系由属性localOverride决定。
 * ————————————————
 * 原文链接：https://blog.csdn.net/andy_zhang2007/article/details/86749301
 *
 * Base class for JavaBean-style components that need to load properties
 * from one or more resources. Supports local properties as well, with
 * configurable overriding.
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public abstract class PropertiesLoaderSupport {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	// 本地属性，通过设置Properties对象直接设置进来的属性
	@Nullable
	protected Properties[] localProperties;

	// 本地属性和外来属性之间的优先级，或者叫做覆盖关系
	// false 表示 外来属性优先级高于本地属性  (缺省值)
	// true 表示 本地属性优先级高于外来属性
	protected boolean localOverride = false;

	// 外来属性对应资源，通过设置外部资源位置设置进来需要加载的属性
	@Nullable
	private Resource[] locations;

	// 读取外来属性时遇到不存在的资源路径应该怎么办 ?
	// false : 输出一个日志，然后继续执行其他逻辑 (缺省值)
	// true : 抛出异常
	private boolean ignoreResourceNotFound = false;

	// 加载外来属性资源文件时使用的字符集
	@Nullable
	private String fileEncoding;

	// 外来属性加载工具
	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();


	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties
	 * loaded from files.
	 */
	public void setProperties(Properties properties) {
		this.localProperties = new Properties[] {properties};
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions,
	 * allowing for merging multiple properties sets into one.
	 */
	public void setPropertiesArray(Properties... propertiesArray) {
		this.localProperties = propertiesArray;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 * <p>Can point to a classic properties file or to an XML file
	 * that follows JDK 1.5's properties XML format.
	 */
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}

	/**
	 * Set locations of properties files to be loaded.
	 * <p>Can point to classic properties files or to XML files
	 * that follow JDK 1.5's properties XML format.
	 * <p>Note: Properties defined in later files will override
	 * properties defined earlier files, in case of overlapping keys.
	 * Hence, make sure that the most specific files are the last
	 * ones in the given list of locations.
	 */
	public void setLocations(Resource... locations) {
		this.locations = locations;
	}

	/**
	 * Set whether local properties override properties from files.
	 * <p>Default is "false": Properties from files override local defaults.
	 * Can be switched to "true" to let local properties override defaults
	 * from files.
	 */
	public void setLocalOverride(boolean localOverride) {
		this.localOverride = localOverride;
	}

	/**
	 * Set if failure to find the property resource should be ignored.
	 * <p>"true" is appropriate if the properties file is completely optional.
	 * Default is "false".
	 */
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}

	/**
	 * Set the encoding to use for parsing properties files.
	 * <p>Default is none, using the {@code java.util.Properties}
	 * default encoding.
	 * <p>Only applies to classic properties files, not to XML files.
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * The default is DefaultPropertiesPersister.
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(@Nullable PropertiesPersister propertiesPersister) {
		this.propertiesPersister =
				(propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister());
	}


	/**
	 * Return a merged Properties instance containing both the
	 * loaded properties and properties set on this FactoryBean.
	 */
//	合并本地属性和外来属性然后返回一个Properties对象，这里面考虑了属性 localOverride 的应用。
	protected Properties mergeProperties() throws IOException {
		Properties result = new Properties();

		if (this.localOverride) {
			// Load properties from file upfront, to let local properties override.	从文件预先加载属性，以使本地属性被覆盖。
			// localOverride == true, 先加载外来属性到结果对象
			loadProperties(result);
		}

		if (this.localProperties != null) {
			// 将本地属性合并到结果对象
			for (Properties localProp : this.localProperties) {
				CollectionUtils.mergePropertiesIntoMap(localProp, result);
			}
		}

		if (!this.localOverride) {
			// Load properties from file afterwards, to let those properties override.	之后从文件加载属性，以使这些属性被覆盖。
			// localOverride == false, 后加载外来属性到结果对象
			loadProperties(result);
		}

		return result;
	}

	/**
	 * 将属性加载到给定实例中。
	 * Load properties into the given instance.
	 * @param props the Properties instance to load into
	 * @throws IOException in case of I/O errors
	 * @see #setLocations
	 */
//	外部属性的加载方法。
	protected void loadProperties(Properties props) throws IOException {
		if (this.locations != null) {
			// 读取每一个属性文件资源
			for (Resource location : this.locations) {
				if (logger.isDebugEnabled()) {
					logger.debug("Loading properties file from " + location);
				}
				try {
					// 使用指定的字符集fileEncoding从外部资源路径location读取属性到props,使用的属性读取工具
					// 是 propertiesPersister
					//xml 格式的propertieswenj格式参考applicationProperties.xml
					PropertiesLoaderUtils.fillProperties(
							props, new EncodedResource(location, this.fileEncoding), this.propertiesPersister);
				}
				catch (FileNotFoundException | UnknownHostException ex) {
					// 出现异常时，如果ignoreResourceNotFound==true,则仅仅记录日志，继续读取下一个
					// 资源文件，否则直接抛出该异常
					if (this.ignoreResourceNotFound) {
						if (logger.isInfoEnabled()) {
							logger.info("Properties resource not found: " + ex.getMessage());
						}
					}
					else {
						throw ex;
					}
				}
			}
		}
	}

}
