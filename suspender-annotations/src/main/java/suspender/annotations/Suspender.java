package suspender.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Specify some classes that needs to be wrapper class that exposes its methods as suspended methods.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface Suspender {
    /**
     * Array of classes whose suspender wrappers needs to be generated.
     */
    Class[] classesToWrap();
}
