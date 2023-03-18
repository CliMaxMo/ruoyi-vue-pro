package cn.iocoder.yudao.module.mes.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;


import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: Test
 * @Author cmw
 * @Date: 2023/3/6 15:20
 * @describe
 */
@AnnotationTest.Name(value = 12,name = "hy")
public class AnnotationTest {

    @AnnotationTest.Name(value = 13,name = "hy2")
    public  void Test1(){


    }
    @AnnotationTest.Name(value = 12,name = "hy3")
    public  void Test2(){


    }
    @Test
    public void Annotation(){
        Map<String, Class> handlerMap = new HashMap<String, Class>();

        //spring工具类，可以获取指定路径下的全部类
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath("cn.iocoder.yudao.module.mes.service") + "/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            //MetadataReader 的工厂类
            MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (Resource resource : resources) {
                //用于读取类信息
                MetadataReader reader = readerfactory.getMetadataReader(resource);
                //扫描到的class
                String classname = reader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(classname);
                //判断是否有指定主解

                Method[] methods=clazz.getMethods();

                for (Method method:methods){
                    AnnotationTest.Name anno = method.getAnnotation(AnnotationTest.Name.class);
                    if (anno != null) {
                        //将注解中的类型值作为key，对应的类作为 value
                        handlerMap.put(classname, clazz);
                    }

                }

            }
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    @Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE,ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Name{

        String name() default "cmw";
        int value() default 16;

    }
}
