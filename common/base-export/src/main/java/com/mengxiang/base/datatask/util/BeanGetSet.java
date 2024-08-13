package com.mengxiang.base.datatask.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


public class BeanGetSet {
    static Logger log = LoggerFactory.getLogger(BeanGetSet.class);

    private List<String> fieldNames = new ArrayList<>();
    private Map<String, Method> methodsMap = new HashMap<>();
    private Map<String, String> getMethodStringsMap = new HashMap<>();
    private Map<String, String> setMethodStringsMap = new HashMap<>();

    private boolean mapType = false;

    public List<String> fieldNames() {
        return fieldNames;
    }

    public BeanGetSet(Class clazz) {

        if(
           clazz.equals(Map.class) ||
           clazz.equals(HashMap.class) ||
           clazz.equals(TreeMap.class) ||
           clazz.equals(Hashtable.class) ||
           clazz.equals(SortedMap.class)
        ) {
            mapType = true;
        } else {
            Field[] fields = clazz.getDeclaredFields();
            if(null != fields && fields.length>0) {
                for (Field f:fields) {
                    fieldNames.add(f.getName());

                    getMethodStringsMap.put(
                            f.getName(),
                            "get" + Character.toUpperCase(f.getName().charAt(0))+f.getName().substring(1)
                    )
                    ;
                    setMethodStringsMap.put(
                            f.getName(),
                            "set" + Character.toUpperCase(f.getName().charAt(0))+f.getName().substring(1)
                    )
                    ;

                }
            }
            Method[] methods = clazz.getDeclaredMethods();
            if(null != methods && methods.length>0) {
                for (Method m:methods) {
                    methodsMap.put(m.getName(), m);
                }
            }
        }


    }

    public Object get(Object o, String fieldName) {

        if(mapType) {
            Map obj = (Map)o;
            Object rst = null;
            try {
                rst = obj.get(fieldName);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return rst;
        }

        Method m = methodsMap.get(
                getMethodStringsMap.get(fieldName)
        );
        if(null != m) {
            try {
                return m.invoke(o,null);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public void set(Object o,String fieldName,String newValue) {


        if(mapType) {
            Map obj = (Map)o;

            try {
                obj.put(fieldName, newValue);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        Method m = methodsMap.get(
                setMethodStringsMap.get(fieldName)
        );
        if(null != m) {
            try {
                m.invoke(o,newValue);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
