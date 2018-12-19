package com.aihuishou.gateway.logging.util;

import org.springframework.core.NamedInheritableThreadLocal;

public abstract class LoggingContextHolder {

    private static final NamedInheritableThreadLocal<Long> REQUEST_TIME = new NamedInheritableThreadLocal<Long>("Request Time") {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    public static void setRequesTtime(Long requestTime) {
        REQUEST_TIME.set(requestTime);
    }

    public static Long getRequesTtime() {
        return REQUEST_TIME.get();
    }

    public static void reset() {
        REQUEST_TIME.remove();
    }


}
