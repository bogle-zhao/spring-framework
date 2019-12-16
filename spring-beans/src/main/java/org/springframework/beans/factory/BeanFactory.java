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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 这个接口是用于访问spring容器的根入口，它是bean容器的客户端视图，更近一步的接口ListableBeanFactory，ConfigurableBeanFactory
 * The root interface for accessing a Spring bean container.
 * This is the basic client view of a bean container;
 * further interfaces such as {@link ListableBeanFactory} and
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * are available for specific purposes.
 *
 * 这个接口被对象实现，这个对象持有大量的bean定义，每一个对象通常都是由一个唯一的string名字来标识的，
 * 根据bean定义的不同，这个工厂会创建每一个对象所独立的实例（Prototype类型）或者返回一个所有对象所共享的对象实例（Singleton类型）
 * 在Singleton类型实例，在factory中是唯一的，它们的创建取决于bean的配置，他们的api是相同的，在spring2.0中，出现更多的范围，取决具体的应用上下文（比如web上下文）
 *
 * <p>This interface is implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. Depending on the bean definition,
 * the factory will return either an independent instance of a contained object
 * (the Prototype design pattern), or a single shared instance (a superior
 * alternative to the Singleton design pattern, in which the instance is a
 * singleton in the scope of the factory). Which type of instance will be returned
 * depends on the bean factory configuration: the API is the same. Since Spring
 * 2.0, further scopes are available depending on the concrete application
 * context (e.g. "request" and "session" scopes in a web environment).
 *
 * 这种设计方法的重点是BeanFactory是应用程序组件的中央注册表，它对组件做了中央的处理和管理，它不需要每个组件读取自己的配置
 * 它是由beanfactory统一管理
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes configuration of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
 *
 * 注意：通常通过setter方法注入或者构造函数注入的方式来配置程序对象是优于程序本身控制其依赖对象初始化的，
 * Spring的依赖注入功能由使用这个接口的对象和它的子类实现
 * <p>Note that it is generally better to rely on Dependency Injection
 * ("push" configuration) to configure application objects through setters
 * or constructors, rather than use any form of "pull" configuration like a
 * BeanFactory lookup. Spring's Dependency Injection functionality is
 * implemented using this BeanFactory interface and its subinterfaces.
 *
 * 通常一个BeanFactory会加载存储在配置资源文件（如XML文档）中的bean定义，然后使用org.springframework.beans
 * 包来配置对象。然而，一些简单的实现可以直接返回直接在JAVA 代码中返回的对象。
 * 此接口没有bean定义存储的约束，可以是：LDAP, RDBMS, XML,properties file等。
 * 鼓励实现此接口支持依赖注入。
 * <p>Normally a BeanFactory will load bean definitions stored in a configuration
 * source (such as an XML document), and use the {@code org.springframework.beans}
 * package to configure the beans. However, an implementation could simply return
 * Java objects it creates as necessary directly in Java code. There are no
 * constraints on how the definitions could be stored: LDAP, RDBMS, XML,
 * properties file, etc. Implementations are encouraged to support references
 * amongst beans (Dependency Injection).
 *
 * 相比于ListableBeanFactory中的方法，如果这个接口真实类型为HierarchicalBeanFactory
 * ，这个接口中的所有方法会检查他的父接口
 * 如果一个bean没有在当前工厂实例中没有找到，将会直接请求其直接父工厂。
 * 这个工厂实例中的所有bean被假定为配置其所有父工厂实例中与其同名的bean。
 * <p>In contrast to the methods in {@link ListableBeanFactory}, all of the
 * operations in this interface will also check parent factories if this is a
 * {@link HierarchicalBeanFactory}. If a bean is not found in this factory instance,
 * the immediate parent factory will be asked. Beans in this factory instance
 * are supposed to override beans of the same name in any parent factory.
 *
 * Bean factory实现应该支持Bean标准的生命周期实现，完整的初始化方法以及标准的顺序是如下列表
 * <p>Bean factory implementations should support the standard bean lifecycle interfaces
 * as far as possible. The full set of initialization methods and their standard order is:
 * <ol>
 * <li>BeanNameAware's {@code setBeanName}
 * <li>BeanClassLoaderAware's {@code setBeanClassLoader}
 * <li>BeanFactoryAware's {@code setBeanFactory}
 * <li>EnvironmentAware's {@code setEnvironment}
 * <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
 * <li>ResourceLoaderAware's {@code setResourceLoader}
 * (only applicable when running in an application context)
 * <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
 * (only applicable when running in an application context)
 * <li>MessageSourceAware's {@code setMessageSource}
 * (only applicable when running in an application context)
 * <li>ApplicationContextAware's {@code setApplicationContext}
 * (only applicable when running in an application context)
 * <li>ServletContextAware's {@code setServletContext}
 * (only applicable when running in a web application context)
 * <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
 * <li>InitializingBean's {@code afterPropertiesSet}
 * <li>a custom init-method definition
 * <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
 * </ol>
 *
 *  bean factory 销毁时，会执行如下的生命周期
 * <p>On shutdown of a bean factory, the following lifecycle methods apply:
 * <ol>
 * <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
 * <li>DisposableBean's {@code destroy}
 * <li>a custom destroy-method definition
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
//定义获取 bean 及 bean 的各种属性
public interface BeanFactory {

