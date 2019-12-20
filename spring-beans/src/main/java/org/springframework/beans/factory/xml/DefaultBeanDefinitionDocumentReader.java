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

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * BeanDefinitionDocumentReader 接口的默认实现，会根据定义"spring-beans" DTD and XSD格式来读取相应的bean信息（Spring的默认bean定义使用XML格式）
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * 所需XML文档的结构、元素和属性名都硬编码在这个类中（当然，如果需要，可以运行转换来生成这种格式）
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	// 真正解析xml的功能是委托给BeanDefinitionParserDelegate完成的
	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";

	public static final String ALIAS_ELEMENT = "alias";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String IMPORT_ELEMENT = "import";

	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private XmlReaderContext readerContext;

	@Nullable
	private BeanDefinitionParserDelegate delegate;


	/**
	 * 这个实现会根据spring-bean定义的XSD或者（DTD ，DTD是历史遗留下来的问题）
	 * This implementation parses bean definitions according to the "spring-beans" XSD
	 * (or DTD, historically).
	 * 打开一个DOM文档，初始化默认的设置在<beans />级别的，接下来解析所包含的bean定义信息
	 * <p>Opens a DOM Document; then initializes the default settings
	 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
	 */
	//根据Spring DTD对bean的定义规则解析Bean定义Document对象
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		//获得XML描述符
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
		//获得Document的根元素
		Element root = doc.getDocumentElement();
		doRegisterBeanDefinitions(root);
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * to pull the source metadata from the supplied {@link Element}.
	 */
	@Nullable
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/**
	 * 在给定的根{@code <beans/>}元素中注册每个bean定义
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	protected void doRegisterBeanDefinitions(Element root) {
		//任何嵌套的<beans>元素都会在这个方法中引起递归。
		// Any nested <beans> elements will cause recursion in this method.
		//为了正确地传播和保留<beans> default-* 属性，请跟踪当前(父)委托，它(这个委托)可能为空。
		//  In order to propagate and preserve <beans> default-* attributes correctly,keep track of the current (parent) delegate, which may be null.
		// 创建新的(子)委托，其中包含一个对父类的引用，以便进行回退，然后最终将this.delegate重置为其原始(父类)引用。
		//  Create the new (child) delegate with a reference to the parent for fallback purposes, then ultimately reset this.delegate back to its original (parent) reference.
		// 这种行为是模拟了一堆委托，但实际上并不需要一个
		// this behavior emulates a stack of delegates without actually necessitating one.
		//具体的解析过程由BeanDefinitionParserDelegate实现
		//BeanDefinitionParserDelegate中定义了Spring Bean定义XML文件的各种格式
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isInfoEnabled()) {
						logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		//在解析Bean定义之前，进行自定义的解析，增强解析过程的可扩展性
		preProcessXml(root);
		//从Document的根元素开始进行Bena定义的Document对象
		parseBeanDefinitions(root, this.delegate);
		//在解析Bean定义之后，进行自定义的解析，增加解析过程的可扩展性
		postProcessXml(root);

		this.delegate = parent;
	}

	//创建BeanDefinitionParserDelegate，用于完成真正的解析过程
	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		//BeanDefinitionParserDelegate初始化Document根元素
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 */
	//使用Spring的bean规则从Document的根元素开始进行Bean定义的Document对象
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		//Bean定义的Document对象使用了Spring默认的Xml命名空间
		if (delegate.isDefaultNamespace(root)) {
			//获取Bean定义的Document对象根元素的所有子节点
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					//Bean定义的Document元素节点使用的是Spring默认的XML命名空间
					if (delegate.isDefaultNamespace(ele)) {
						//使用Spring的Bena规则解析元素节点
						parseDefaultElement(ele, delegate);
					}
					else {
						//没用使用Spring默认的XML命名空间，则使用用户自定义的解析器解析元素节点
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			//Document的根节点没有使用Spring默认的命名空间，则使用用户自定义的解析规则解析Document根节点
			delegate.parseCustomElement(root);
		}
	}

	//使用Spring 的bean规则解析Document元素节点
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {

		//如果元素节点是<import>导入元素，进行导入解析
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		//如果元素节点是<Alias>别名元素，进行别名解析
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		//元素节点既不是导入元素，也不是别名元素，即普通<bean>元素，按照Spring的bean规则解析元素
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		// 元素节点是<beans/>的节点
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 */
	//解析<import>导入元素，从给定的导入路径加载Bean定义资源到SpringIOC容器中
	protected void importBeanDefinitionResource(Element ele) {
		//获取给定的导入元素的location属性
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		//如果导入元素的location属性为空，则没有导入任何资源，直接返回
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		//使用系统变量值解析location属性值
		// Resolve system properties: e.g. "${user.dir}"
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		Set<Resource> actualResources = new LinkedHashSet<>(4);

		//标识给定的导入元素的location是否是绝对路径
		// Discover whether the location is an absolute or relative URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
			//给定的元素不是绝对路径
		}

		//给定的导入元素的location是绝对路径
		// Absolute or relative?
		if (absoluteLocation) {
			try {
				//使用资源读取器加载给定路径的bean定义资源
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			try {
				int importCount;
				//将给定导入元素的location封装为相对路径资源
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				//封装的相对路径资源存在
				if (relativeResource.exists()) {
					//使用资源读入器加载bean定义资源
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				}
				//封装的相对路径资源不存在
				else {
					//获取Spring IOC容器资源读容器的基本路径
					String baseLocation = getReaderContext().getResource().getURL().toString();
					//根据Spring IOC容器资源读容器的基本路径加载给定导入路径的资源
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]",
						ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		//在解析完<import>元素之后，发送容器导入其他资源处理完成事件
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/**
	 * Process the given alias element, registering the alias with the registry.
	 */
	// 解析<Alias>别名元素，为Bean像Spring IOC容器注册别名
	protected void processAliasRegistration(Element ele) {
		//获取<Alias>别名元素中name的属性值
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		//获取<Alias>别名元素汇总alias的属性值
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		//<Alias>别名元素的name属性值为空
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		//<Alias>别名元素的alias属性值为空
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				//像容器的资源读入器注册别名
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			//在解析完<Alias>元素之后，发送容器别名处理完成事件
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * 处理给定的bean元素，解析bean定义并将其注册到注册表中。
	 * Process the given bean element, parsing the bean definition and registering it with the registry.
	 */
	//解析Bean定义资源Document对象的普通元素
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		//BeanDefinitionHolder 里面持有了BeanDefinition的引用
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		//BeanDefinitionHolder是对BeanDefinition的封装，即Bean定义的封装类，对Document对象中<Bean>元素的解析由BeanDefinitionParserDelegate实现，
		// BeanDefinitionHolder dbHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				//向Spring IOC容器注册解析得到的Bean定义，这是Bean定义向IOC容器注册的入口
				// Register the final decorated instance.
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			//在完成向Spring IOC容器注册解析得到的Bean定义后，发送注册事件
			// Send registration event.
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	/**
	 * 在我们开始处理bean定义之前，允许通过处理任何自定义元素类型来扩展XML
	 * Allow the XML to be extensible by processing any custom element types first,
	 * before we start to process the bean definitions.
	 * 此方法是XML任何其他自定义预处理的自然扩展点。
	 * This method is a natural extension point for any other custom pre-processing of the XML.
	 * 默认的实现是空的。子类可以覆盖此方法，将自定义元素转换为标准的Spring bean定义
	 * <p>The default implementation is empty. Subclasses can override this method to convert custom elements into standard Spring bean definitions,
	 * 比如，实现可以访问实现了访问解析器bean定义的reader 以及底层XML资源，通过相应的访问器
	 * for example. Implementors have access to the parser's bean definition reader and the underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * 在我们完成bean定义的处理之后，允许通过处理任何定制元素类型来扩展XML。
	 * Allow the XML to be extensible by processing any custom element types last, after we finished processing the bean definitions.
	 * 此方法是XML任何其他自定义后处理的自然扩展点
	 * This method is a natural extension point for any other custom post-processing of the XML.
	 * 默认的实现是空的。子类可以覆盖此方法，将自定义元素转换为标准的Spring bean定义
	 * <p>The default implementation is empty. Subclasses can override this method to convert custom elements into standard Spring bean definitions,
	 * 比如。实现者可以通过相应的访问器访问解析器的bean定义reader和底层XML资源。
	 * for example. Implementors have access to the parser's bean definition reader and the underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
