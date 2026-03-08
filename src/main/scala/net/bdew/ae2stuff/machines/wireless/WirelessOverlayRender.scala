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
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glEnable(GL11.GL_LINE_SMOOTH)
    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

    renderPinnedConnections()
    renderPinnedHubConnections()
    renderHoveredConnections()

    GL11.glPopAttrib()
  }

  private def renderPinnedConnections(): Unit = {
    val tess = Tessellator.instance
    GL11.glLineWidth(4.0f)

    WirelessConnectionRenderer.getPinnedConnections.foreach { case (from, to) =>
      tess.startDrawing(GL11.GL_LINES)
      tess.setColorRGBA_F(0, 0, 1, 0.8f)
      tess.addVertex(from.x + 0.5d, from.y + 0.5d, from.z + 0.5d)
      tess.addVertex(to.x + 0.5d, to.y + 0.5d, to.z + 0.5d)
      tess.draw()
    }
  }

  private def renderPinnedHubConnections(): Unit = {
    val tess = Tessellator.instance
    GL11.glLineWidth(3.0f)

    WirelessConnectionRenderer.getPinnedHubs.foreach { hubPos =>
      WirelessConnectionRenderer.getHubConnections(hubPos).foreach {
        connectedPos =>
          tess.startDrawing(GL11.GL_LINES)
          tess.setColorRGBA_F(1, 1, 0, 0.8f)
          tess.addVertex(hubPos.x + 0.5d, hubPos.y + 0.5d, hubPos.z + 0.5d)
          tess.addVertex(
            connectedPos.x + 0.5d,
            connectedPos.y + 0.5d,
            connectedPos.z + 0.5d
          )
          tess.draw()
      }
    }
  }

  private def renderHoveredConnections(): Unit = {
    val mop = Client.minecraft.objectMouseOver
    if (
      mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
    ) {
      val pos = BlockRef(mop.blockX, mop.blockY, mop.blockZ)

      if (!WirelessConnectionRenderer.hasPinnedConnection(pos)) {
        Client.world.getTileEntity(pos.x, pos.y, pos.z) match {
          case tile: TileWireless => renderTileConnections(pos, tile)
          case _                  =>
        }
      }
    }
  }

  private def renderTileConnections(pos: BlockRef, tile: TileWireless): Unit = {
    val tess = Tessellator.instance
    GL11.glLineWidth(2.0f)

    if (tile.isHub) {
      renderHubConnections(pos, tess)
    } else if (tile.link.isDefined) {
      renderPeerConnection(pos, tile.link.get, tess)
    }
  }

  private def renderHubConnections(pos: BlockRef, tess: Tessellator): Unit = {
    WirelessConnectionRenderer.getActualHubConnections(pos).foreach {
      connectedPos =>
        tess.startDrawing(GL11.GL_LINES)
        tess.setColorRGBA_F(0, 1, 0, 0.6f)
        tess.addVertex(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d)
        tess.addVertex(
          connectedPos.x + 0.5d,
          connectedPos.y + 0.5d,
          connectedPos.z + 0.5d
        )
        tess.draw()
    }
  }

  private def renderPeerConnection(
      pos: BlockRef,
      otherPos: BlockRef,
      tess: Tessellator
  ): Unit = {
    tess.startDrawing(GL11.GL_LINES)
    tess.setColorRGBA_F(0, 1, 0, 0.6f)
    tess.addVertex(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d)
    tess.addVertex(otherPos.x + 0.5d, otherPos.y + 0.5d, otherPos.z + 0.5d)
    tess.draw()
  }
}
