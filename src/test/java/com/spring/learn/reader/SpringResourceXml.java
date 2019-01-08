package com.spring.learn.reader;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author bogle
 * @version 1.0 2019/1/8 下午2:31
 */
public class SpringResourceXml {

    //spring 的加载流程
    @Test
    public void springTest() {
        //根据xml配置文件创建Resource资源对象，该对象中包含了BeanDefinition的信息
        ClassPathResource resource = new ClassPathResource("learn/application-context.xml");

        //创建DefaultListableBeanFactory
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        //创建XmlBeanDefinitionReader读取器，用于载入BeanDefinition.
        //之所以需要BeanFactory作为参数，是因为会将读取的信息回调配置给factory
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);

        //XmlBeanDefinitionReader执行载入BeanDefinition的方法，最后会完成Bean的载入和注册。
        //完成后bean就成功的放置到IOC容器中，以后我们就可以从中取得Bean来使用
        reader.loadBeanDefinitions(resource);

        //根据工厂获取bean信息
        UserService bean = factory.getBean(UserService.class);
        bean.println();
    }

}
