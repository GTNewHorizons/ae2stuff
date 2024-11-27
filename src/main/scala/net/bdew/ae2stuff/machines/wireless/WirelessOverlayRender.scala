/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.machines.wireless

import net.bdew.ae2stuff.misc.WorldOverlayRenderer
import net.bdew.lib.Client
import net.bdew.lib.block.BlockRef
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.MovingObjectPosition
import org.lwjgl.opengl.GL11

object WirelessOverlayRender extends WorldOverlayRenderer {
  override def doRender(
      partialTicks: Float,
      viewX: Double,
      viewY: Double,
      viewZ: Double
  ): Unit = {
    val mop = Client.minecraft.objectMouseOver
    if (
      mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
    ) {
      val pos = BlockRef(mop.blockX, mop.blockY, mop.blockZ)
      for {
        tile <- pos.getTile[TileWireless](Client.world)
        other <- tile.link.value
      } {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glLineWidth(4.0f)

        val tess = Tessellator.instance
        tess.startDrawing(GL11.GL_LINES)
        tess.setColorRGBA_F(0, 0, 1, 1)
        tess.addVertex(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d)
        tess.addVertex(other.x + 0.5d, other.y + 0.5d, other.z + 0.5d)
        tess.draw()

        GL11.glPopAttrib()
      }
    }
  }
}
