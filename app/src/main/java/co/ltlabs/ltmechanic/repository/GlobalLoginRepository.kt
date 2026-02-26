package co.ltlabs.ltmechanic.repository

import co.ltlabs.ltmechanic.domain.Employee
import co.ltlabs.ltmechanic.domain.RfidRequest
import co.ltlabs.ltmechanic.network.ApiGlobal


class GlobalLoginRepository(private val api: ApiGlobal) {

    fun loginEmployeeAsync(employee: Employee) = api.loginEmployeeAsync(employee)

    fun loginEmployeeWithRfidAsync(rfidRequest: RfidRequest) = api.loginEmployeeWithRfidAsync(rfidRequest)

}