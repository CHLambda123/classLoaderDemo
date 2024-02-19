package com.lambda.classloaderdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MyClassLoader extends ClassLoader {
    private static ThreadLocal<URL[]> threadLocal = new ThreadLocal<>();
    private URL[] allUrl;

    /**
     * 构造器1
     * @param paths
     */
    public MyClassLoader(String[] paths) {
        this(paths, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 构造器2
     * @param paths
     * @param parent
     */
    public MyClassLoader(String[] paths, ClassLoader parent) {
        super(parent);
        MyClassLoader.getURLs(paths);
        allUrl = threadLocal.get();
    }


    /**
     * 获取paths路径下的所有jar包的URL集合
     * @param paths
     * @return
     */
    private static void getURLs(String[] paths) {
        if (null == paths || 0 == paths.length) {
            throw  new RuntimeException("jar包路径不能为空");
        }
        List<String> allJarPaths = new ArrayList<>();
        for (String path : paths) {
            MyClassLoader.collectJars(path, allJarPaths);
        }
        ArrayList<URL> urls = new ArrayList<>();
        for (String allJarPath : allJarPaths) {
            urls.add(MyClassLoader.doGetURLs(allJarPath));
        }
        URL[] threadLocalURLs = urls.toArray(new URL[0]);
        threadLocal.set(threadLocalURLs);
    }

    /**
     * 递归获取path路径下的所有jar包的绝对路径，放到 allJarPaths 中
     * @param path
     * @param allJarPaths
     */
    private static void collectJars(String path, List<String> allJarPaths) {
        if (null == path || path.isEmpty() || !new File(path).exists()) {
            return;
        }
        File current = new File(path);
        if (!current.isDirectory()) {
            if (path.endsWith(".jar")) {
                allJarPaths.add(current.getAbsolutePath());
            }
            return;
        }
        for (File child : Optional.ofNullable(current.listFiles()).orElse(new File[0])) {
            if (!child.isDirectory()) {
                if (child.getName().endsWith(".jar")) {
                    allJarPaths.add(child.getAbsolutePath());
                }
                continue;
            }
            collectJars(child.getAbsolutePath(), allJarPaths);
        }
    }

    /**
     * 将jar包String路径转换成URL
     * @param jarPath
     * @return
     */
    private static URL doGetURLs(final String jarPath) {
        if (null == jarPath || jarPath.isEmpty()) {
            throw new RuntimeException("jar包路径不能为空");
        }
        File jarFile = new File(jarPath);
        if (!jarPath.endsWith(".jar") || !jarFile.exists() || jarFile.isDirectory()) {
            throw new RuntimeException("传递的不是jar包");
        }
        try {
            return jarFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("系统加载jar包出错", e);
        }
    }
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }

    /**
     * 重写loadClass方法
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            Class<?> clazz = findLoadedClass(className);
            if (clazz != null) {
                return clazz;
            }
            if (allUrl != null) {
                String classPath = className.replaceAll("\\.", "/");
                classPath = classPath.concat(".class");
                for (URL url : allUrl) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JarFile jarFile = null;
                    InputStream is = null;
                    try {
                        File file = new File(url.toURI());
                        if (file.exists()) {
                            jarFile = new JarFile(file);
                            JarEntry jarEntry = jarFile.getJarEntry(classPath);
                            if (jarEntry != null) {
                                is = jarFile.getInputStream(jarEntry);
                                int c = 0;
                                while (-1 != (c = is.read())) {
                                    baos.write(c);
                                }
                                byte[] data = baos.toByteArray();
                                return defineClass(className, data, 0, data.length);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            if (jarFile != null) {
                                jarFile.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (resolve) {
                super.resolveClass(clazz);
            }
            return super.loadClass(className, resolve);
        }
    }
}
