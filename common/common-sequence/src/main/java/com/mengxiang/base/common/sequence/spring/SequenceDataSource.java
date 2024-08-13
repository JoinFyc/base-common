package com.mengxiang.base.common.sequence.spring;

import java.lang.annotation.*;

/**
 * @author JoinFyc
 * @date 2020/12/18 13:50
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SequenceDataSource {
}
