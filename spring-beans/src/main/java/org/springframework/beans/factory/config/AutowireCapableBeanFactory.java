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

package org.springframework.beans.factory.config;

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * Extension of the {@link org.springframework.beans.factory.BeanFactory}
 * interface to be implemented by bean factories that are capable of
 * autowiring, provided that they want to expose this functionality for
 * existing bean instances.
 *
 * BeanFactory接口的扩展，被那些能够实现自动匹配的工厂实现，提供针对已存在bean实例的服务。
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: stick to {@link org.springframework.beans.factory.BeanFactory}
 * or {@link org.springframework.beans.factory.ListableBeanFactory} for
 * typical use cases.
 *
 * 此BeanFactory的子接口通常情况下不用于应用程序代码，
 * 典型使用案例查看BeanFactory或者ListableBeanFactory
 *
 * <p>Integration code for other frameworks can leverage this interface to
 * wire and populate existing bean instances that Spring does not control
 * the lifecycle of. This is particularly useful for WebWork Actions and
 * Tapestry Page objects, for example.
 *
 * 整合其他框架的代码是此接口优势所在，它可以连接和填充那些生命周期不被Spring管理的已存在的bean实例
 *
 * <p>Note that this interface is not implemented by
 * {@link org.springframework.context.ApplicationContext} facades,
 * as it is hardly ever used by application code. That said, it is available
 * from an application context too, accessible through ApplicationContext's
 * {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()}
 * method.
 *
 * 此接口没有被ApplicationContext实现，因为他几乎不（代表有）在程序代码中使用。
 * 那也就是说他从一个application context上下文中也是可用的，
 * 通过ApplicationContext的getAutowireCapableBeanFactory方法就很容易访问此接口
 *
 * <p>You may also implement the {@link org.springframework.beans.factory.BeanFactoryAware}
 * interface, which exposes the internal BeanFactory even when running in an
 * ApplicationContext, to get access to an AutowireCapableBeanFactory:
 * simply cast the passed-in BeanFactory to AutowireCapableBeanFactory.
 *
 * 你也可通过实现BeanFactoryAware，通过它当运行在ApplicationContext暴露内部BeanFactory接口
 * 的功能访问AutowireCapableBeanFactory，仅仅需要将BeanFactory转换为AutowireCapableBeanFactory
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
//提供创建 bean、自动注入、初始化以及应用 bean 的后处理器
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * 表示没有外部定义自动匹配的常数。
	 * 注：此属性不影响实现BeanFactoryAware等接口和通过注解注入时的使用情况。
	 * Constant that indicates no externally defined autowiring. Note that
	 * BeanFactoryAware etc and annotation-driven injection will still be applied.
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_NO = 0;

	/**
	 * 表示通过名字自动配置bean属性 的常量（适用于bean所有属性的set方法）
	 * Constant that indicates autowiring bean properties by name
	 * (applying to all bean property setters).
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * 表示通过类型自动配置bean属性 的常量（适用于bean所有属性的set方法）
	 * Constant that indicates autowiring bean properties by type
	 * (applying to all bean property setters).
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * 表示通过构造函数匹配的常量（涉及解决选择适当的构造函数）
	 * Constant that indicates autowiring the greediest constructor that
	 * can be satisfied (involves resolving the appropriate constructor).
	 * @see #createBean
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * 表示自动选择适当匹配策略的常量
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * @see #createBean
	 * @see #autowire
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * prefer annotation-based autowiring for clearer demarcation of autowiring needs.
	 */
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;



	//-------------------------------------------------------------------------
	// Typical methods for creating and populating external bean instances
	//	典型的创建和填充处部bean实例的方法
	//-------------------------------------------------------------------------

	/**
	 * 创建给定类对象类型的新的bean实例。
	 * 完整初始化所创建bean，包含所有可应用的BeanPostProcessor BeanPostProcessors
	 * 注：此方法预期创建一个新的实例，填充有注解的属性和方法，同时应用所的bean标准初始化回调。
	 * 此方法并非通过传统的by-name或者by-type完成实例属性的自动注入，如果要使用
	 * 传统的这些方式，使用方法createBean(Class, int, boolean)
	 * Fully create a new bean instance of the given class.
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}.
	 * <p>Note: This is intended for creating a fresh instance, populating annotated
	 * fields and methods as well as applying all standard bean initialization callbacks.
	 * It does <i>not</> imply traditional by-name or by-type autowiring of properties;
	 * use {@link #createBean(Class, int, boolean)} for those purposes.
	 * @param beanClass the class of the bean to create
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed		实例化或者注入失败抛出此异常
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * 通过实例化之后的回调和bean属性的填充处理进程（如注解驱动注入） 填充给定的bean实例
	 * 注：此方法预期是填充有注解的属性和方法，或者是新建的实例，也或者是反序列化得到的实例。
	 * 此方法同样并非通过传统的by-name或者by-type实现属性的注入，反之，要达成此目的，使用方法autowireBeanProperties
	 * Populate the given bean instance through applying after-instantiation callbacks
	 * and bean property post-processing (e.g. for annotation-driven injection).
	 * <p>Note: This is essentially intended for (re-)populating annotated fields and
	 * methods, either for new instances or for deserialized instances. It does
	 * <i>not</i> imply traditional by-name or by-type autowiring of properties;
	 * use {@link #autowireBeanProperties} for those purposes.
	 * @param existingBean the existing bean instance
	 * @throws BeansException if wiring failed		如果注入失败，抛出此异常
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * 配置给定的未加工的bean：自动注入bean的属性，应用bean的属性值，
	 * 应用工厂回调就像方法setBeanName和setBeanFactory，也应用bean的后处理器（包括那些包装
	 * 结定未加工的bean的处理）
	 *
	 * 对于initializeBean方法返回的结果，此方法的效果相当 于initializeBean提供功能的超级有效集。
	 *
	 * 注：此方法需要给定名字的bean实例的bean定义
	 *
	 * Configure the given raw bean: autowiring bean properties, applying
	 * bean property values, applying factory callbacks such as {@code setBeanName}
	 * and {@code setBeanFactory}, and also applying all bean post processors
	 * (including ones which might wrap the given raw bean).
	 * <p>This is effectively a superset of what {@link #initializeBean} provides,
	 * fully applying the configuration specified by the corresponding bean definition.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * @param existingBean the existing bean instance		存在的bean实例
	 * @param beanName the name of the bean, to be passed to it if necessary	bean的名字
	 * (a bean definition of that name has to be available)
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException  	如果bean名字对就的bean定义不存在，抛出NoSuchBeanDefinitionException。如果初始化失败，抛出BeansException
	 * if there is no bean definition with the given name
	 * @throws BeansException if the initialization failed
	 * @see #initializeBean
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle
	// bean生命周中细粒度控制的特殊方法
	//-------------------------------------------------------------------------

	/**
	 * 依据指定的自动注入策略创建给定class对象的新的bean实例。
	 *
	 * 此接口中定义的所有常量在这里被使用。
	 *
	 * 执行给定bean的完整初始化，包括所有可应用的BeanPostProcessor和BeanPostProcessors
	 *
	 * 此方法是autowire方法提供的功能的一个有效超集，同时添加了initializeBean方法的功能。
	 *
	 * Fully create a new bean instance of the given class with the specified
	 * autowire strategy. All constants defined in this interface are supported here.
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}. This is effectively a superset
	 * of what {@link #autowire} provides, adding {@link #initializeBean} behavior.
	 * @param beanClass the class of the bean to create		要创建 的bean的类对象
	 * @param autowireMode by name or type, using the constants in this interface		使用此接口定的常量
	 * @param dependencyCheck whether to perform a dependency check for objects			是否执行对象的依赖检查（不支持构造函数注入）
	 * (not applicable to autowiring a constructor, thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed			如果初始化失败或者注入失败，抛出此异常
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 */
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 依据指定的注入策略初始化一个给定的类对象的bean实例。
	 * 此接口定义的所有常量都可以在这里使用。
	 * 也可以使用AUTOWIRE_NO策略调用，以便仅仅应用初始化之前的调用（例如注解驱动的注入）。
	 * 不会应用标准的BeanPostProcessor和BeanPostProcessors回调和更深一步执行bean的初始化。
	 * 此接口提供不同的，细粒度控制的方法实现那些操作（BeanPostProcessor和BeanPostProcessors
	 * 回调和更深一步执行bean的初始化），如initializeBean方法。
	 * 但是，如果实例的创建过程中如果InstantiationAwareBeanPostProcessor的回调是可用的，那么将被使用。
	 *
	 * Instantiate a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * before-instantiation callbacks (e.g. for annotation-driven injection).
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the construction of the instance.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance (not applicable to autowiring a constructor,
	 * thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 自动注入给定的bean实例的属性，通过by name 或者 type的方式，也可以通过AUTOWIRE_NO以实现仅仅对实例化
	 * 后回调的调用（例如注解驱动的注入）。
	 *
	 * Autowire the bean properties of the given bean instance by name or type.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * after-instantiation callbacks (e.g. for annotation-driven injection).
	 *
	 * 不会应用标准的BeanPostProcessor和BeanPostProcessors回调和更深一步执行bean的初始化。
	 * 此接口提供不同的，细粒度控制的方法实现那些操作（BeanPostProcessor和BeanPostProcessors
	 * 回调和更深一步执行bean的初始化），如initializeBean方法。
	 * 但是，如果实例的配置过程中如果InstantiationAwareBeanPostProcessor的回调是可用的，那么将被使用。
	 *
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * 应用给定bean名字的bean定义的属性值到给定的bean实例，指定的bean定义可以定义一个完全独立的bean
	 * 重要它自己的属性值，或者是仅仅属性值以用于给已存在的bean实例使用。
	 *
	 * Apply the property values of the bean definition with the given name to
	 * the given bean instance. The bean definition can either define a fully
	 * self-contained bean, reusing its property values, or just property values
	 * meant to be used for existing bean instances.
	 *
	 * 此方法不自动注入bean的属性，它仅仅应用明确定义的属性值。autowireBeanProperties方法实现了对已
	 * 存在实例的属性的自动注入。
	 *
	 * <p>This method does <i>not</i> autowire bean properties; it just applies
	 * explicitly defined property values. Use the {@link #autowireBeanProperties}
	 * method to autowire an existing bean instance.
	 *
	 * 注：这个方法要求指定的名字存在bean定义。
	 *
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance		已存在的bean实例
	 * @param beanName the name of the bean definition in the bean factory			指定的bean工厂中定义的bean名字
	 * (a bean definition of that name has to be available)
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name			如果没有指定名字的bean定义，抛出
	 * @throws BeansException if applying the property values failed		如果应用属性失败，抛出
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 *
	 * 初始化指定的原始bean，应用工厂回调如setBeanName和setBeanFactory方法，
	 * 也应用所有的bean后台处理器（包括包装指定原始bean的）
	 *
	 * Initialize the given raw bean, applying factory callbacks
	 * such as {@code setBeanName} and {@code setBeanFactory},
	 * also applying all bean post processors (including ones which
	 * might wrap the given raw bean).
	 *
	 * 注：不需要指定的bean名字的工厂中的bean定义一定存在。此bean名字仅用于回调，有作是否注册检查。
	 *
	 * <p>Note that no bean definition of the given name has to exist
	 * in the bean factory. The passed-in bean name will simply be used
	 * for callbacks but not checked against the registered bean definitions.
	 * @param existingBean the existing bean instance		已存在的bean实例
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors})
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if the initialization failed
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 应用后台处理器（BeanPostProcessor BeanPostProcessors）于指定的已存在的bean实例，
	 * 调用他们的postProcessBeforeInitialization方法。
	 * 返回的对象可能是一个原始对象的包装对象
	 *
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessBeforeInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 应用BeanPostProcessor BeanPostProcessors 于指定的已存在的bean实例
	 * ，调用他们的postProcessAfterInitialization方法。
	 * 返回的对象可能是原始对象的包装对象
	 *
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessAfterInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 销毁指定的bean实例，使用DisposableBean以及注册的DestructionAwareBeanPostProcessor
	 * DestructionAwareBeanPostProcessors。
	 *
	 * 销毁过程中出现的任何异常都需要捕捉且记录，而不应该抛出给调用者
	 *
	 * Destroy the given bean instance (typically coming from {@link #createBean}),
	 * applying the {@link org.springframework.beans.factory.DisposableBean} contract as well as
	 * registered {@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * @param existingBean the bean instance to destroy
	 */
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// Delegate methods for resolving injection points
	// 解决注入点的委托（代理）方法
	//-------------------------------------------------------------------------

	/**
	 * 解决惟一匹配指定类型的实例，包括实例的bean名称。
	 * 此方法是持有匹配实例bean名称的getBean(Class)方法的有效变种。
	 *
	 * Resolve the bean instance that uniquely matches the given object type, if any,
	 * including its bean name.
	 * <p>This is effectively a variant of {@link #getBean(Class)} which preserves the
	 * bean name of the matching instance.
	 * @param requiredType type the bean must match; can be an interface or superclass.
	 * {@code null} is disallowed.		要匹配的类型，不能是空
	 * @return the bean name plus bean instance		bean名字加bean实例
	 * @throws NoSuchBeanDefinitionException if no matching bean was found		没匹配抛出
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found		不是惟一匹配抛出
	 * @throws BeansException if the bean could not be created		不能创建实例时抛出
	 * @since 4.3.3
	 * @see #getBean(Class)
	 */
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	/**
	 * 解决此bean工厂中定义的指定的bean依赖。
	 * Resolve the specified dependency against the beans defined in this factory.
	 * @param descriptor the descriptor for the dependency (field/method/constructor)		依赖描述
	 * @param requestingBeanName the name of the bean which declares the given dependency		声明指定依赖的bean名称
	 * @return the resolved object, or {@code null} if none found
	 * @throws NoSuchBeanDefinitionException if no matching bean was found
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found
	 * @throws BeansException if dependency resolution failed for any other reason
	 * @since 2.5
	 * @see #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * @param descriptor the descriptor for the dependency (field/method/constructor)
	 * @param requestingBeanName the name of the bean which declares the given dependency
	 * @param autowiredBeanNames a Set that all names of autowired beans (used for
	 * resolving the given dependency) are supposed to be added to
	 * @param typeConverter the TypeConverter to use for populating arrays and collections
	 * @return the resolved object, or {@code null} if none found
	 * @throws NoSuchBeanDefinitionException if no matching bean was found
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found
	 * @throws BeansException if dependency resolution failed for any other reason
	 * @since 2.5
	 * @see DependencyDescriptor
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;

}