	/**
	 * 用于区别FactoryBean,和FactoryBean创建的beans。
	 * 例如：如果一个名字为myJndiObject的FactoryBean，BeanFactory调用getBean("myJndiObject")方法时返回的不是该FactoryBean
	 * 实例，而是该FactoryBean调用其自身getObject方法返回的对象，要想返回该FactoryBean实例，则需要BeanFactory实例
	 * 这样调用getBean("&myJndiObject")
	 * 注：Bean和FactoryBean为Spring中的两种bean,bean为普通bean,FactoryBean为工厂bean,都规BeanFactory管理
	 * Used to dereference a {@link FactoryBean} instance and distinguish it from
	 * beans <i>created</i> by the FactoryBean. For example, if the bean named
	 * {@code myJndiObject} is a FactoryBean, getting {@code &myJndiObject}
	 * will return the factory, not the instance returned by the factory.
	 */
	//对FactoryBean的转义定义，因为如果使用bean的名字检索FactoryBean得到的对象是工厂生成的对像
	//如果需要得到工厂本身，需要转义
	String FACTORY_BEAN_PREFIX = "&";


	/**
	 * 返回指定的bean实例，可以是共享的，可以是独立的。
	 * 这个方法允许spring BeanFactory作为单例设计模式和原型设计模式的替代。
	 * 在单例bean的情型下，调用者可以保留对返回对象的引用。
	 * 将别名转换回正常的对应的beanName.
	 * 如果在本实例中找不到bean，将会其父工厂中查找 。
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>This method allows a Spring BeanFactory to be used as a replacement for the
	 * Singleton or Prototype design pattern. Callers may retain references to
	 * returned objects in the case of Singleton beans.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to retrieve		需要检索的bean的名字
	 * @return an instance of the bean		返回指定名字的bean实例
	 * @throws NoSuchBeanDefinitionException if there is no bean definition		如果检索不到对应的bean实例，抛出此异常
	 * with the specified name
	 * @throws BeansException if the bean could not be obtained
	 * 注：如果传入的名字为FactoryBean的名字，则如上面“注”。
	 */
	//根据bean名字，获取在IOC容器中用到Bean实例
	Object getBean(String name) throws BeansException;

	/**
	 * 返回指定的bean实例，可以是共享的，可以是独立的。
	 * 与上面的getBean方法行为一样，但是提供了一个类型检测，如果找到的bean不是要求的类型，则抛出BeanNotOfRequiredTypeException
	 * 异常。其他同上。
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Behaves the same as {@link #getBean(String)}, but provides a measure of type
	 * safety by throwing a BeanNotOfRequiredTypeException if the bean is not of the
	 * required type. This means that ClassCastException can't be thrown on casting
	 * the result correctly, as can happen with {@link #getBean(String)}.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to retrieve
	 * @param requiredType type the bean must match. Can be an interface or superclass		返回bean实例的类型，可以是其实现的接口，也可以是其父类，也可以是null任意匹配
	 * of the actual class, or {@code null} for any match. For example, if the value
	 * is {@code Object.class}, this method will succeed whatever the class of the
	 * returned instance.
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws BeansException if the bean could not be created
	 */
	//根据bean名字和class类型得到bean实例，增加了类型安全验证机制
	<T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;

