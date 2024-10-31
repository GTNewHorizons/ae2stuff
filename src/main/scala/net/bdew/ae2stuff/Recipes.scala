package net.bdew.ae2stuff

import cpw.mods.fml.common.registry.GameRegistry
import net.bdew.ae2stuff.items.{AdvWirelessKit, ItemWirelessKit}
import net.bdew.ae2stuff.machines.wireless.BlockWireless
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

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
      new ItemStack(BlockWireless, 1, OreDictionary.WILDCARD_VALUE)
    )
  }
}
