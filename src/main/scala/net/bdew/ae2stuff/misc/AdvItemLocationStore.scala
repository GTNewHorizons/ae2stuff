/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.misc

import net.bdew.lib.block.BlockRef
import net.bdew.lib.nbt.NBT
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTBase, NBTTagCompound}

trait AdvItemLocationStore extends Item {

  import net.bdew.ae2stuff.items.AdvWirelessKit.MODE_QUEUING

  private val COMPOUND_TAG = NBTBase.NBTTypes.indexOf("COMPOUND")

  def addLocation(
      stack: ItemStack,
      loc: BlockRef,
      dimension: Int,
      isHub: Boolean
  ): Boolean = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("dim") && tag.getInteger("dim") != dimension) {
      false
    }
    val locList = tag.getTagList("loc", COMPOUND_TAG)
    for (i <- 0 until locList.tagCount()) {
      val tag = locList.getCompoundTagAt(i)
      val pos = BlockRef.fromNBT(tag)
      if (pos == loc && !isHub) {
        return false
      }
    }
    locList.appendTag(NBT.from(loc.writeToNBT _))
    tag.setTag("loc", locList)
    tag.setInteger("dim", dimension)
    true
  }

  def getLocations(stack: ItemStack) = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("loc")) {
      val locList = tag.getTagList("loc", COMPOUND_TAG)
      locList
    } else {
      tag.setTag("loc", new NBTTagCompound)
      tag.getTagList("loc", COMPOUND_TAG)
    }
  }

  // location is going to be a queue of coordinates

  def hasLocation(stack: ItemStack): Boolean = {
    if (
      stack.getItem == this && stack.hasTagCompound && stack.getTagCompound
        .hasKey("loc")
    ) {
      // check if list is not empty
      val loc = stack.getTagCompound.getTagList("loc", COMPOUND_TAG)
      if (loc.tagCount() > 0) {
        return true
      }
    }
    false
  }

  def getNextLocation(stack: ItemStack): BlockRef =
    BlockRef.fromNBT(
      stack.getTagCompound.getTagList("loc", COMPOUND_TAG).getCompoundTagAt(0)
    )

  def getDimension(stack: ItemStack): Int =
    stack.getTagCompound.getInteger("dim")

  def setLocation(stack: ItemStack, loc: BlockRef, dimension: Int): Unit = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    val locList = tag.getTagList("loc", COMPOUND_TAG)
    locList.appendTag(NBT.from(loc.writeToNBT _))
    tag.setTag("loc", locList)
    tag.setInteger("dim", dimension)
  }

  def popLocation(stack: ItemStack): BlockRef = {
    if (stack.hasTagCompound) {
      val locList = stack.getTagCompound.getTagList("loc", COMPOUND_TAG)
      if (locList.tagCount() > 0) {
        val tag = locList.getCompoundTagAt(0)
        locList.removeTag(0)
        val pos = BlockRef.fromNBT(tag)
        stack.getTagCompound.setTag("loc", locList)
        return pos;
      }
      if (locList.tagCount() == 0) {
        stack.getTagCompound.removeTag("loc")
        stack.getTagCompound.removeTag("dim")
      }
    }
    null
  }

  def getMode(stack: ItemStack): Integer = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("mode")) {
      tag.getInteger("mode")
    } else {
      tag.setInteger("mode", MODE_QUEUING)
      MODE_QUEUING
    }
  }

  def toggleMode(stack: ItemStack): Integer = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("mode")) {
      val mode = tag.getInteger("mode")
      tag.setInteger("mode", (mode + 1) % 2)
      tag.getInteger("mode")
    } else {
      tag.setInteger("mode", MODE_QUEUING)
      MODE_QUEUING
    }
  }
}
