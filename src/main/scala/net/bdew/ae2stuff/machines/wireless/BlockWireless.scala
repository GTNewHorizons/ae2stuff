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
import appeng.util.Platform
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.bdew.ae2stuff.misc.{BlockWrenchable, MachineMaterial}
import net.bdew.lib.Misc
import net.bdew.lib.block.{HasTE, SimpleBlock}
import net.minecraft.block.Block
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection

object BlockWireless
    extends SimpleBlock("Wireless", MachineMaterial)
    with HasTE[TileWireless]
    with BlockWrenchable {
  override val TEClass = classOf[TileWireless]

  setHardness(1)

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
      if (stack != null && stack.hasDisplayName) {
        te.customName = stack.getDisplayName
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
  var icon_off: IIcon = null

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
      if (meta > 0) {
        val color = te.asInstanceOf[TileWireless].color.ordinal()
        return icon_on.apply(color)
      }
    }
    icon_off
  }

  override def getIcon(side: Int, meta: Int): IIcon = {
    icon_on.apply(AEColor.Transparent.ordinal())
  }

  @SideOnly(Side.CLIENT)
  override def registerBlockIcons(reg: IIconRegister): Unit = {
    val index = 1.to(17)
    icon_on = index
      .map(index =>
        reg.registerIcon(Misc.iconName(modId, name, "side_on" + index))
      )
      .toList
    icon_off = reg.registerIcon(Misc.iconName(modId, name, "side_off"))
  }
}
