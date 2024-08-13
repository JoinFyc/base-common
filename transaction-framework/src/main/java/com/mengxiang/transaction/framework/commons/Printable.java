package com.mengxiang.transaction.framework.commons;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @description 打印基础类
 * @author JoinFyc
 * @date 2020年11月4日
 *
 */
public abstract class Printable implements Serializable {

	private static final long serialVersionUID = 6066611493519757245L;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
