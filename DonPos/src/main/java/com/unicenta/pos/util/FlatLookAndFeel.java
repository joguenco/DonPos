package com.unicenta.pos.util;

import com.formdev.flatlaf.FlatLaf;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class FlatLookAndFeel {


    public static Map<String, String> getLafs(){
        Map<String, String> lafs = new TreeMap<>();
        Set<Class> classes = findAllClassesUsingReflectionsLibrary("com.formdev.flatlaf");
        classes.forEach((Class name) -> {
            try {
                Field[] declaredFields = name.getDeclaredFields();
                for (Field field: declaredFields) {
                    if (field.getName().equals("NAME")) {
                        lafs.put((String)field.get(null), name.getCanonicalName());
                    }
                }
            } catch (Exception e) {
                log.error("Error getting Flat Look and Feel "+e.getMessage());
            }
        });


        return lafs;
    }

    private static Set<Class> findAllClassesUsingReflectionsLibrary(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        return reflections.getSubTypesOf(FlatLaf.class)
                .stream()
                .collect(Collectors.toSet());
    }
}
