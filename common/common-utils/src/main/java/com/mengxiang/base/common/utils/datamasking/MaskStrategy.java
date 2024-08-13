package com.mengxiang.base.common.utils.datamasking;

/**
 * 脱敏策略接口
 */
public interface MaskStrategy {

	String mask(String source, int[] params);
}
