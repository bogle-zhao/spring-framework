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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 参考资料：https://www.jianshu.com/p/021702f21e7e
 *
 * 是BeanFactory接口的扩展，被能够一次性列举所有它们bean实例，而不是试图根据客户端请求一个一个的通过名字查找的
 * 的工厂容器实现。那些需要预先加载所有bean定义的工厂需要实现这个接口。
 *
 * Extension of the {@link BeanFactory} interface to be implemented by bean factories
 * that can enumerate all their bean instances, rather than attempting bean lookup
 * by name one by one as requested by clients. BeanFactory implementations that
 * preload all their bean definitions (such as XML-based factories) may implement
 * this interface.
 *
 * 如果这是一个层次继承的接口，则只会考虑当前层次这个工厂内的bean定义，而不会考虑其他任何层次。
 * 也可以使用BeanFactoryUtils这个帮助类来针对其祖先工厂也考虑的情况。
 *
 * <p>If this is a {@link HierarchicalBeanFactory}, the return values will <i>not</i>
 * take any BeanFactory hierarchy into account, but will relate only to the beans
 * defined in the current factory. Use the {@link BeanFactoryUtils} helper class
 * to consider beans in ancestor factories too.
 *
 * 这个接口中的方法仅仅关注此工厂内部的bean定义。
 * 它们会忽略任何被像ConfigurableBeanFactory的registerSingleton方法已注册的单例的bean，
 * getBeanNamesOfType和getBeansOfType例处，但也会手动的检查已被注册的单例bean。
 * 当然，BeanFactory的getBean方法也可以透明的访问这些特殊的bean（已被注册的单例bean）。
 * 然而，在经典的场合，无论如何，所有bean都会被处部定义 定义，所以许多程序不需要这方面不同。
 *
 * <p>The methods in this interface will just respect bean definitions of this factory.
 * They will ignore any singleton beans that have been registered by other means like
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}'s
 * {@code registerSingleton} method, with the exception of
 * {@code getBeanNamesOfType} and {@code getBeansOfType} which will check
 * such manually registered singletons too. Of course, BeanFactory's {@code getBean}
 * does allow transparent access to such special beans as well. However, in typical
 * scenarios, all beans will be defined by external bean definitions anyway, so most
 * applications don't need to worry about this differentiation.
 *
 * 注意：除了getBeanDefinitionCount和containsBeanDefinition，这个接口中的方法没有被当作
 * 频烦调用的方法设计，实现可以会慢。
 *
 * <p><b>NOTE:</b> With the exception of {@code getBeanDefinitionCount}
 * and {@code containsBeanDefinition}, the methods in this interface
 * are not designed for frequent invocation. Implementations may be slow.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
