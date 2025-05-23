/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.items

import appeng.api.config.SecurityPermissions
import appeng.api.exceptions.FailedConnection
import net.bdew.ae2stuff.AE2Stuff
import net.bdew.ae2stuff.grid.Security
import net.bdew.ae2stuff.machines.wireless.{BlockWireless, TileWireless}
import net.bdew.ae2stuff.misc.AdvItemLocationStore
import net.bdew.lib.Misc
import net.bdew.lib.block.BlockRef
import net.bdew.lib.items.SimpleItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraft.client.resources.I18n
import net.minecraft.client.settings.KeyBinding
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard

import java.util

object AdvWirelessKit
    extends SimpleItem("AdvWirelessKit")
    with AdvItemLocationStore {
  setMaxStackSize(1)

  val MODE_QUEUING = 0;
  val MODE_BINDING = 1;

  def checkSecurity(t1: TileWireless, t2: TileWireless, p: EntityPlayer) = {
    val pid = Security.getPlayerId(p)
    Security.playerHasPermission(
      t1.getNode.getGrid,
      pid,
      SecurityPermissions.BUILD
    ) &&
    Security.playerHasPermission(
      t2.getNode.getGrid,
      pid,
      SecurityPermissions.BUILD
    )
  }

  override def onItemRightClick(
      stack: ItemStack,
      world: World,
      player: EntityPlayer
  ): ItemStack = {
    import net.bdew.lib.helpers.ChatHelper._
    if (!world.isRemote && player.isSneaking) {
      if (AE2Stuff.keybindModeSwitch.isKeyDown(player)) {
        while (hasLocation(stack)) {
          popLocation(stack)
        }
        player.addChatMessage(
          L("ae2stuff.wireless.advtool.queueing.clear").setColor(
            Color.GREEN
          )
        )
        return stack
      }
      toggleMode(stack)
      if (getMode(stack) == MODE_QUEUING) {
        player.addChatMessage(
          L("ae2stuff.wireless.advtool.queueing.activated").setColor(
            Color.GREEN
          )
        )
      } else {
        player.addChatMessage(
          L("ae2stuff.wireless.advtool.binding.activated").setColor(Color.GREEN)
        )
      }
    }
    stack
  }

  override def onItemUse(
      stack: ItemStack,
      player: EntityPlayer,
      world: World,
      x: Int,
      y: Int,
      z: Int,
      side: Int,
      xOff: Float,
      yOff: Float,
      zOff: Float
  ): Boolean = {
    import net.bdew.lib.helpers.ChatHelper._
    val pos = BlockRef(x, y, z)
    if (!pos.blockIs(world, BlockWireless)) return false
    if (!world.isRemote) {
      if (player.isSneaking) {
        if (AE2Stuff.keybindModeSwitch.isKeyDown(player)) {
          while (hasLocation(stack)) {
            popLocation(stack)
          }
          player.addChatMessage(
            L("ae2stuff.wireless.advtool.queueing.clear").setColor(
              Color.GREEN
            )
          )
          return true
        }
        toggleMode(stack)
        if (getMode(stack) == MODE_QUEUING) {
          player.addChatMessage(
            L("ae2stuff.wireless.advtool.queueing.activated").setColor(
              Color.GREEN
            )
          )
        } else if (getMode(stack) == MODE_BINDING) {
          player.addChatMessage(
            L("ae2stuff.wireless.advtool.binding.activated").setColor(
              Color.GREEN
            )
          )
        }
        return true;
      }
      pos.getTile[TileWireless](world) foreach { tile =>
        val pid = Security.getPlayerId(player)
        // Check that the player can modify the network
        if (
          !Security.playerHasPermission(
            tile.getNode.getGrid,
            pid,
            SecurityPermissions.BUILD
          )
        ) {
          player.addChatMessage(
            L("ae2stuff.wireless.tool.security.player").setColor(Color.RED)
          )
        } else if (getMode(stack) == MODE_QUEUING) {
          var isHub = false;
          val tempTE =
            world.getTileEntity(pos.x, pos.y, pos.z).asInstanceOf[TileWireless]
          if (tempTE != null) {
            if (tempTE.connectionsList.length >= 32) {
              player.addChatMessage(
                L("ae2stuff.wireless.tool.targethubfull").setColor(Color.RED)
              )
              return true
            }
            isHub = tempTE.isHub
            if (isHub && AE2Stuff.keybindModeSwitch.isKeyDown(player)) {
              val t = 32 - tempTE.connectionsList.length
              var i = 0
              while (i < t) {
                addLocation(stack, pos, world.provider.dimensionId, isHub)
                i = i + 1
              }
              player.addChatMessage(
                L(
                  "ae2stuff.wireless.advtool.hub.queued",
                  i.toString
                ).setColor(Color.GREEN)
              )
            } else {
              if (addLocation(stack, pos, world.provider.dimensionId, isHub)) {
                player.addChatMessage(
                  L(
                    "ae2stuff.wireless.advtool.queued",
                    pos.x.toString,
                    pos.y.toString,
                    pos.z.toString
                  ).setColor(Color.GREEN)
                )
              } else {
                player.addChatMessage(
                  L("ae2stuff.wireless.advtool.queuederror").setColor(Color.RED)
                )
              }
            }
          }
        } else if (getMode(stack) == MODE_BINDING) {
          var doLoop = true
          while (doLoop) {
            doLoop = false
            if (hasLocation(stack)) {
              if (tile.isHub && AE2Stuff.keybindModeSwitch.isKeyDown(player)) {
                if (
                  tile.connectionsList.length < 31 && getLocations(stack)
                    .tagCount() > 1
                ) {
                  doLoop = true
                } else if (tile.connectionsList.length == 32) {
                  player.addChatMessage(
                    L("ae2stuff.wireless.tool.targethubfull").setColor(
                      Color.RED
                    )
                  )
                  return true
                }
              }
              // Have other location - start connecting
              val otherPos = getNextLocation(stack)

              if (getDimension(stack) != world.provider.dimensionId) {
                // Different dimensions - error out
                player.addChatMessage(
                  L("ae2stuff.wireless.tool.dimension").setColor(Color.RED)
                )
              } else if (pos == otherPos) {
                // Same block - clear the location
                popLocation(stack)
              } else {
                otherPos.getTile[TileWireless](world) match {
                  // Check that the other tile is still around
                  case Some(other: TileWireless) =>
                    // And check that the player can modify it too
                    if (
                      !Security.playerHasPermission(
                        other.getNode.getGrid,
                        pid,
                        SecurityPermissions.BUILD
                      )
                    ) {
                      player.addChatMessage(
                        L("ae2stuff.wireless.tool.security.player").setColor(
                          Color.RED
                        )
                      )
                    } else if (tile.isHub && other.isHub) {
                      player.addChatMessage(
                        L("ae2stuff.wireless.tool.failed").setColor(Color.RED)
                      )
                    } else {
                      // Player can modify both sides - unlink current connections if any
                      if (!tile.isHub) tile.doUnlink()
                      if (!other.isHub) other.doUnlink()

                      // Make player the owner of both blocks
                      tile.getNode.setPlayerID(pid)
                      other.getNode.setPlayerID(pid)
                      try {
                        if (tile.doLink(other)) {
                          player.addChatMessage(
                            L(
                              "ae2stuff.wireless.tool.connected",
                              pos.x.toString,
                              pos.y.toString,
                              pos.z.toString
                            ).setColor(Color.GREEN)
                          )
                        } else {
                          player.addChatMessage(
                            L("ae2stuff.wireless.tool.failed").setColor(
                              Color.RED
                            )
                          )
                        }
                      } catch {
                        case e: FailedConnection =>
                          player.addChatComponentMessage(
                            (L(
                              "ae2stuff.wireless.tool.failed"
                            ) & ": " & e.getMessage).setColor(Color.RED)
                          )
                          tile.doUnlink()
                          print("Failed to link wireless connector: " + e)
                      }
                    }
                    popLocation(stack)
                  case _ =>
                    // The other block is gone - error out
                    player.addChatMessage(
                      L("ae2stuff.wireless.tool.noexist").setColor(Color.RED)
                    )
                    popLocation(stack)
                }
              }
            } else {
              player.addChatMessage(
                L("ae2stuff.wireless.advtool.noconnectors").setColor(Color.RED)
              )
            }
          }
        }
      }
    }
    true
  }

  override def addInformation(
      stack: ItemStack,
      player: EntityPlayer,
      tips: util.List[_],
      detailed: Boolean
  ): Unit = {
    val list = tips.asInstanceOf[util.List[String]]
    if (getLocations(stack).tagCount() > 0) {
      val next = getNextLocation(stack)
      list.add(
        Misc.toLocalF(
          "ae2stuff.wireless.advtool.connector.next",
          next.x,
          next.y,
          next.z
        )
      )
    }
    if (getMode(stack) == MODE_QUEUING) {
      list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing"))
      if (getLocations(stack).tagCount() == 0) {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing.empty"))
      } else {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing.notempty"))
        for (i <- 0 until getLocations(stack).tagCount()) {
          val loc = BlockRef.fromNBT(getLocations(stack).getCompoundTagAt(i))
          list.add(loc.x + "," + loc.y + "," + loc.z)
        }
      }
      list.add(
        I18n.format(
          "ae2stuff.wireless.tooltips.advtool.hubqols.queueing",
          Minecraft.getMinecraft.gameSettings.keyBindings
            .find(_.getKeyDescription == AE2Stuff.keybindModeId)
            .map(kb => Keyboard.getKeyName(kb.getKeyCode))
            .getOrElse("NONE")
        )
      )
    } else if (getMode(stack) == MODE_BINDING) {
      list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding"))
      if (getLocations(stack).tagCount() == 0) {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding.empty"))
      } else {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding.notempty"))
        for (i <- 0 until getLocations(stack).tagCount()) {
          val loc = BlockRef.fromNBT(getLocations(stack).getCompoundTagAt(i))
          list.add(loc.x + "," + loc.y + "," + loc.z)
        }
      }
      list.add(
        I18n.format(
          "ae2stuff.wireless.tooltips.advtool.hubqols.binding",
          Minecraft.getMinecraft.gameSettings.keyBindings
            .find(_.getKeyDescription == AE2Stuff.keybindModeId)
            .map(kb => Keyboard.getKeyName(kb.getKeyCode))
            .getOrElse("NONE")
        )
      )
    }
    list.add(
      I18n.format(
        "ae2stuff.wireless.tooltips.advtool.queueing.clear",
        Minecraft.getMinecraft.gameSettings.keyBindings
          .find(_.getKeyDescription == AE2Stuff.keybindModeId)
          .map(kb => Keyboard.getKeyName(kb.getKeyCode))
          .getOrElse("NONE")
      )
    )
    list.add(Misc.toLocal("ae2stuff.wireless.advtool.extra"))
  }
}
