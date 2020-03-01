package suspender.compiler.builder


import java.io.File
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

/**
 * Manages kotlin file generation
 */
open class BindingManager(private val processingEnvironment: ProcessingEnvironment) {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private val elementUtils: Elements = processingEnvironment.elementUtils
    private var stringTypeMirror: TypeMirror? = null
    private var charSequenceTypeMirror: TypeMirror? = null
    private var listTypeMirror: TypeMirror? = null
    private var setTypeMirror: TypeMirror? = null
    private var mapTypeMirror: TypeMirror? = null


    init {

        stringTypeMirror = getType("java.lang.String")
        listTypeMirror = getType("java.util.List")
        setTypeMirror = getType("java.util.Set")
        mapTypeMirror = getType("java.util.Map")
        charSequenceTypeMirror = getType("java.lang.CharSequence")
    }

    /**
     * Returns a [TypeMirror] for the given class
     */
    fun getType(className: String?): TypeMirror {
        return elementUtils.getTypeElement(className).asType()
    }


    /**
     * Returns a [Element] for the given class
     */
    fun getElement(className: String): Element {
        var cName = className
        val templateStart = className.indexOf('<')
        if (templateStart != -1) {
            cName = className.substring(0, templateStart).trim()
        }
        return elementUtils.getTypeElement(cName)
    }

    /**
     * Generates the abstract publisher class
     */
    fun generateProxy(element: Element) {
        val kaptKotlinGeneratedDir = processingEnvironment.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.replace("kaptKotlin", "kapt")
        kaptKotlinGeneratedDir?.let {
            element.annotationMirrors.forEach {
                it.elementValues.forEach { entry ->
                    if (entry.key.simpleName.toString() == "classesToWrap") {
                        val classesToConvert = entry.value.value as List<Any>
                        classesToConvert.forEach { covertClass ->
                            val convertTypeElement = getElement(covertClass.toString().removeSuffix(".class"))
                            ClassBuilder(convertTypeElement, this).generateSuspenderWrapper().writeTo(File(kaptKotlinGeneratedDir))
                        }
                    }
                }
            }
        }
    }


    fun getMessager(): Messager = processingEnvironment.messager

    internal fun getFunctiondBuilder(element: Element) = MethodBuilder(element, this)
}