//根据各种条件获取 bean 的配置清单
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * 检查这个BeanFactory是否包含给定名字的的bean定义。
	 * 不考虑这个工厂参与的任何层级关系，
	 * 忽略不是bean定义的所有通过其他方式注册的单例bean
	 *
	 * Check if this bean factory contains a bean definition with the given name.
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 */
	//根据beanName,检查容器是否含有BeanDefinition
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回在此工厂中定义的bean数量。
	 * 不考虑这个工厂参与的任何层级关系，
	 * 忽略不是bean定义的所有通过其他方式注册的单例bean
	 *
	 * Return the number of beans defined in the factory.
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the number of beans defined in the factory
	 */
	//返回容器含有的BeanDefinition总数
	int getBeanDefinitionCount();

	/**
	 *
	 * 返回此工厂中定义的所有bean的名字。
	 * 不考虑此工厂参与的所有层次关系，
	 * 忽略不是bean定义的所有通过其他方式注册的单例bean
	 *
	 * Return the names of all beans defined in this factory.
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the names of all beans defined in this factory,
	 * or an empty array if none defined		如果此工厂没有bean定义，返回一个空的数组。
	 */
	//返回容器中所有bean的名字
	String[] getBeanDefinitionNames();

	/**
	 * 返回匹配给定类型（包括子类）的所有bean的名字，如果是普通bean，则是bean定义的名字，如果是
	 * FactoryBean，则是其getObjectType方法返回的对象的名字
	 *
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of {@code getObjectType}
	 * in the case of FactoryBeans.
	 *
	 * 这个方法只考虑最顶层的bean，内部嵌套的bean即便可能匹配指定的类型也不考虑。
	 *
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 *
	 * 如果考虑FactoryBean创建的对象，意味着FactoryBean已经初始化。
	 * 如果FactoryBean创建的对象不匹配，FactoryBean自身将与对象进行匹配。
	 *
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 *
	 * 不考虑任何此工厂参与的层级关系。
	 * 也可以使用BeanFactoryUtils的beanNamesForTypeIncludingAncestors方法处理有关继承方面的问题。
	 *
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beanNamesForTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 *
	 * 不忽略不是bean定义的通过其他方式注册的单例bean.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 *
	 * getBeansOfType方法的这个版本匹配所有种类的bean，可是是单例的，原型的，FactoryBean.
	 * 在许多实现中，此方法返回的结果与调用getBeansOfType(type, true, true)一样。
	 *
	 * <p>This version of {@code getBeanNamesForType} matches all kinds of beans,
	 * be it singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for {@code getBeanNamesForType(type, true, true)}.
	 *
	 * 这个方法返回的bean名称应该尽可能与后台配置的bean定义顺序一样。
	 *
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * @param type the generically typed class or interface to match	要匹配的类或者是接口，如果是null，返回所有bean的名字
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none		如果是不存指定类型的bean，则返回一个空的数组
	 * @since 4.2
	 * @see #isTypeMatch(String, ResolvableType)
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType)
	 */
	//根据类型返回BeanNames
	String[] getBeanNamesForType(ResolvableType type);

	/**
	 * 此处翻译同上
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of {@code getObjectType}
	 * in the case of FactoryBeans.
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beanNamesForTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>This version of {@code getBeanNamesForType} matches all kinds of beans,
	 * be it singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for {@code getBeanNamesForType(type, true, true)}.
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * @param type the class or interface to match, or {@code null} for all bean names
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	//根据类型（包括子类）返回BeanNames
	String[] getBeanNamesForType(@Nullable Class<?> type);

	/**
	 * 此处以上同上。
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of {@code getObjectType}
	 * in the case of FactoryBeans.
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 *
	 * 考虑对象为FactoryBean创建的情况，如果allowEagerInit设置，FactoryBean对象会初始化，如果被FactoryBean创建
	 * 的对象与指定类型不匹配，那么将与此FactoryBean本身匹配;
	 * 如果allowEagerInit没有设置，仅仅FactoryBean本身被检查。
	 *
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 *
	 * 此段同上
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beanNamesForTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 *
	 *	是否也包含多实例型bean或者scoped bean，或者仅仅只是单例bean（也适用于FactoryBean）
	 * @param type the class or interface to match, or {@code null} for all bean names
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 *  是否允许马上加载 ，如果是factoryBean创建的对象，此处应是true
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	/**根据类型(包括子类)返回BeanNames
	 *includeNonSingletons：false,代表只包含单例；true，代表包含多例、单例
	 *allowEagerInit：是否懒加载（注意：FactoryBeans都是立即加载）
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 返回匹配给定类型（包含子类）的实例，可能是通过bean定义创建，也可以是FactoryBean时其getObjectType返回
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * {@code getObjectType} in the case of FactoryBeans.
	 *
	 * 注意：此方法仅考虑最顶层bean，不含其内部嵌套的bean，即使内部嵌套的bean匹配给定类型
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 *
	 * 如果考虑FactoryBean创建的对象，需要先初始化对应的FactoryBean。
	 * 如果FactoryBean创建的对象与指定类型不匹配，则需要匹配FactoryBean对象本身
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 *
	 * 不考虑此工厂所参与的任何层次，也可以使用BeanFactoryUtils的beansOfTypeIncludingAncestors
	 * 的方法处理考虑分层处理的情况。
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beansOfTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 *
	 *	注：不忽略那些不是bean定义的已经通过其他方式创建的单例bean
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 *
	 * getBeansOfType方法的这个版本匹配所有种类的bean，可是是单例的，原型的，FactoryBean.
	 * 在许多实现中，此方法返回的结果与调用getBeansOfType(type, true, true)一样。
	 * <p>This version of getBeansOfType matches all kinds of beans, be it
	 * singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for {@code getBeansOfType(type, true, true)}.
	 *
	 * 这个方法返回的map应该是匹配指定类型的bean的名字与其名字对应的实例的键值对，
	 * 并且顺序要尽最大可能的与配置时一样。
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible.
	 *
	 * @param type the class or interface to match, or {@code null} for all concrete beans		指定的类或者是接口，如空是空，则匹配所有现有bean
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if a bean could not be created		如果bean不能被创建 ，则抛出此异常
	 * @since 1.1.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	//根据类型（包括子类）返回Map(BeanName,BeanInstance)
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

	/**
	 * 此处以上参考上面
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * {@code getObjectType} in the case of FactoryBeans.
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 *
	 * 考虑FactoryBean创建的对象时：如果设置allowEagerInit，也就是默认初始化FactoryBean.
	 * 这时如果FactoryBean创建的对象与指定的类型不匹配，将匹配该FactoryBean实例本身。
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 *
	 *	此处同样参考上面
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beansOfTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible.
	 *
	 *
	 * @param type the class or interface to match, or {@code null} for all concrete beans
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if a bean could not be created
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	/**根据类型(包括子类)返回Map(BeanNames,BeanInstance)
	 *includeNonSingletons：false,代表只包含单例；true，代表包含多例、单例
	 *allowEagerInit：是否懒加载（注意：FactoryBeans都是立即加载）
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * 查找所有使用注解的类，返回BeanNames
	 * 通过指定的注解类型，获取所有那些还没有创建bean实例的名字。
	 * Find all names of beans whose {@code Class} has the supplied {@link Annotation}
	 * type, without creating corresponding bean instances yet.
	 * <p>Note that this method considers objects created by FactoryBeans, which means
	 * that FactoryBeans will get initialized in order to determine their object type.
	 * @param annotationType the type of annotation to look for		指定的注解类型
	 * @return the names of all matching beans		匹配的所有bean的名字
	 * @since 4.0
	 */
	//查找所有使用注解的类，返回BeanNames
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	/**
	 * 根据注解的类型，返回Map(BeanNames,BeanInstance)
	 * 查找所有注解为指定类型的bean，返回一个bean名字与其对应实例的映射表。
	 * Find all beans whose {@code Class} has the supplied {@link Annotation} type,
	 * returning a Map of bean names with corresponding bean instances.
	 * <p>Note that this method considers objects created by FactoryBeans, which means
	 * that FactoryBeans will get initialized in order to determine their object type.
	 * @param annotationType the type of annotation to look for		要查找的注解类型
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values		返回一map，以匹配的bean名字作为键，以bean名字对应的bean实例作为值。
	 * @throws BeansException if a bean could not be created		如果bean实例不能被创建，抛出
	 * @since 3.0
	 */
	//根据注解的类型，返回Map(BeanNames,BeanInstance)
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	/**
	 * 查找指定bean的注解。
	 * 如果在指定bean自身上面没有找到，则遍历它实现的接口和他的超类。
	 * Find an {@link Annotation} of {@code annotationType} on the specified
	 * bean, traversing its interfaces and super classes if no annotation can be
	 * found on the given class itself.
	 * @param beanName the name of the bean to look for annotations on		指定的bean 的名称
	 * @param annotationType the annotation class to look for		指定的注解类型
	 * @return the annotation of the given type if found, or {@code null} otherwise		返回找到的注解，或者是null
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name		如果找不到给定名字的bean定义，抛出此异常
	 * @since 3.0
	 */
	//根据注解类型和BeanName来查找指定的Bean
	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
