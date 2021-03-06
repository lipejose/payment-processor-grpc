package dev.lipejose.paymentprocessor.infra.providers

import cieloecommerce.sdk.Merchant
import cieloecommerce.sdk.ecommerce.CieloEcommerce
import cieloecommerce.sdk.ecommerce.Environment
import cieloecommerce.sdk.ecommerce.Sale
import cieloecommerce.sdk.ecommerce.request.CieloRequestException
import dev.lipejose.paymentprocessor.PaymentResponse
import dev.lipejose.paymentprocessor.domain.entities.Card
import dev.lipejose.paymentprocessor.domain.entities.Order
import dev.lipejose.paymentprocessor.domain.error.PaymentFailedError
import dev.lipejose.paymentprocessor.domain.protocols.PaymentProvider
import java.io.IOException


class CieloProvider : PaymentProvider {
    private val merchant: Merchant? =
        Merchant(System.getenv("CIELO_ID"), System.getenv("CIELO_KEY"))

    override fun execute(card: Card, order: Order): PaymentResponse {
        var sale = Sale(order.id)

        sale.customer(order.customer.fullName())

        val payment = sale.payment(order.amount)

        with(payment) {
            creditCard(card.ccv, card.brand)
                .setExpirationDate(card.exp)
                .setCardNumber(card.number)
                .setHolder(card.name)
        }


        return try {
            sale = CieloEcommerce(merchant, Environment.SANDBOX).createSale(sale)

            val paymentId = sale.payment.paymentId

            CieloEcommerce(merchant, Environment.SANDBOX).captureSale(paymentId, order.amount, 0)

            PaymentResponse.newBuilder().setSuccess(true).setCode(200).setTransactionId(paymentId).build()
        } catch (e: CieloRequestException) {
            PaymentFailedError(e.error.message, e.error.code).build()
        } catch (e: IOException) {
            e.printStackTrace()
            PaymentFailedError(e.message.toString(), 500).build()
        }
    }


}




