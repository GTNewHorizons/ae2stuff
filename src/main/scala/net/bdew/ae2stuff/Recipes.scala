package net.bdew.ae2stuff

import cpw.mods.fml.common.registry.GameRegistry
import net.bdew.ae2stuff.items.{AdvWirelessKit, ItemWirelessKit}
import net.bdew.ae2stuff.machines.wireless.BlockWireless
import net.minecraft.item.ItemStack

object Recipes {
  def load(): Unit = {
    // add recipe to clear NBT from adv wireless kit
    GameRegistry.addShapelessRecipe(
      new ItemStack(AdvWirelessKit),
      AdvWirelessKit
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(ItemWirelessKit),
      ItemWirelessKit
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 1)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 2)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 3)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 4)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 5)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 6)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 7)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 8)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 9)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 10)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 11)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 12)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 13)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 14)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 15)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless),
      new ItemStack(BlockWireless, 1, 16)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 18)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 19)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 20)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 21)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 22)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 23)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 24)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 25)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 26)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 27)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 28)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 29)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 30)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 31)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 32)
    )
    GameRegistry.addShapelessRecipe(
      new ItemStack(BlockWireless, 1, 17),
      new ItemStack(BlockWireless, 1, 33)
    )
  }
}
