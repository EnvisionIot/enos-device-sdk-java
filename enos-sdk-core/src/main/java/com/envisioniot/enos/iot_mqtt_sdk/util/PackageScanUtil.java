package com.envisioniot.enos.iot_mqtt_sdk.util;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhensheng.cai
 * @date 2018/7/31.
 */
public class PackageScanUtil {
    public static void main(String[] args) throws Exception {
        String packageName = "com.envisioniot.enos.iot_mqtt_sdk";
        scan(packageName, IMqttArrivedMessage.class);
    }

    public static List<Class<?>> scan(String pkg, Class<?> filter) throws ClassNotFoundException {
        List<Class<?>> ret = new ArrayList<>();
        List<String> classNames = getClassName(pkg, true);
        if (classNames != null) {
            for (String className : classNames) {
                Class<?> cls = Class.forName(className);

                if (Modifier.isAbstract(cls.getModifiers())) {
                    continue;
                }

                if (filter.isAssignableFrom(cls)) {
                    ret.add(cls);
                }
            }
        }

        return ret;
    }

    /**
     * Get all classes under a package (including all child-packages of the package)
     *
     * @param packageName 
     * @return The full name of the class
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * Get all classes in a package
     *
     * @param packageName  
     * @param childPackage Whether to traverse child-packages
     * @return The full name of the class
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String urlPath;
            try {
                urlPath = URLDecoder.decode(url.getPath(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return fileNames;
            }
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNames = getClassNameByFile(urlPath, null, childPackage);
            } else if (type.equals("jar")) {
                fileNames = getClassNameByJar(urlPath, childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * Get all classes in a package from the project file
     *
     * @param filePath     
     * @param className    
     * @param childPackage Whether to traverse child packages
     * @return The full name of the class
     */
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf(File.separator + "classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace(File.separator, ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * Get all classes in a package from jar
     *
     * @param jarPath      jar file path
     * @param childPackage whether to traverse child-packages
     * @return The full name of a class
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            try {
                Enumeration<JarEntry> entrys = jarFile.entries();
                while (entrys.hasMoreElements()) {
                    JarEntry jarEntry = entrys.nextElement();
                    String entryName = jarEntry.getName();
                    if (entryName.endsWith(".class")) {
                        if (childPackage) {
                            if (entryName.startsWith(packagePath)) {
                                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                myClassName.add(entryName);
                            }
                        } else {
                            int index = entryName.lastIndexOf("/");
                            String myPackagePath;
                            if (index != -1) {
                                myPackagePath = entryName.substring(0, index);
                            } else {
                                myPackagePath = entryName;
                            }
                            if (myPackagePath.equals(packagePath)) {
                                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                myClassName.add(entryName);
                            }
                        }
                    }
                }
            } finally {
                jarFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * Search the package from all jars and get all the classes under the package
     *
     * @param urls         URLs
     * @param packagePath  
     * @param childPackage whether to traverse child packages
     * @return The full name of a class
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // No need to search the classes folder
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }

}
