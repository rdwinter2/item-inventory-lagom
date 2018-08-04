package ca.cbotek.cart.impl

import java.util.UUID

import ca.cbotek.cart.api.CartItem
import ca.cbotek.cart.impl.CartStatuses.CartStatus
import ca.cbotek.shared.JsonFormats._
import play.api.libs.json.{Format, Json}

case class CartState(id: UUID,
                     user: String,
                     items: Set[CartItem],
                     status: CartStatus)
object CartState {
  implicit val format: Format[CartState] = Json.format[CartState]
}

object CartStatuses extends Enumeration {
  type CartStatus = Value
  val IN_USE, CHECKEDOUT = Value

  implicit val format: Format[CartStatus] = enumFormat(CartStatuses)
}
