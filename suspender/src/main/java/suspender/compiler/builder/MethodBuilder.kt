package suspender.compiler.builder

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.throws
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import suspender.compiler.builder.ClassBuilder.Companion.PROXY_SUFFIX
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType


/**
 * A [SuspenderBuilder] that knows how to generate methods for the wrapper
 */
class MethodBuilder(suspenderElement: Element, bindingManager: BindingManager) : SuspenderBuilder(suspenderElement, bindingManager) {

    private val addedMethods = mutableSetOf<String>()

    /**
     * Build the proxy methods
     */
    fun addWrapperMethods(classBuilder: TypeSpec.Builder) {
        processSuspenderElements(classBuilder, object : ElementVisitor {
            override fun visitElement(classBuilder: TypeSpec.Builder, member: Element, methodIndex: Int, methodBuilder: FunSpec.Builder?) {
                addWrapperMethods(classBuilder, member, methodIndex)
            }
        }, null)
        addHashCode(classBuilder)
        addEquals(classBuilder)
        addWrapperToString(classBuilder)
        addWrapperDispatcher(classBuilder)
        addWrapperInstance(classBuilder)
    }


    /**
     * Build the proxy methods
     */
    private fun addWrapperMethods(classBuilder: TypeSpec.Builder, member: Element, methodIndex: Int) {
        val executableElement = member as ExecutableElement
        val methodName = executableElement.simpleName.toString()

        val isSuspendFunction = executableElement.isSuspendFunction()
        if (isSuspendFunction) {
            return
        }

        val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.PUBLIC)

        methodBuilder.addModifiers(KModifier.SUSPEND)

        methodBuilder.returns(executableElement.returnType.asKotlinType(this, executableElement).copy(executableElement.isNullable()))

        //add Exceptions
        if (executableElement.thrownTypes?.isNotEmpty() == true) {
            methodBuilder.throws(executableElement.thrownTypes.map { it.asKotlinType() })
        }

        //add parameters
        val paramsSize = executableElement.parameters.size
        var paramIndex = 0
        for (params in executableElement.parameters) {

            val varArgParamIndex = if (isSuspendFunction) paramsSize - 2 else paramsSize - 1

            if (paramIndex == varArgParamIndex && executableElement.isVarArgs) {
                val arrayComponentType = (params.asType() as ArrayType).componentType
                methodBuilder.addParameter(ParameterSpec.builder(params.simpleName.toString(),
                        arrayComponentType.asKotlinType().copy(true)).addModifiers(KModifier.VARARG).build())

            } else {
                methodBuilder.addParameter(ParameterSpec.builder(params.simpleName.toString(),
                        params.asKotlinType(this)).build())
            }

            paramIndex++
        }

        methodBuilder.beginControlFlow("return kotlinx.coroutines.withContext(getSuspenderWrapperDispatcher(\"$methodName\"))")

        var methodCall = "instance.$methodName("
        val paramSize = executableElement.parameters.size
        for (paramCount in 0 until paramSize) {

            methodCall += executableElement.parameters[paramCount]
            if (paramCount < paramSize - 1) {
                methodCall += ", "
            }
        }
        methodCall += ")"

        methodBuilder.addStatement("$methodCall as %T", executableElement.returnType.asKotlinType(this, executableElement).copy(executableElement.isNullable()))

        methodBuilder.endControlFlow()

        val funSpec = methodBuilder.build()
        val methodSignature = funSpec.name.toString() + funSpec.parameters.toString()
        if (addedMethods.contains(methodSignature)) {
            return
        }
        addedMethods.add(methodSignature)

        classBuilder.addFunction(methodBuilder.build())
    }

    /**
     * Add proxy method to set hashcode to uniqueu id of binder
     */
    private fun addHashCode(classBuilder: TypeSpec.Builder) {
        val methodBuilder = FunSpec.builder("hashCode")
                .addModifiers(KModifier.PUBLIC)
                .returns(Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return instance.hashCode()")
        classBuilder.addFunction(methodBuilder.build())
    }

    /**
     * Add proxy method for equals
     */
    private fun addEquals(classBuilder: TypeSpec.Builder) {

        val declaredType = (suspenderElement.asType() as DeclaredType)
        var typeAddition = ""
        val totalTypesArguments = declaredType.typeArguments.size
        if (totalTypesArguments > 0) {
            typeAddition = "<*"
            for (index in 1 until totalTypesArguments) {
                typeAddition += ",*"
            }
            typeAddition += ">"
        }
        val methodBuilder = FunSpec.builder("equals")
                .addModifiers(KModifier.PUBLIC)
                .addParameter("other", Any::class.asTypeName().copy(true))
                .returns(Boolean::class)
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return (other is $suspenderClassName$PROXY_SUFFIX$typeAddition) && (other.instance.equals(instance))")
        classBuilder.addFunction(methodBuilder.build())
    }

    /**
     * Add proxy method for equals
     */
    private fun addWrapperToString(classBuilder: TypeSpec.Builder) {
        val methodBuilder = FunSpec.builder("toString")
                .addModifiers(KModifier.PUBLIC)
                .returns(String::class)
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return \"$suspenderClassName$PROXY_SUFFIX[\$instance]\"")
        classBuilder.addFunction(methodBuilder.build())
    }

    /**
     * Add proxy method for equals
     */
    private fun addWrapperInstance(classBuilder: TypeSpec.Builder) {
        val methodBuilder = FunSpec.builder("getSuspenderWrappedInstance")
                .addKdoc("Returns the instance of [$suspenderClassName] that this wraps")
                .addModifiers(KModifier.PUBLIC)
                .returns(suspenderElement.asKotlinType())
                .addStatement("return instance")
        classBuilder.addFunction(methodBuilder.build())
    }


    /**
     * Add proxy method for equals
     */
    private fun addWrapperDispatcher(classBuilder: TypeSpec.Builder) {
        val methodBuilder = FunSpec.builder("getSuspenderWrapperDispatcher")
                .addKdoc("Override this to selectively specify on which [CoroutineDispatcher] to run each function\n")
                .addKdoc("Default is [Dispatchers.IO]\n")
                .addModifiers(KModifier.PUBLIC)
                .addModifiers(KModifier.OPEN)
                .addParameter("methodName", String::class)
                .returns(CoroutineDispatcher::class)
                .addStatement("return %T.IO", Dispatchers::class)
        classBuilder.addFunction(methodBuilder.build())
    }
}