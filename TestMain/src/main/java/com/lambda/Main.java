package com.lambda;

import com.lambda.classloaderdemo.MyClassLoader;
import com.lambda.classloaderdemo.MyClassLoaderFactory;
import com.lambda.myinterface.MyInterfaceDemoWrapper;


public class Main {
    public static void main(String[] args) {

        MyClassLoader myClassLoaderA = MyClassLoaderFactory.getMyClassLoader("versionA", "pluginJar/ImplementDemoA.jar");
        MyClassLoader myClassLoaderB = MyClassLoaderFactory.getMyClassLoader("versionB", "pluginJar/ImplementDemoB.jar");
        try {

            Class<?> aClass1 = Class.forName("com.lambda.myimplement.MyImplementDemo", true, myClassLoaderA);
            Class<?> bClass1 = Class.forName("com.lambda.myimplement.MyImplementDemo", true, myClassLoaderB);
            MyInterfaceDemoWrapper myInterfaceDemoWrapperA1 = new MyInterfaceDemoWrapper(aClass1.getConstructor().newInstance());
            MyInterfaceDemoWrapper myInterfaceDemoWrapperB1 = new MyInterfaceDemoWrapper(bClass1.getConstructor().newInstance());
            myInterfaceDemoWrapperA1.sayWhoAreYou();
            myInterfaceDemoWrapperB1.sayWhoAreYou();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}