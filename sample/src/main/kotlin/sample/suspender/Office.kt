package sample.suspender

import remoter.annotations.NullableType

/**
 * An office
 */
class Office {

    private val employees = mutableListOf<Employee>()

    fun addEmployee(employee: Employee) {
        //validate through some long running process
        employees.add(employee)
    }

    fun getEmployees(): List<Employee> {
        Thread.sleep(1000)
        println("getEmployees Running on ${Thread.currentThread()}")
        return employees
    }

    @NullableType
    suspend fun anExistingSuspendFunction(): Employee? {
        return if (employees.isEmpty())
            null
        else
            employees[0]
    }
}