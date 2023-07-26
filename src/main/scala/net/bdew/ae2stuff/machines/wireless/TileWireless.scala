/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.machines.wireless

import java.util
import appeng.api.AEApi
import appeng.api.implementations.tiles.IColorableTile
import appeng.api.networking.{GridFlags, IGridConnection}
import appeng.api.util.AEColor
import appeng.me.helpers.AENetworkProxy
import net.bdew.ae2stuff.AE2Stuff
import net.bdew.ae2stuff.grid.{GridTile, VariableIdlePower}
import net.bdew.lib.block.BlockRef
import net.bdew.lib.data.base.{TileDataSlots, UpdateKind}
import net.bdew.lib.multiblock.data.DataSlotPos
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

class TileWireless
    extends TileDataSlots
    with GridTile
    with VariableIdlePower
    with IColorableTile {
  val cfg = MachineWireless

  val link =
    DataSlotPos("link", this).setUpdate(UpdateKind.SAVE, UpdateKind.WORLD)

  var connection: IGridConnection = null

  lazy val myPos = BlockRef.fromTile(this)

  var customName: String = ""
  var color: AEColor = AEColor.Transparent
  def isLinked = link.isDefined
  def getLink = link flatMap (_.getTile[TileWireless](worldObj))

  override def getFlags = util.EnumSet.of(GridFlags.DENSE_CAPACITY)

  serverTick.listen(() => {
    if (connection == null && link.isDefined) {
      try {
        setupConnection()
      } catch {
        case t: Throwable =>
          AE2Stuff.logWarnException(
            "Failed setting up wireless link %s <-> %s: %s",
            t,
            myPos,
            link.get,
            t.getMessage
          )
          doUnlink()
      }
    }
  })

  def doLink(other: TileWireless): Boolean = {
    if (other.link.isEmpty) {
      other.link.set(myPos)
      this.customName = other.customName
      link.set(other.myPos)
      setupConnection()
    } else false
  }

  def doUnlink(): Unit = {
    breakConnection()
    getLink foreach { that =>
      this.link := None
      that.link := None
    }
  }

  def setupConnection(): Boolean = {
    getLink foreach { that =>
      connection =
        AEApi.instance().createGridConnection(this.getNode, that.getNode)
      that.connection = connection
      val dx = this.xCoord - that.xCoord
      val dy = this.yCoord - that.yCoord
      val dz = this.zCoord - that.zCoord
      // val power = cfg.powerBase + cfg.powerDistanceMultiplier * (dx * dx + dy * dy + dz * dz)
      val dist = math.sqrt(dx * dx + dy * dy + dz * dz)
      val power = cfg.powerBase + cfg.powerDistanceMultiplier * dist * math.log(
        dist * dist + 3
      )
      this.setIdlePowerUse(power)
      that.setIdlePowerUse(power)
      if (worldObj.blockExists(xCoord, yCoord, zCoord))
        worldObj.setBlockMetadataWithNotify(
          this.xCoord,
          this.yCoord,
          this.zCoord,
          1,
          3
        )
      if (worldObj.blockExists(that.xCoord, that.yCoord, that.zCoord))
        worldObj.setBlockMetadataWithNotify(
          that.xCoord,
          that.yCoord,
          that.zCoord,
          1,
          3
        )
      return true
    }
    false
  }

  def breakConnection(): Unit = {
    if (connection != null)
      connection.destroy()
    connection = null
    setIdlePowerUse(0d)
    getLink foreach { other =>
      other.connection = null
      other.setIdlePowerUse(0d)
      if (worldObj.blockExists(other.xCoord, other.yCoord, other.zCoord))
        worldObj.setBlockMetadataWithNotify(
          other.xCoord,
          other.yCoord,
          other.zCoord,
          0,
          3
        )
    }
    if (worldObj.blockExists(xCoord, yCoord, zCoord))
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3)
  }

  override def getMachineRepresentation: ItemStack = new ItemStack(
    BlockWireless
  )

  override def shouldRefresh(
      oldBlock: Block,
      newBlock: Block,
      oldMeta: Int,
      newMeta: Int,
      world: World,
      x: Int,
      y: Int,
      z: Int
  ): Boolean =
    newBlock != BlockWireless

  override def doSave(kind: UpdateKind.Value, t: NBTTagCompound): Unit = {
    super.doSave(kind, t)
    if (customName != "") {
      t.setString("CustomName", customName)
    }
    t.setString("CustomName", customName)
    t.setShort("Color", color.ordinal().toShort)
  }

  override def doLoad(kind: UpdateKind.Value, t: NBTTagCompound): Unit = {
    super.doLoad(kind, t)
    this.customName = t.getString("CustomName")
    if (!t.hasKey("Color")) {
      t.setShort("Color", AEColor.Transparent.ordinal().toShort)
    }
    val colorIdx = t.getShort("Color").toInt
    this.color = AEColor.values().apply(colorIdx)
  }

  override def recolourBlock(
      side: ForgeDirection,
      colour: AEColor,
      who: EntityPlayer
  ): Boolean = {
    this.color = colour
    true
  }

  override def getConnectableSides: util.EnumSet[ForgeDirection] =
    super.getConnectableSides
  override def getColor: AEColor = color

  override def getGridColor: AEColor = color
}
