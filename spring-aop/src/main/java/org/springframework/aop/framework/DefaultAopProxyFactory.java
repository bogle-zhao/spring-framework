/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.springframework.aop.SpringProxy;

/**
 * https://cloud.tencent.com/developer/article/1497612
 *
 *
 * 默认的AopProxyFactory实现，它可以创建cglib代理或者是jdk的动态代理
 * Default {@link AopProxyFactory} implementation, creating either a CGLIB proxy
 * or a JDK dynamic proxy.
 *
 * 如果给定AdvisedSupport实例满足以下条件之一，则创建CGLIB代理 ：
 *
 * 		该optimize标志设置
 * 		该proxyTargetClass标志设置
 * 		没有指定代理接口
 * <p>Creates a CGLIB proxy if one the following is true for a given
 * {@link AdvisedSupport} instance:
 * <ul>
 * <li>the {@code optimize} flag is set
 * <li>the {@code proxyTargetClass} flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * 通常，指定proxyTargetClass执行CGLIB代理，
 * 或指定一个或多个接口使用JDK动态代理。
 *
 * <p>In general, specify {@code proxyTargetClass} to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
//通过对DefaultAopProxyFactory的源码分析，我们了解了Spring在创建AOP代理对象时：
//		• 如果配置的目标类是接口，则使用JDK的动态代理机制来生成AOP代理对象
//		• 如果使用的不是接口，则使用CGLIB方式来生成AOP代理对象
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		//如果AOP使用显式优化，或者配置了目标类，或者只使用Spring支持的代理接口
		//config.isOptimize()与config.isProxyTargetClass()默认返回都是false
		// 需要优化  强制cglib  没有实现接口等都会进入这里面来
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			//获取AOP配置的目标类
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			//如果配置的AOP目标类是接口，则使用JDK动态代理机制来生成AOP代理
			// 倘若目标Class本身就是个接口，或者它已经是个JDK得代理类（Proxy的子类。所有的JDK代理类都是此类的子类），那还是用JDK的动态代理吧
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			// 实用CGLIB代理方式 ObjenesisCglibAopProxy是CglibAopProxy的子类。Spring4.0之后提供的

//			Objenesis是专门用于实例化一些特殊java对象的一个工具，如私有构造方法。我们知道带参数的构造等不能通过class.newInstance()实例化的，通过它可以轻松完成
//			基于Objenesis的CglibAopProxy扩展，用于创建代理实例，没有调用类的构造器
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			// 否则（一般都是有实现接口） 都会采用JDK得动态代理
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * //判断AOP是否只配置了SpringProxy代理接口或者没有配置任何代理接口
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	// 如果它没有实现过接口（ifcs.length == ）  或者 仅仅实现了一个接口，但是呢这个接口却是SpringProxy类型的   那就返回false
	// 总体来说，就是看看这个cofnig有没有实现过靠谱的、可以用的接口
	// SpringProxy:一个标记接口。Spring AOP产生的所有的代理类 都是它的子类~~
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
	}

}
