package org.springframework.cloud.netflix.feign;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author fyc
 * @date 2020/11/3
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignClient {

    /**
     * The name of the service with optional protocol prefix. Synonym for {@link #name()
     * name}. A name must be specified for all clients, whether or not a url is provided.
     * Can be specified as property key, eg: ${propertyKey}.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The service id with optional protocol prefix. Synonym for {@link #value() value}.
     *
     * @deprecated use {@link #name() name} instead
     */
    @Deprecated
    String serviceId() default "";

    /**
     * The service id with optional protocol prefix. Synonym for {@link #value() value}.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * Sets the <code>@Qualifier</code> value for the feign client.
     */
    String qualifier() default "";

    /**
     * An absolute URL or resolvable hostname (the protocol is optional).
     */
    String url() default "";

    /**
     * Whether 404s should be decoded instead of throwing FeignExceptions
     */
    boolean decode404() default false;

    Class<?>[] configuration() default {};

    /**
     * Fallback class for the specified Feign client interface. The fallback class must
     * implement the interface annotated by this annotation and be a valid spring bean.
     */
    Class<?> fallback() default void.class;

    Class<?> fallbackFactory() default void.class;

    /**
     * Path prefix to be used by all method-level mappings. Can be used with or without
     * <code>@RibbonClient</code>.
     */
    String path() default "";

    /**
     * Whether to mark the feign proxy as a primary bean. Defaults to true.
     */
    boolean primary() default true;

}
