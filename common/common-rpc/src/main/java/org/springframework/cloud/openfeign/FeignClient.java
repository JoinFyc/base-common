package org.springframework.cloud.openfeign;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author fyc
 * @date 2020/11/3
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FeignClient {

    /**
     * The name of the service with optional protocol prefix. Synonym for {@link #name()
     * name}. A name must be specified for all clients, whether or not a url is provided.
     * Can be specified as property key, eg: ${propertyKey}.
     *
     * @return the name of the service with optional protocol prefix
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The service id with optional protocol prefix. Synonym for {@link #value() value}.
     *
     * @return the service id with optional protocol prefix
     * @deprecated use {@link #name() name} instead
     */
    @Deprecated
    String serviceId() default "";

    /**
     * This will be used as the bean name instead of name if present, but will not be used
     * as a service id.
     *
     * @return bean name instead of name if present
     */
    String contextId() default "";

    /**
     * @return The service id with optional protocol prefix. Synonym for {@link #value()
     * value}.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * @return the <code>@Qualifier</code> value for the feign client.
     */
    String qualifier() default "";

    /**
     * @return an absolute URL or resolvable hostname (the protocol is optional).
     */
    String url() default "";

    /**
     * @return whether 404s should be decoded instead of throwing FeignExceptions
     */
    boolean decode404() default false;

    Class<?>[] configuration() default {};

    /**
     * Fallback class for the specified Feign client interface. The fallback class must
     * implement the interface annotated by this annotation and be a valid spring bean.
     *
     * @return fallback class for the specified Feign client interface
     */
    Class<?> fallback() default void.class;

    Class<?> fallbackFactory() default void.class;

    String path() default "";

    /**
     * @return whether to mark the feign proxy as a primary bean. Defaults to true.
     */
    boolean primary() default true;

}
