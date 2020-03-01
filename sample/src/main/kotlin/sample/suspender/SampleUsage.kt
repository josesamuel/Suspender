package sample.suspender

import kotlinx.coroutines.runBlocking



fun main() = runBlocking{

    val student = Student("Foo", "Bar")
    val school = School()
    val employee = Employee("Foo", "Bar")
    val office = Office()


    //These calls will block
    school.addStudent(student)
    //These calls will block
    println(school.students)

    //These calls will block
    office.addEmployee(employee)


    //These calls will block
    println(office.getEmployees())



    //This is suspended call
    school.asSuspendable().addStudent(student)

    //This is suspended call
    println(school.asSuspendable().getStudents())

    //This is suspended call
    office.asSuspendable().addEmployee(employee)

    //This is suspended call
    println(office.asSuspendable().getEmployees())

}