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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DecoratingProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 生成JDK动态代理
 * JDK-based {@link AopProxy} implementation for the Spring AOP framework,
 * based on JDK {@link java.lang.reflect.Proxy dynamic proxies}.
 *
 * <p>Creates a dynamic proxy, implementing the interfaces exposed by
 * the AopProxy. Dynamic proxies <i>cannot</i> be used to proxy methods
 * defined in classes, rather than interfaces.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an {@link AdvisedSupport} class. This class is internal
 * to Spring's AOP framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class will be thread-safe if the
 * underlying (target) class is thread-safe.
 *
 * <p>Proxies are serializable so long as all Advisors (including Advices
 * and Pointcuts) and the TargetSource are serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
//JDK创建的Aop代理对象即JdkDynamicAopProxy，创建JdkDynamicAopProxy是通过Proxy.newProxyInstance()方法，该方法需要指明三个参数:
//		• 一个是类装载器
//		• 一个是代理接口
//		• 一个是就是Proxy回调方法所在的对象，这个对象需要实现InvocationHandler接口，这个InvocationHandler接口定了invoke方法，提供代理对象的回调入口，也就是真正调用目标对象的地方

//JdkDynamicAopProxy本身实现了InvocationHandler接口和invoke()方法，JDK的动态代理机制的工作原理是：当调用目标对象的方法时，
// 不是直接调用目标对象，而是首先生成一个目标对象的动态代理对象，触发代理对象的invoke()方法，代理的invoke()方法才会真正调用目标对象的方法。
// Spring AOP的实现原理是在代理对象invoke()方法调用目标对象的方法时，调用配置的通知。
//	https://www.yuque.com/liuxinfeng/studynotes/sn3nze
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = 5531744639992436476L;


	/*
	 * NOTE: We could avoid the code duplication between this class and the CGLIB
	 * proxies by refactoring "invoke" into a template method. However, this approach
	 * adds at least 10% performance overhead versus a copy-paste solution, so we sacrifice
	 * elegance for performance. (We have a good test suite to ensure that the different
	 * proxies behave the same :-)
	 * This way, we can also more easily take advantage of minor optimizations in each class.
	 */

	/** We use a static Log to avoid serialization issues */
	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/** Config used to configure this proxy */
	private final AdvisedSupport advised;

	/**
	 * Is the {@link #equals} method defined on the proxied interfaces?
	 */
	private boolean equalsDefined;

	/**
	 * Is the {@link #hashCode} method defined on the proxied interfaces?
	 */
	private boolean hashCodeDefined;


	/**
	 * 根据给定的AOP配置创建一个JdkDynamicAopProxy
	 * Construct a new JdkDynamicAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		//获取AOPBeanFactory中配置的通知器链和目标源
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		//为当前对象设置AOP配置
		this.advised = config;
	}


	//获取AOP代理对象的入口方法
	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	//创建AOP代理对象
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
		}
		//获取AOPBeanFactory中配置的代理接口
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
		//查找代理目标的接口中是否定义equals()和hashCode()方法
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		//使用JDK的动态代理机制创建AOP代理对象
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

	/**
	 * Finds any {@link #equals} or {@link #hashCode} method that may be defined
	 * on the supplied set of interfaces.
	 * @param proxiedInterfaces the interfaces to introspect
	 */
	//查找给定类或接口中是否定义了equals()和hashCode()方法
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		//遍历给定的类/接口数组
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			//或者给定类/接口中所有声明的方法
			Method[] methods = proxiedInterface.getDeclaredMethods();
			//遍历类/接口中的声明的方法
			for (Method method : methods) {
				//如果方法是equals()方法，则设置当前对象equalsDefined属性
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				//如果方法是hashCode()方法，则设置当前对象hashCodeDefined属性
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}


	/**
	 * 实现InvocationHandler.invoke。
	 * 调用者将确切地看到目标抛出的异常，除非钩子方法抛出异常。
	 * Implementation of {@code InvocationHandler.invoke}.
	 * <p>Callers will see exactly the exception thrown by the target,
	 * unless a hook method throws an exception.
	 */
	//AOP代理对象的回调方法
	@Override
	@Nullable
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;
		//获取通知的目标源
		TargetSource targetSource = this.advised.targetSource;
		Object target = null;

		try {
			//如果代理目标对象的接口中没有定义equals()方法，且当前调用的方法
			//是equals()方法，即目标对象没有自己实现equals()方法
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				// 目标没有实现自己的equals方法
				return equals(args[0]);
			}
			//如果代理目标对象的接口中没有定义hashCode()方法，且当前调用的方法
			//是hashCode()方法，即目标对象没有自己实现hashCode()方法
			else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				// 目标没有实现自己的hashCodes方法
				return hashCode();
			}
			//如果AOP配置了通知，使用反射机制调用通知的同名方法
//			java.lang.reflect.Method.getDeclaringClass() ： 方法返回表示声明由此Method对象表示的方法的类的Class对象。
			else if (method.getDeclaringClass() == DecoratingProxy.class) {
				// There is only getDecoratedClass() declared -> dispatch to proxy config.
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}
			//如果AOP配置了通知，使用反射机制调用通知的同名方法
			else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				// 根据代理对象的配置调用服务，如果是Advised接口的实现类，则直接调用
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			//如果当前通知暴露了代理，则将当前代理使用currentProxy()方法变为可用代理
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			// 有可能为null.尽可能减少拥有目标对象的时间，在这种情况下对象来自于对象池
			//获取目标对象
			target = targetSource.getTarget();
			Class<?> targetClass = (target != null ? target.getClass() : null);

			// Get the interception chain for this method.
			// 获得这个方法的拦截器链
			//获取目标对象方法配置的拦截器(通知器)链
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// Check whether we have any advice. If we don't, we can fallback on direct
			// reflective invocation of the target, and avoid creating a MethodInvocation.
			//如果没有拦截器链，则直接调用目标对象
			if (chain.isEmpty()) {
				// 我们可以忽略创建MethodInvocation：直接去调用目标
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				//没有配置通知，使用反射直接调用目标对象的方法，并获取方法返回值
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			//如果配置了通知
			else {
				// We need to create a method invocation...
				// 构造一个方法调用
				//为目标对象创建方法回调对象，需要在调用通知之后才调用目标对象的方法
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// Proceed to the joinpoint through the interceptor chain.
				// 调用连接点的拦截器链(见3)
				//调用通知链，沿着通知器链调用所有配置的通知
				retVal = invocation.proceed();
			}

			// Massage return value if necessary.
			Class<?> returnType = method.getReturnType();
			//如果方法有返回值，则将代理对象最为方法返回
			if (retVal != null && retVal == target &&
					returnType != Object.class && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				// 必须来自TargetSource. 释放目标对象
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				// 重新保存旧的代理,存储代理对象
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Equality means interfaces, advisors and TargetSource are equal.
	 * <p>The compared object may be a JdkDynamicAopProxy instance itself
	 * or a dynamic proxy wrapping a JdkDynamicAopProxy instance.
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		else {
			// Not a valid comparison...
			return false;
		}

		// If we get here, otherProxy is the other AopProxy.
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * Proxy uses the hash code of the TargetSource.
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}
