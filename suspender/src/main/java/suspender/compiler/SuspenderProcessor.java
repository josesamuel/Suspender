package suspender.compiler;


import com.google.auto.service.AutoService;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import suspender.compiler.builder.BindingManager;
import suspender.annotations.Suspender;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * AnnotationProcessor that processes the @{@link Suspender} annotations and
 * generates the Wrapper with suspend methods for those
 *
 * @author js
 */
@AutoService(Processor.class)
@IncrementalAnnotationProcessor(ISOLATING)
public class SuspenderProcessor extends AbstractProcessor {

    private BindingManager kotlinBindingManager;
    private Messager messager;


    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.messager = env.getMessager();
        kotlinBindingManager = new BindingManager(env);
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Only one annotation is supported at class level - @{@link Suspender}
     */
    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(Suspender.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Suspender.class)) {
            if (element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.CLASS) {
                kotlinBindingManager.generateProxy(element);
            }
        }
        return false;
    }
}
