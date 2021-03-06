package dev.lipejose.paymentprocessor.domain.entities

class Customer(
    private val firstName: String,
    private val lastName: String,
) {
    fun fullName(): String {
        return "$firstName $lastName"
    }
}