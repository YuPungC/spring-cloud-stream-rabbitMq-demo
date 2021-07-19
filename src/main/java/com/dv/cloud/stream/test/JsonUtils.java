package com.dv.cloud.stream.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Json工具类
 */
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //设置如果多传了实体类没有的字段，则不会抛异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * @param value
     * @return
     * @ref ObjectMapper
     * 此方法将指定的对象序列化为JSON表示的等价
     */
    public static String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转为json失败", e);
        }
    }

    /**
     * @param json
     * @param classOfT
     * @return
     * @ref ObjectMapper
     * 此方法将指定的JSON为指定类型的对象。
     * 如果指定的对象是泛型类型，该方法是有用的。
     */
    public static <T> T readValue(String json, Class<T> classOfT) {
        try {
            return objectMapper.readValue(json, classOfT);
        } catch (IOException e) {
            throw new RuntimeException("json转对象失败", e);
        }
    }

    /**
     * @param json
     * @param type
     * @return
     * @ref ObjectMapper
     * 此方法将指定的JSON为指定类型的对象。
     * 如果指定的对象是泛型类型，该方法是有用的。
     */
    public static <T> T readValue(String json, Type type) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(type);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("json转对象失败", e);
        }
    }

    /**
     * @param json
     * @param javaType
     * @return
     * @ref ObjectMapper
     * 此方法将指定的JSON为指定类型的对象。
     * 如果指定的对象是泛型类型，该方法是有用的。
     */
    public static <T> T readValue(String json, JavaType javaType) {
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("json转对象失败", e);
        }
    }

    /**
     * @param json
     * @param type
     * @return
     * @ref ObjectMapper
     * 此方法将指定的JSON为指定类型的对象。
     * 如果指定的对象是泛型类型，该方法是有用的。
     */
    public static <T> T readValue(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("json转对象失败", e);
        }
    }

}
