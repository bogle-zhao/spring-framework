package com.spring.learn.property;

import org.junit.Test;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.ResourceUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author bogle
 * @version 1.0 2019/1/8 下午5:42
 */
public class ProperPlaceholderHelperTest {

	@Test
	public void testPlace() throws Exception{
		String a = "{name}{age}{sex}";
		String b = "{name{age}{sex}}";
		PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("{", "}");
		InputStream
				in = new BufferedInputStream(new FileInputStream(ResourceUtils.getFile("classpath:test01.properties")));
		;
		Properties properties = new Properties();
		properties.load(in);

		System.out.println("替换前:" + a);
		System.out.println("替换后:" + propertyPlaceholderHelper.replacePlaceholders(a, new PropertyPlaceholderHelper.PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String placeholderName) {
				String value = properties.getProperty(placeholderName);
				return value;
			}
		}));
		System.out.println("====================================================");
		System.out.println("替换前:" + b);
		System.out.println("替换后:" + propertyPlaceholderHelper.replacePlaceholders(b, new PropertyPlaceholderHelper.PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String placeholderName) {
				String value = properties.getProperty(placeholderName);
				return value;
			}
		}));
	}
}
