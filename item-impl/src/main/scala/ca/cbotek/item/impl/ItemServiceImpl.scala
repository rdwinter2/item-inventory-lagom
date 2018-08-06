package ca.cbotek.item.impl

import java.util.UUID

import akka.NotUsed
import ca.cbotek.item.api._
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.entity.{CartEntity, ItemInventoryEntity}
import ca.cbotek.item.impl.readside.CartRepository
import ca.cbotek.shared.{ErrorResponse, Marshaller}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

class ItemServiceImpl(registry: PersistentEntityRegistry,
                      cartRepository: CartRepository)
                     (implicit ec: ExecutionContext) extends ItemService with Marshaller {

  private def refForInventory = registry.refFor[ItemInventoryEntity]("item-inventory")
  private def refForCart(id: UUID) = registry.refFor[CartEntity](id.toString)

  override def getCatalog: ServiceCall[NotUsed, Catalog] =
    ServiceCall(_ =>
      refForInventory
        .ask(GetInventory)
        .map(inventory => Catalog(inventory.items, inventory.bundles))
    )

  override def addItem: ServiceCall[ItemRequest, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddItem(UUID.randomUUID(), request.name, request.description, request.price))
        .map(_.marshall)
    )

  override def addBundle: ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddBundle(UUID.randomUUID(), request.name, request.items, request.price))
        .map(_.marshall)
    )

  override def removeItem(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, _) =>
      refForInventory.ask(DeleteItem(id))
        .map(_.marshall)
    )

  override def removeBundle(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, _) =>
      refForInventory
        .ask(DeleteBundle(id))
        .map(_.marshall)
    )

  override def getCarts: ServiceCall[NotUsed, Iterable[Cart]] =
    ServiceCall(_ => cartRepository.getCarts)

  override def createCart: ServiceCall[CartRequest, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, request) =>
      checkItemIdsExist(request.items.map(_.itemId)).flatMap(_.fold(
        e => Future.successful(Left(e)),
        _ => {
          val cartId: UUID = UUID.randomUUID()
          refForCart(cartId).ask(CreateCart(cartId, request.user, request.items))}))
        .map(_.marshall)
    )

  override def setQuantityForCartItem(cartId: UUID, itemId: UUID, quantity: Int): ServiceCall[NotUsed, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, _) =>
      Right(quantity)
        .filterOrElse(qtt => qtt >= 0, ErrorResponse(400, "Bad request", "Quantity cannot be negative."))
        .fold(
          e => Future.successful(Left(e)),
          _ => checkItemIdsExist(Set(itemId)).flatMap(_.fold(
            e => Future.successful(Left(e)),
            _ => refForCart(cartId).ask(SetItemToCart(cartId, itemId, quantity)))))
        .map(_.marshall)
    )

  override def checkout(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Checkout]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "yet.")))
        .map(_.marshall)
    )

  private def checkItemIdsExist(itemIds: Set[UUID]): Future[Either[ErrorResponse, Catalog]] =
    refForInventory
      .ask(GetInventory)
      .map(inventory =>
        if (inventory.items.map(_.id).intersect(itemIds).size == itemIds.size) {
          Right(Catalog(inventory.items, inventory.bundles))
        } else {
          Left(ErrorResponse(400, "Bad Request", "Item(s) not in catalog."))
        })
}