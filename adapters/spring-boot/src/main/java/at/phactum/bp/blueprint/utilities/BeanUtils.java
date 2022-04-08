package at.phactum.bp.blueprint.utilities;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

public class BeanUtils {
    
    public static Class<?> targetClass(
            final Object bean) {
        
        final var proxyClass = bean.getClass();
        final var result = AopUtils.getTargetClass(bean);
        if (result != proxyClass) {
            return result;
        }
        return ClassUtils.getUserClass(bean);
        
    }

}
