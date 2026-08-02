package net.furizon.backend.infrastructure.logging;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogCallAspect {
    private static final String APPEND = " (LOG_CALL)";

    private static final String ANN = "net.furizon.backend.infrastructure.logging.LogCall";

    private static final Map<Method, String> signatures = new ConcurrentHashMap<>();

    @Around(
            "(@annotation(" + ANN + ") || @within(" + ANN + "))"
                    + " && execution(* *(..))"
                    + " && !execution(String *.toString())"
                    + " && !execution(int *.hashCode())"
                    + " && !execution(boolean *.equals(Object))"
                    + " && !execution(boolean *.canEqual(Object))"
    )
    public Object around(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();
        final Class<?> target = point.getTarget() == null
                              ? method.getDeclaringClass()
                              : AopUtils.getTargetClass(point.getTarget());

        final Logger log = LoggerFactory.getLogger(target);
        if (!log.isDebugEnabled()) {
            return point.proceed();
        }

        final String sig = signatures.computeIfAbsent(method, LogCallAspect::signature);
        log.debug("#{}: called", sig);
        final long start = System.nanoTime();
        try {
            final Object out = point.proceed();
            log.debug("#{}: returned in {}ms" + APPEND, sig, printTime(start));
            return out;
        } catch (final Throwable thrown) {
            final String className = thrown.getClass().getSimpleName();
            log.debug("#{}: thrown {} in {}ms" + APPEND, sig, className, printTime(start));
            throw thrown;
        }
    }

    private static String printTime(long startTime) {
        long elapsed = System.nanoTime() - startTime;
        return String.format("%d.%d", elapsed / 1_000_000L, (elapsed % 1_000_000L) / 1_000L);
    }

    private static String signature(final Method method) {
        final StringBuilder sig = new StringBuilder(method.getName()).append('(');
        final Class<?>[] params = method.getParameterTypes();
        for (int idx = 0; idx < params.length; ++idx) {
            if (idx > 0) {
                sig.append(", ");
            }
            sig.append(params[idx].getSimpleName());
        }
        return sig.append(')').toString();
    }
}