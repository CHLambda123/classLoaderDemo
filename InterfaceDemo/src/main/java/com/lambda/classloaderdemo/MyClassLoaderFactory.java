package com.lambda.classloaderdemo;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyClassLoaderFactory {
    static private Map<String, MyClassLoader> classLoaderMap = new ConcurrentHashMap<>();
    static public MyClassLoader getMyClassLoader(String version, String path) {
        return classLoaderMap.computeIfAbsent(version, key -> new MyClassLoader(new String[]{path}));
    }
}
