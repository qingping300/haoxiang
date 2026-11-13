package com.haoxiang.mall.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回结果类
 */
public class R {

    private int code;
    private String msg;
    private Map<String, Object> map = new HashMap<>();

    /**
     * 请求成功
     * @return
     */
    public static R success() {
        R r = new R();
        r.code = 200;
        r.msg = "请求成功";
        return r;
    }

    /**
     * 请求结果
     * @param result
     * @return
     */
    public static R result(boolean result) {
        return result ? success() : error();
    }

    /**
     * 请求结果
     * @param result
     * @param errorMessage
     * @return
     */
    public static R result(boolean result, String errorMessage) {
        return result ? success() : error(errorMessage);
    }

    /**
     * 请求成功
     * @param msg
     * @return
     */

    public static R success(String msg) {
        R r = new R();
        r.code = 200;
        r.msg = msg;
        return r;
    }

    /**
     * 请求失败
     * @return
     */
    public static R error() {
        R r = new R();
        r.code = 500;
        r.msg = "请求失败";
        return r;
    }

    /**
     * 请求失败
     * @param msg
     * @return
     */
    public static R error(String msg) {
        R r = new R();
        r.code = 500;
        r.msg = msg;
        return r;
    }

    /**
     * 添加数据
     * @param key
     * @param value
     * @return
     */
    public R add(String key, Object value) {
        map.put(key, value);
        return this;
    }

    /**
     * 获取状态码
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置状态码
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取提示信息
     * @return
     */
    public String getMsg() {
        return msg;
    }
    /**
     * 设置提示信息
     * @param msg
     */

    public void setMsg(String msg) {
        this.msg = msg;
    }
    /**
     * 获取数据
     * @return
     */

    public Map<String, Object> getMap() {
        return map;
    }
    /**
     * 设置数据
     * @param map
     */

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * 重写toString方法
     * @return
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("msg", msg)
                .append("map", map)
                .toString();
    }
}
