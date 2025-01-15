/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.AEColor
import appeng.core.AppEng
import appeng.core.sync.GuiBridge
import appeng.items.tools.quartz.ToolQuartzCuttingKnife
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.bdew.ae2stuff.misc.{BlockWrenchable, MachineMaterial}
import net.bdew.lib.Misc
import net.bdew.lib.block.{HasItemBlock, HasTE, ItemBlockTooltip, SimpleBlock}
import net.minecraft.block.Block
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraft.world.{IBlockAccess, World}

import java.util

object BlockWireless
    extends SimpleBlock("Wireless", MachineMaterial)
    with HasTE[TileWireless]
    with BlockWrenchable
    with HasItemBlock {
  override val TEClass = classOf[TileWireless]
  override val ItemBlockClass: Class[_ <: ItemBlockWireless] =
    classOf[ItemBlockWireless]

  setHardness(1)

  var isHub = false;

  override def getDrops(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      metadata: Int,
      fortune: Int
  ): util.ArrayList[ItemStack] = {
    val stack = new ItemStack(this)
    val te = world.getTileEntity(x, y, z).asInstanceOf[TileWireless]
    if (te != null) {
      if (te.isHub) {
        if (te.color != AEColor.Transparent) {
          stack.setItemDamage(te.color.ordinal() + 18)
        } else {
          stack.setItemDamage(17)
        }
      } else if (te.color != AEColor.Transparent) {
        stack.setItemDamage(te.color.ordinal() + 1)
      }
    } else if (isHub) {
      stack.setItemDamage(17);
    }
    val drops = new util.ArrayList[ItemStack]()
    drops.add(stack)
    drops
  }

  override def getSubBlocks(
      itemIn: Item,
      tab: CreativeTabs,
      list: util.List[_]
  ): Unit = {
    for (meta <- 0 to 33) {
      list
        .asInstanceOf[util.List[ItemStack]]
        .add(new ItemStack(itemIn, 1, meta))
    }
  }

  override def getPickBlock(
      target: MovingObjectPosition,
      world: World,
      x: Int,
      y: Int,
      z: Int,
      player: EntityPlayer
  ): ItemStack = {
    val stack = new ItemStack(this)
    val te = getTE(world, x, y, z)
    if (te.isHub) {
      if (te.color != AEColor.Transparent) {
        stack.setItemDamage(te.color.ordinal() + 18)
      } else {
        stack.setItemDamage(17)
      }
    } else if (te.color != AEColor.Transparent) {
      stack.setItemDamage(te.color.ordinal() + 1)
    }
    stack
  }

  override def breakBlock(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      block: Block,
      meta: Int
  ): Unit = {
    val te = getTE(world, x, y, z);
    te.doUnlink()
    isHub = te.isHub;
    super.breakBlock(world, x, y, z, block, meta)
  }

  override def onBlockPlacedBy(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      player: EntityLivingBase,
      stack: ItemStack
  ): Unit = {
    val te = getTE(world, x, y, z)
    if (player.isInstanceOf[EntityPlayer]) {
      te.placingPlayer = player.asInstanceOf[EntityPlayer]
    }
    if (stack != null) {
      val itemDamage = stack.getItemDamage
      if (stack.hasDisplayName) {
        te.customName = stack.getDisplayName
      }
      if (itemDamage > 16) {
        te.isHub = true;
        if (itemDamage == 17) {
          te.color = AEColor.values().apply(16)
        } else {
          te.color = AEColor.values().apply(itemDamage - 18)
        }
      } else if (itemDamage > 0) {
        te.color = AEColor.values().apply(itemDamage - 1)
      }
    }
  }

  override def onBlockActivatedReal(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      player: EntityPlayer,
      side: Int,
      xOffs: Float,
      yOffs: Float,
      zOffs: Float
  ): Boolean = {
    val item = player.getHeldItem
    if (item != null && item.getItem.isInstanceOf[ToolQuartzCuttingKnife]) {
      val te = world.getTileEntity(x, y, z)
      if (te.isInstanceOf[TileWireless]) {
        player.openGui(
          AppEng.instance,
          (GuiBridge.GUI_RENAMER.ordinal << 5) | side,
          world,
          te.xCoord,
          te.yCoord,
          te.zCoord
        )
        return true
      }
    }
    false
  }

  var icon_on: List[IIcon] = null
  var icon_off: List[IIcon] = null

  @SideOnly(Side.CLIENT)
  override def getIcon(
      worldIn: IBlockAccess,
      x: Int,
      y: Int,
      z: Int,
      side: Int
  ): IIcon = {
    val te = getTE(worldIn, x, y, z)
    val meta = worldIn.getBlockMetadata(x, y, z)
    if (te.isHub) {
      if (meta > 0) {
        icon_on.apply(te.color.ordinal() + 17)
      } else {
        icon_off.apply(te.color.ordinal() + 17)
      }
    } else {
      if (meta > 0) {
        icon_on.apply(te.color.ordinal())
      } else {
        icon_off.apply(te.color.ordinal())
      }
    }
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon = {
    if (meta == 0) {
      icon_on.apply(AEColor.Transparent.ordinal())
    } else if (meta == 17) {
      icon_on.apply(33)
    } else {
      icon_on.apply(meta - 1)
    }
  }

  @SideOnly(Side.CLIENT)
  override def registerBlockIcons(reg: IIconRegister): Unit = {
    val index = 1.to(34)
    icon_on = index
      .map(index =>
        reg.registerIcon(Misc.iconName(modId, name, "side_on" + index))
      )
      .toList
    icon_off = index
      .map(index =>
        reg.registerIcon(Misc.iconName(modId, name, "side_off" + index))
      )
      .toList
  }
}

class ItemBlockWireless(b: Block) extends ItemBlockTooltip(b) {

  setHasSubtypes(true)

  override def addInformation(
      stack: ItemStack,
      player: EntityPlayer,
      list: util.List[_],
      advanced: Boolean
  ): Unit = {
    super.addInformation(stack, player, list, advanced)
    val itemDamage = stack.getItemDamage
    if (itemDamage == 17) {
      list
        .asInstanceOf[util.List[String]]
        .add(Misc.toLocal("tile.ae2stuff.WirelessHub.name"))
      list
        .asInstanceOf[util.List[String]]
        .add(Misc.toLocal(AEColor.values().apply(16).unlocalizedName))
    } else if (itemDamage > 16) {
      list
        .asInstanceOf[util.List[String]]
        .add(Misc.toLocal("tile.ae2stuff.WirelessHub.name"))
      list
        .asInstanceOf[util.List[String]]
        .add(
          Misc.toLocal(AEColor.values().apply(itemDamage - 18).unlocalizedName)
        )
    } else if (itemDamage > 0) {
      list
        .asInstanceOf[util.List[String]]
        .add(
          Misc.toLocal(
            AEColor.values().apply(itemDamage - 1).unlocalizedName
          )
        )
    }
  }
}
