# Suspender

Suspender generates wrapper classes around normal java classes and exposes its methods as **suspend** functions


Consider an existing class that performs a long running process as part of its function - 

```kotlin
/**
 * An office
 */
class Office {
	....
	
    fun addEmployee(employee: Employee) {
        //validate through some long running process
        
        //add
        employees.add(employee)
    }
}

```

Suspender allows you to call the above function safely from your coroutine without blocking it


```kotlin

    //This calls will block
    office.addEmployee(employee)

	//This is suspended call
    office.asSuspendable().addEmployee(employee)
```

All you have to do is specify which all existing classes needs to have wrappers generated!

Simply annotate an interface with **@Suspender** and specify all the classes that needs to be wrapped.

```kotlin

@Suspender(classesToWrap = [
    Office::class, 
    School::class
])
interface XXX

```

That's it! 


**Annotations**

* **@Suspender** Annotate on any interface/class to specify the classes that needs to be wrapped. For each of the class **X** specified, a wrapper class **X_SuspendWrapper** will be generated along with an extension function **X.asSuspendable()**




Getting Suspender
--------

Gradle dependency

```groovy
dependencies {

    implementation 'com.josesamuel:suspender-annotations:1.0.0'
    kapt 'com.josesamuel:suspender:1.0.0'
}
```


License
-------

    Copyright 2020 Joseph Samuel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


