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

package org.springframework.aop;

/**
 * org.springframework.aop.PointcutAdvisor ,代表具有切点的切面，包括Advice和Pointcut两个类，这样就可以通过类、
 * 方法名以及方位等信息灵活的定义切面的连接点，提供更具实用性的切面。PointcutAdvisor主要有6个具体的实现类：
 *
 * 1. DefaultPointcutAdvisor：最常用的切面类型，它可以通过任意Pointcut和Advice定义一个切面，唯一不支持的就是引介的切面类型，
 * 一般可以通过扩展该类实现自定义的切面
 * 2. NameMatchMethodPointcutAdvisor：通过该类可以定义按方法名定义切点的切面
 * 3. AspectJExpressionPointcutAdvisor：用于AspectJ切点表达式定义切点的切面
 * 4. StaticMethodMatcherPointcutAdvisor：静态方法匹配器切点定义的切面，默认情况下匹配所有的的目标类
 * 5. AspectJPointcutAdvisor：用于AspectJ语法定义切点的切面
 * 6. 引介切面IntroductionAdvisor org.springframework.aop.IntroductionAdvisor代表引介切面，
 * 引介切面是对应引介增强的特殊的切面，它应用于类层上面，所以引介切点使用ClassFilter进行定义。
 *
 * 作者：闲来也无事
 * 链接：https://www.jianshu.com/p/320f6fe39f4b
 * 来源：简书
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 *
 * 链接：https://www.jianshu.com/p/320f6fe39f4b
 * Superinterface for all Advisors that are driven by a pointcut.
 * This covers nearly all advisors except introduction advisors,
 * for which method-level matching doesn't apply.
 *
 * @author Rod Johnson
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * Get the Pointcut that drives this advisor.
	 */
	Pointcut getPointcut();

}
