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
import net.minecraft.util.IIcon
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

  override def getDrops(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      metadata: Int,
      fortune: Int
  ): util.ArrayList[ItemStack] = {

    val stack = new ItemStack(Item.getItemFromBlock(this), 1, 0)
    val te = world.getTileEntity(x, y, z)
    if (
      te.isInstanceOf[TileWireless] && te
        .asInstanceOf[TileWireless]
        .color != AEColor.Transparent
    ) {
      stack.setItemDamage(te.asInstanceOf[TileWireless].color.ordinal() + 1)
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
    for (meta <- 0 to (16)) {
      list
        .asInstanceOf[util.List[ItemStack]]
        .add(new ItemStack(itemIn, 1, meta))
    }
  }

  override def breakBlock(
      world: World,
      x: Int,
      y: Int,
      z: Int,
      block: Block,
      meta: Int
  ): Unit = {
    getTE(world, x, y, z).doUnlink()
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
    if (player.isInstanceOf[EntityPlayer]) {
      val te = getTE(world, x, y, z)
      te.placingPlayer = player.asInstanceOf[EntityPlayer]
      if (stack != null) {
        if (stack.hasDisplayName) {
          te.customName = stack.getDisplayName
        }
        if (stack.getItemDamage > 0) {
          te.color = AEColor.values().apply(stack.getItemDamage - 1)
        }
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
  var icon_none: IIcon = null

  @SideOnly(Side.CLIENT)
  override def getIcon(
      worldIn: IBlockAccess,
      x: Int,
      y: Int,
      z: Int,
      side: Int
  ): IIcon = {
    val te = worldIn.getTileEntity(x, y, z)
    val meta = worldIn.getBlockMetadata(x, y, z)

    if (te.isInstanceOf[TileWireless]) {
      val color = te.asInstanceOf[TileWireless].color.ordinal()
      if (meta > 0) {
        icon_on.apply(color)
      } else {
        icon_off.apply(color)
      }
    } else {
      icon_none
    }
  }

  override def getIcon(side: Int, meta: Int): IIcon = {
    if (meta == 0) {
      icon_on.apply(16)
    } else {
      icon_on.apply(meta - 1)
    }
  }

  @SideOnly(Side.CLIENT)
  override def registerBlockIcons(reg: IIconRegister): Unit = {
    val index = 1.to(17)
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
    icon_none = reg.registerIcon(Misc.iconName(modId, name, "side_off"))
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
    if (stack.getItemDamage > 0) {
      list
        .asInstanceOf[util.List[String]]
        .add(
          Misc.toLocal(
            AEColor.values().apply(stack.getItemDamage - 1).unlocalizedName
          )
        )
    }
  }
}
