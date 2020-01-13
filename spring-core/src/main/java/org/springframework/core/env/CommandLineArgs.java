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

package org.springframework.core.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;

/**
 * 概述
 * Spring对应用程序运行的命令行参数进行了抽象，这个抽象是类CommandLineArgs。
 *
 * CommandLineArgs类将命令行参数分为两类：
 *
 * option 参数
 * 		以 --开头
 * 		可以认为是name/value对参数
 * 		例子 : --foo, --foo=bar
 * 非 option 参数
 * 		不以 --开头
 * 		可以认为是只提供了value的参数(具体怎么理解这个值，看业务逻辑的需求)
 * ————————————————
 * 原文链接：https://blog.csdn.net/andy_zhang2007/article/details/84327104
 * 相关链接：https://blog.csdn.net/antchen88/article/details/56673291
 */

/**
 * 命令行参数的简单表示形式，分为“选项参数”和“非选项参数”。
 * A simple representation of command line arguments, broken into "option arguments" and "non-option arguments".
 *
 * @author Chris Beams
 * @since 3.1
 * @see SimpleCommandLineArgsParser
 */
class CommandLineArgs {

	private final Map<String, List<String>> optionArgs = new HashMap<>();
	private final List<String> nonOptionArgs = new ArrayList<>();

	/**
	 * 添加option参数,例如 --foo, --foo=bar 这样的参数
	 * 同名option参数可以有多个值，比如同时提供了 --id=1 --id=2 这样的参数
	 * Add an option argument for the given option name and add the given value to the
	 * list of values associated with this option (of which there may be zero or more).
	 * The given value may be {@code null}, indicating that the option was specified
	 * without an associated value (e.g. "--foo" vs. "--foo=bar").
	 */
	public void addOptionArg(String optionName, @Nullable String optionValue) {
		if (!this.optionArgs.containsKey(optionName)) {
			this.optionArgs.put(optionName, new ArrayList<>());
		}
		if (optionValue != null) {
			this.optionArgs.get(optionName).add(optionValue);
		}
	}

	/**
	 * 获取所有option参数的名称列表
	 * Return the set of all option arguments present on the command line.
	 */
	public Set<String> getOptionNames() {
		return Collections.unmodifiableSet(this.optionArgs.keySet());
	}

	/**
	 * 是否包含某个指定名称的option参数
	 * Return whether the option with the given name was present on the command line.
	 */
	public boolean containsOption(String optionName) {
		return this.optionArgs.containsKey(optionName);
	}

	/**
	 * 获取指定名称的option参数的值，如果命令行中没有这个option参数，返回null；
	 * 因为一个option参数可能会被指定多个值，所以返回的是一个列表
	 * Return the list of values associated with the given option. {@code null} signifies
	 * that the option was not present; empty list signifies that no values were associated
	 * with this option.
	 */
	@Nullable
	public List<String> getOptionValues(String optionName) {
		return this.optionArgs.get(optionName);
	}

	/**
	 * 将给定值添加到非选项参数列表中。
	 * Add the given value to the list of non-option arguments.
	 */
	public void addNonOptionArg(String value) {
		this.nonOptionArgs.add(value);
	}

	/**
	 * 返回在命令行上指定的非选项参数的列表。
	 * Return the list of non-option arguments specified on the command line.
	 */
	public List<String> getNonOptionArgs() {
		return Collections.unmodifiableList(this.nonOptionArgs);
	}

}