	/**
	 * 返回指定名字的bean，可以是独立的，也可以是共享的
	 * 允许清楚的指定普通bean的构造函数的参数列表或者是FactoryBean的工厂方法参数列表，这将会覆盖bean原定义中的默认参数。
	 * @param name  指定的需要返回的Bean的名字
	 * @param args  普通Bean的构造函数参数，或者是FactoryBean的工厂方法参数列表
	 *  （仅在创建一个新的实例的时候使用，而不是检索已存在实例的时候）
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Allows for specifying explicit constructor arguments / factory method arguments,
	 * overriding the specified default arguments (if any) in the bean definition.
	 * @param name the name of the bean to retrieve
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanDefinitionStoreException if arguments have been given but
	 * the affected bean isn't a prototype
	 * @throws BeansException if the bean could not be created
	 * @since 2.5
	 */
	Object getBean(String name, Object... args) throws BeansException;

	/**
	 *
	 * 返回任何惟一匹配 给定类型的bean实例。
	 * 这个方法进入ListableBeanFactory的 by-type查找区域查找 ，但也可能给定类型的名字被转换成传统的按名字查找。
	 * 针对更多对于beans集合的检索操作，使用ListableBeanFactory，BeanFactoryUtils。
	 * 指定类型不可以是空
	 * Return the bean instance that uniquely matches the given object type, if any.
	 * <p>This method goes into {@link ListableBeanFactory} by-type lookup territory
	 * but may also be translated into a conventional by-name lookup based on the name
	 * of the given type. For more extensive retrieval operations across sets of beans,
	 * use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @return an instance of the single bean matching the required type
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @since 3.0
	 * @see ListableBeanFactory
	 */
	<T> T  getBean(Class<T> requiredType) throws BeansException;

	/**
	 * 返回指定类型的bean实例，可以是共享的，也可以是独立的
	 * 允许清楚的指定普通bean的构造函数的参数列表或者是FactoryBean的工厂方法参数列表，这将会覆盖bean原定义中的默认参数。
	 * 这个方法进入ListableBeanFactory的 by-type查找区域查找 ，但也可能给定类型的名字被转换成传统的按名字查找。
	 * 针对更多对于beans集合的检索操作，使用ListableBeanFactory，BeanFactoryUtils。
	 * @param requiredType   返回实例必须匹配的类型，可以是该实例实现的接口，可以是其父类，不可以是null
	 * @param args  当依据指定的参数创创建新的实例时指定的参数
	 *  （仅在创建一个新的实例的时候使用，而不是检索已存在实例的时候）
	 * @return
	 * @throws BeansException
	 * （1）NoSuchBeanDefinitionException  如果没有指定类型的bean定义时抛出。
	 * （2）BeanDefinitionStoreException   指定了参数，但是返回的的有郊bean实例不是原型的时候抛出。
	 *                                       我的理解：需要返回的是新创建的，而不是已存在的
	 * （3）BeansException                  bean实例无法创建时抛出
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Allows for specifying explicit constructor arguments / factory method arguments,
	 * overriding the specified default arguments (if any) in the bean definition.
	 * <p>This method goes into {@link ListableBeanFactory} by-type lookup territory
	 * but may also be translated into a conventional by-name lookup based on the name
	 * of the given type. For more extensive retrieval operations across sets of beans,
	 * use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanDefinitionStoreException if arguments have been given but
	 * the affected bean isn't a prototype
	 * @throws BeansException if the bean could not be created
	 * @since 4.1
	 */
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;


	/**
	 * 判断这个BeanFactory是否包含指定名字的bean定义或者是包含指定名字的处部已注册的bean实例。
	 * 如果所给名字为别名，将会被转换回正确的规范的bean名字。
	 * 如果当前BeanFactory是分层的，那么当在本实例范围内找不到时会在其所有父工厂实例里查找。
	 * 如果存在指定名字的bean定义或者是单例实例，返回true,与抽象类还是实现类，延迟加载还是马上加载，当前范围还是其父工厂范围无关。
	 * 所以注意：这个方法返回true,但并不意味着当传入同样名字调用此工厂的getBean方法时一定会获得对象实例。
	 * Does this bean factory contain a bean definition or externally registered singleton
	 * instance with the given name?
	 * <p>If the given name is an alias, it will be translated back to the corresponding
	 * canonical bean name.
	 * <p>If this factory is hierarchical, will ask any parent factory if the bean cannot
	 * be found in this factory instance.
	 * <p>If a bean definition or singleton instance matching the given name is found,
	 * this method will return {@code true} whether the named bean definition is concrete
	 * or abstract, lazy or eager, in scope or not. Therefore, note that a {@code true}
	 * return value from this method does not necessarily indicate that {@link #getBean}
	 * will be able to obtain an instance for the same name.
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is present
	 */
	//提供对bean的检索，看看是否在IOC容器中有这个名字的bean
	boolean containsBean(String name);

