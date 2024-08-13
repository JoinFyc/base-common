package com.mengxiang.base.common.sequence.exception;

/**
 * @author JoinFyc
 */
public class SequenceException extends RuntimeException {

    private static final long serialVersionUID = 6988667235936181690L;

    public SequenceException() {
        super();
    }

    public SequenceException(String message) {
        super(message);
    }

    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceException(Throwable cause) {
        super(cause);
    }
}
