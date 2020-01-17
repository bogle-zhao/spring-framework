/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * 用于创建代理的配置的便利超类，以确保所有代理创建者具有一致的属性。
 *
 * proxyTargetClass，true代表直接代理类，false代表代理接口。默认为false
 * optimize，是否执行某些优化，感觉基本没怎么用到
 * opaque，代表子类是否能被转换为Advised接口，默认为false，表示可以
 * exposeProxy，是否暴露代理，也就是是否把当前代理对象绑定到AopContext的ThreadLocal属性currentProxy上去，常用于代理类里面的代理方法需要调用同类里面另外一个代理方法的场景。
 * frozen，当前代理配置是否被冻结，如果被冻结，配置将不能被修改
 *
 * 链接：https://www.jianshu.com/p/1f8dbeadd79d
 * 参考：https://www.jianshu.com/p/b38b1a8cb0a4
 *
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = -8409359707199703185L;


	// 标记是否直接对目标类进行代理，而不是通过接口产生代理
	private boolean proxyTargetClass = false;

	// 标记是否对代理进行优化。启动优化通常意味着在代理对象被创建后，增强的修改将不会生效，因此默认值为false。
	// 如果exposeProxy设置为true，即使optimize为true也会被忽略。
	private boolean optimize = false;

	// 标记是否需要阻止通过该配置创建的代理对象转换为Advised类型，默认值为false，表示代理对象可以被转换为Advised类型
	//Advised接口其实就代表了被代理的对象（此接口是Spring AOP提供，它提供了方法可以对代理进行操作，比如移除一个切面之类的），它持有了代理对象的一些属性，通过它可以对生成的代理对象的一些属性进行人为干预
	// 默认情况，我们可以这么完 Advised target = (Advised) context.getBean("opaqueTest"); 从而就可以对该代理持有的一些属性进行干预勒   若此值为true，就不能这么玩了
	boolean opaque = false;

	// 标记代理对象是否应该被aop框架通过AopContext以ThreadLocal的形式暴露出去。
	// 当一个代理对象需要调用它自己的另外一个代理方法时，这个属性将非常有用。默认是是false，以避免不必要的拦截。
	boolean exposeProxy = false;

	// 标记该配置是否需要被冻结，如果被冻结，将不可以修改增强的配置。
	// 当我们不希望调用方修改转换成Advised对象之后的代理对象时，这个配置将非常有用。
	private boolean frozen = false;


	/**
	 * 设置是否直接代理目标类，而不是仅代理特定的接口。默认值为“ false”。
	 *
	 * 将此设置为“ true”可强制代理TargetSource的公开目标类。如果该目标类是接口，
	 * 则将为给定接口创建一个JDK代理。如果该目标类是任何其他类，则将为给定类创建CGLIB代理。
	 *
	 * 注意：根据具体代理工厂的配置，如果未指定接口（并且未激活接口自动检测），则也将应用代理目标类行为。
	 *
	 * Set whether to proxy the target class directly, instead of just proxying
	 * specific interfaces. Default is "false".
	 * <p>Set this to "true" to force proxying for the TargetSource's exposed
	 * target class. If that target class is an interface, a JDK proxy will be
	 * created for the given interface. If that target class is any other class,
	 * a CGLIB proxy will be created for the given class.
	 * <p>Note: Depending on the configuration of the concrete proxy factory,
	 * the proxy-target-class behavior will also be applied if no interfaces
	 * have been specified (and no interface autodetection is activated).
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * 返回是否直接代理目标类以及任何接口。
	 * Return whether to proxy the target class directly as well as any interfaces.
	 */
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * 设置是否进行代理优化，代理之间“积极优化”的确切含义会有所不同，但通常会有一些权衡。默认值为“ false”。
	 * Set whether proxies should perform aggressive optimizations.
	 * The exact meaning of "aggressive optimizations" will differ
	 * between proxies, but there is usually some tradeoff.
	 * Default is "false".
	 *
	 * 例如，优化通常意味着在创建代理后建议更改将不会生效。
	 * 因此，默认情况下禁用优化。如果其他设置无法进行优化，
	 * 则可以忽略优化值“ true”：例如，如果“ exposeProxy”设置为“ true”，
	 * 并且与优化不兼容。
	 * <p>For example, optimization will usually mean that advice changes won't
	 * take effect after a proxy has been created. For this reason, optimization
	 * is disabled by default. An optimize value of "true" may be ignored
	 * if other settings preclude optimization: for example, if "exposeProxy"
	 * is set to "true" and that's not compatible with the optimization.
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * Return whether proxies should perform aggressive optimizations.
	 */
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * 设置是否应防止将此配置创建的代理强制转换Advised为查询代理状态。
	 * 默认值为“ false”，表示任何AOP代理都可以转换为 Advised。
	 *
	 * Set whether proxies created by this configuration should be prevented
	 * from being cast to {@link Advised} to query proxy status.
	 * <p>Default is "false", meaning that any AOP proxy can be cast to
	 * {@link Advised}.
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * Return whether proxies created by this configuration should be
	 * prevented from being cast to {@link Advised}.
	 */
	public boolean isOpaque() {
		return this.opaque;
	}

	/**
	 * 设置代理是否应由AOP框架公开为ThreadLocal以便通过AopContext类进行检索。如果建议对象需要自己调用另一个建议方法，这将很有用。（如果使用this，则不建议调用）。
	 * 默认值为“ false”，以避免不必要的额外拦截。这意味着，不能保证AopContext访问将在建议对象的任何方法中都能一致地工作。
	 *
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses {@code this}, the invocation will not be advised).
	 * <p>Default is "false", in order to avoid unnecessary extra interception.
	 * This means that no guarantees are provided that AopContext access will
	 * work consistently within any method of the advised object.
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * Return whether the AOP proxy will expose the AOP proxy for
	 * each invocation.
	 */
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * 设置是否应冻结此配置。
	 * 冻结配置后，将无法更改建议。这对于优化很有用，在我们不希望调用者在转换为Advised之后能够操纵配置时有用。
	 * Set whether this config should be frozen.
	 * <p>When a config is frozen, no advice changes can be made. This is
	 * useful for optimization, and useful when we don't want callers to
	 * be able to manipulate configuration after casting to Advised.
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * Return whether the config is frozen, and no advice changes can be made.
	 */
	public boolean isFrozen() {
		return this.frozen;
	}


	/**
	 * 从另一个配置对象复制配置。
	 *
	 * Copy configuration from the other config object.
	 * @param other object to copy configuration from
	 */
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}
