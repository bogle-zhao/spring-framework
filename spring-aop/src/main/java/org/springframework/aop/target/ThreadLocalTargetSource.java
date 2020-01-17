/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.aop.target;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.NamedThreadLocal;

/**
 *
 * ThreadLocalTargetSource也就是和线程绑定的TargetSource，可以理解，其底层实现必然使用的是ThreadLocal。
 * 既然使用了ThreadLocal，也就是说我们需要注意两个问题：
 *
 * 目标对象必须声明为prototype类型，因为每个线程都会持有一个不一样的对象；
 * 目标对象必须是无状态的，因为目标对象是和当前线程绑定的，而Spring是使用的线程池处理的请求，
 * 因而每个线程可能处理不同的请求，因而为了避免造成问题，目标对象必须是无状态的。
 * ————————————————
 * 原文链接：https://blog.csdn.net/shenchaohao12321/article/details/85538163
 *
 * 这里ThreadLocalTargetSource主要集成了AbstractPrototypeBasedTargetSource和DisposableBean。
 * 关于AbstractPrototypeBasedTargetSource前面已经讲过了，读者可以到前面翻看；
 * 而DisposableBean的作用主要是提供一个方法，以供给Spring在销毁当前对象的时候调用。
 * 也就是说Spring在销毁当前TargetSource对象的时候会首先销毁其生成的各个目标对象。
 * 这里需要注意的是，TargetSource和生成的目标对象是两个对象，前面讲的TargetSouce都是单例的，
 * 只是生成的目标对象可能是单例的，也可能是多例的。
 * ————————————————
 * 原文链接：https://blog.csdn.net/shenchaohao12321/article/details/85538163
 *
 * Alternative to an object pool. This {@link org.springframework.aop.TargetSource}
 * uses a threading model in which every thread has its own copy of the target.
 * There's no contention for targets. Target object creation is kept to a minimum
 * on the running server.
 *
 * <p>Application code is written as to a normal pool; callers can't assume they
 * will be dealing with the same instance in invocations in different threads.
 * However, state can be relied on during the operations of a single thread:
 * for example, if one caller makes repeated calls on the AOP proxy.
 *
 * <p>Cleanup of thread-bound objects is performed on BeanFactory destruction,
 * calling their {@code DisposableBean.destroy()} method if available.
 * Be aware that many thread-bound objects can be around until the application
 * actually shuts down.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see ThreadLocalTargetSourceStats
 * @see org.springframework.beans.factory.DisposableBean#destroy()
 */
@SuppressWarnings("serial")
public class ThreadLocalTargetSource extends AbstractPrototypeBasedTargetSource
		implements ThreadLocalTargetSourceStats, DisposableBean {

	/**
	 * ThreadLocal holding the target associated with the current
	 * thread. Unlike most ThreadLocals, which are static, this variable
	 * is meant to be per thread per instance of the ThreadLocalTargetSource class.
	 */
	private final ThreadLocal<Object> targetInThread =
			new NamedThreadLocal<>("Thread-local instance of bean '" + getTargetBeanName() + "'");

	/**
	 * Set of managed targets, enabling us to keep track of the targets we've created.
	 */
	private final Set<Object> targetSet = new HashSet<>();

	private int invocationCount;

	private int hitCount;


	/**
	 * Implementation of abstract getTarget() method.
	 * We look for a target held in a ThreadLocal. If we don't find one,
	 * we create one and bind it to the thread. No synchronization is required.
	 */
	@Override
	public Object getTarget() throws BeansException {
		++this.invocationCount;
		Object target = this.targetInThread.get();
		if (target == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No target for prototype '" + getTargetBeanName() + "' bound to thread: " +
						"creating one and binding it to thread '" + Thread.currentThread().getName() + "'");
			}
			// Associate target with ThreadLocal.
			target = newPrototypeInstance();
			this.targetInThread.set(target);
			synchronized (this.targetSet) {
				this.targetSet.add(target);
			}
		}
		else {
			++this.hitCount;
		}
		return target;
	}

	/**
	 * Dispose of targets if necessary; clear ThreadLocal.
	 * @see #destroyPrototypeInstance
	 */
	@Override
	public void destroy() {
		logger.debug("Destroying ThreadLocalTargetSource bindings");
		synchronized (this.targetSet) {
			for (Object target : this.targetSet) {
				destroyPrototypeInstance(target);
			}
			this.targetSet.clear();
		}
		// Clear ThreadLocal, just in case.
		this.targetInThread.remove();
	}


	@Override
	public int getInvocationCount() {
		return this.invocationCount;
	}

	@Override
	public int getHitCount() {
		return this.hitCount;
	}

	@Override
	public int getObjectCount() {
		synchronized (this.targetSet) {
			return this.targetSet.size();
		}
	}


	/**
	 * Return an introduction advisor mixin that allows the AOP proxy to be
	 * cast to ThreadLocalInvokerStats.
	 */
	public IntroductionAdvisor getStatsMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, ThreadLocalTargetSourceStats.class);
	}

}
