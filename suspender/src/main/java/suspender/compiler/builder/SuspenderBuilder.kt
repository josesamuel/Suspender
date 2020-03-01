package suspender.compiler.builder

import com.google.auto.common.MoreElements.getPackage
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

/**
 * Base class of all the builders
 */
open class SuspenderBuilder(val suspenderElement: Element, val bindingManager: BindingManager) {

    val suspenderPackageName: String = getPackage(suspenderElement).qualifiedName.toString()
    val suspenderClassName: String = suspenderElement.simpleName.toString()
    private val messager: Messager = bindingManager.getMessager()

    /**
     * Callback that visit each element to be processed
     */
    interface ElementVisitor {
        fun visitElement(classBuilder: TypeSpec.Builder, member: Element, methodIndex: Int, methodBuilder: FunSpec.Builder?)
    }


    /**
     * Logs an error
     */
    open fun logError(message: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    /**
     * Logs a warning
     */
    open fun logWarning(message: String?) {
        messager.printMessage(Diagnostic.Kind.WARNING, message)
    }

    /**
     * Logs an info
     */
    open fun logInfo(message: String?) {
        messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    /**
     * Finds that elements that needs to be processed
     */
    protected open fun processSuspenderElements(classBuilder: TypeSpec.Builder, elementVisitor: ElementVisitor, methodBuilder: FunSpec.Builder?) {
        processSuspenderElements(classBuilder, suspenderElement, 0, elementVisitor, methodBuilder)
    }

    /**
     * Recursevely Visit extended elements
     */
    internal open fun processSuspenderElements(classBuilder: TypeSpec.Builder, element: Element, methodIndex: Int, elementVisitor: ElementVisitor, methodBuilder: FunSpec.Builder?): Int {
        var methodIndex = methodIndex
        if (element is TypeElement) {
            for (typeMirror in element.interfaces) {
                if (typeMirror is DeclaredType) {
                    val superElement = typeMirror.asElement()
                    methodIndex = processSuspenderElements(classBuilder, superElement, methodIndex, elementVisitor, methodBuilder)
                }
            }
            for (member in element.getEnclosedElements()) {
                if (member.kind == ElementKind.METHOD) {
                    elementVisitor.visitElement(classBuilder, member, methodIndex, methodBuilder)
                    methodIndex++
                }
            }
        }
        return methodIndex
    }

}