package ca.cbotek.item.impl

import java.util.UUID

import ca.cbotek.item.api.{BundleRequestItem, Item}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class ItemInventoryInit(registry: PersistentEntityRegistry)
                       (implicit ec: ExecutionContext) {
  private def refForInventory = registry.refFor[ItemInventoryEntity]("item-inventory")

  val carabinerId: UUID = UUID.randomUUID

  val harness1Id: UUID = UUID.randomUUID
  val harness2Id: UUID = UUID.randomUUID
  val harness3Id: UUID = UUID.randomUUID

  val shoes1Id: UUID = UUID.randomUUID
  val shoes2Id: UUID = UUID.randomUUID
  val shoes3Id: UUID = UUID.randomUUID

  val bundle1Id: UUID = UUID.randomUUID
  val bundle1Items: Iterable[BundleRequestItem] =
    Iterable(
      BundleRequestItem(5, carabinerId))

  val bundle2Id: UUID = UUID.randomUUID
  val bundle2Items: Iterable[BundleRequestItem] =
    Iterable(
      BundleRequestItem(1, shoes1Id),
      BundleRequestItem(1, harness3Id))

  val bundle3Id: UUID = UUID.randomUUID
  val bundle3Items: Iterable[BundleRequestItem] =
    Iterable(
      BundleRequestItem(1, shoes1Id),
      BundleRequestItem(1, harness3Id),
      BundleRequestItem(5, carabinerId))

  refForInventory
    .ask(AddItem(carabinerId, "Carabiner Harness", "Lightweight and stainless steel.", 24.95))
    .flatMap(_ => refForInventory.ask(AddItem(harness1Id, "Blue Harness", "Heavy and blue.", 45.95)))
    .flatMap(_ => refForInventory.ask(AddItem(harness2Id, "Black Harness", "Durable and black.", 61.95)))
    .flatMap(_ => refForInventory.ask(AddItem(harness3Id, "Red Harness", "Cheap and red.", 25.95)))
    .flatMap(_ => refForInventory.ask(AddItem(shoes1Id, "Beginner Shoes", "Ugly and soft.",45.95)))
    .flatMap(_ => refForInventory.ask(AddItem(shoes2Id, "Intermediate Shoes", "Perfect for outdoors.", 65.95)))
    .flatMap(_ => refForInventory.ask(AddItem(shoes3Id, "Advanced Shoes", "Will suit everybody.", 95.95)))

    .flatMap(_ => refForInventory.ask(AddBundle(bundle1Id, "Carabiner x5 Pack", bundle1Items, 20)))
    .flatMap(_ => refForInventory.ask(AddBundle(bundle2Id, "Harness and shoes Pack", bundle2Items, 60)))
    .flatMap(_ => refForInventory.ask(AddBundle(bundle3Id, "All in pack !", bundle3Items, 75)))

}