	/**
	 * 判断是否是一个共享的单例对象。如果是，查看getBean方法看其是否一定会返回同一实例。
	 * 注意：此方法返回false时，并不表示给定名字的实例一定是独立的实例。
	 * 它表示非单例实例，也可能是对应一定范围的Bean（request,session）。
	 * 将别名转换回对应的规范的bean名字
	 * 当在本工厂实例中无法找到给定名字的bean时，在其父工厂中查找
	 * Is this bean a shared singleton? That is, will {@link #getBean} always
	 * return the same instance?
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * independent instances. It indicates non-singleton instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isPrototype} operation to explicitly
	 * check for independent instances.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return whether this bean corresponds to a singleton instance
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @see #getBean
	 * @see #isPrototype
	 */
	//根据bean名字得到bean实例，并同时判断这个bean是不是单例
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 判断是否是一个独立的对象，如果是true,如果是，查看getBean方法看其是否一定会返回一个独立的实例。
	 * 注意：此方法返回false时，并不表示给定名字的实例一定是单例实例。
	 * 它表示非独立的实例，也可能是对应一定范围的Bean（request,session）。
	 * 将别名转换回对应的规范的bean名字
	 * 当在本工厂实例中无法找到给定名字的bean时，在其父工厂中查找
	 * Is this bean a prototype? That is, will {@link #getBean} always return
	 * independent instances?
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * a singleton object. It indicates non-independent instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isSingleton} operation to explicitly
	 * check for a shared singleton instance.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return whether this bean will always deliver independent instances
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name	如果给定名字的Bean不存在，抛出此异常
	 * @since 2.0.3
	 * @see #getBean
	 * @see #isSingleton
	 */
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 检查给定名字的实例是否匹配指定类型。
	 * 更具体的说，检查通过给定名字的一个getBean调用返回的对象是否是指定的目录类型。
	 * 如果给定名字的bean本工厂内找不到，将到其父工厂中查找
	 * @param name
	 * @param typeToMatch
	 * @return
	 * @throws NoSuchBeanDefinitionException  如果给定名字的Bean不存在，抛出此异常
	 * 本方法从spring 4.2开始
	 * Check whether the bean with the given name matches the specified type.
	 * More specifically, check whether a {@link #getBean} call for the given name
	 * would return an object that is assignable to the specified target type.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @param typeToMatch the type to match against (as a {@code ResolvableType})
	 * @return {@code true} if the bean type matches,
	 * {@code false} if it doesn't match or cannot be determined yet
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 4.2
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * Check whether the bean with the given name matches the specified type.
	 * More specifically, check whether a {@link #getBean} call for the given name
	 * would return an object that is assignable to the specified target type.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @param typeToMatch the type to match against (as a {@code Class})
	 * @return {@code true} if the bean type matches,
	 * {@code false} if it doesn't match or cannot be determined yet
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 2.0.1
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * 明确给定名字对应Bean的类型。
	 * 具体说就是，确定通过给定名字调用getBean方法返回的Object的类型。
	 * 对于FactoryBean,返回FactoryBean创建的Object的类型，就像FactoryBean的getObjectType方法
	 * Determine the type of the bean with the given name. More specifically,
	 * determine the type of object that {@link #getBean} would return for the given name.
	 * <p>For a {@link FactoryBean}, return the type of object that the FactoryBean creates,
	 * as exposed by {@link FactoryBean#getObjectType()}.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return the type of the bean, or {@code null} if not determinable
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 1.1.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	//得到bean实例的class类型
	@Nullable
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 通过给定的bean名字，获取其所有的别名。
	 * 当getBean方法调用时，那些所有的别名都指向同一个bean。
	 * 如果传入的是一个别名，那么这个别名所对应的bean名字，和这个bean名字所对的其他别名被返回，这个bean的名字在数组的第一位
	 * Return the aliases for the given bean name, if any.
	 * All of those aliases point to the same bean when used in a {@link #getBean} call.
	 * <p>If the given name is an alias, the corresponding original bean name
	 * and other aliases (if any) will be returned, with the original bean name
	 * being the first element in the array.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @see #getBean
	 */
	//得到bean别名，如果根据别名检索，那么其原名也会被检索出来
	String[] getAliases(String name);

}
