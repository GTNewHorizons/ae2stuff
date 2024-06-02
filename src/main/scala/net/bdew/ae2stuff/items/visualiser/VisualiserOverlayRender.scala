/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.items.visualiser

import net.bdew.ae2stuff.misc.{OverlayRenderHandler, WorldOverlayRenderer}
import net.bdew.ae2stuff.network.{MsgVisualisationData, NetHandler}
import net.bdew.lib.Client
import org.lwjgl.opengl.GL11

object VisualiserOverlayRender extends WorldOverlayRenderer {
  var currentLinks = new VisualisationData()
  var dense, normal = Seq.empty[VLink]

  val staticList = GL11.glGenLists(1)
  var needListRefresh = true

  final val SIZE = 0.2d

  NetHandler.regClientHandler { case MsgVisualisationData(data) =>
    currentLinks = data
    val (dense1, normal1) =
      currentLinks.links.partition(_.flags.contains(VLinkFlags.DENSE))
    dense = dense1
    normal = normal1
    needListRefresh = true
  }

  def setColor(rgb: (Double, Double, Double), mul: Double): Unit = {
    GL11.glColor3d(rgb._1 * mul, rgb._2 * mul, rgb._3 * mul)
  }

  def renderNodes(): Unit = {
    GL11.glBegin(GL11.GL_QUADS)

    for (node <- currentLinks.nodes) {
      val color =
        if (node.flags.contains(VNodeFlags.MISSING))
          (1d, 0d, 0d)
        else if (node.flags.contains(VNodeFlags.DENSE))
          (1d, 1d, 0d)
        else
          (0d, 0d, 1d)

      setColor(color, 1d) // +Y
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )

      setColor(color, 0.5d) // -Y
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )

      setColor(color, 0.8d) // +/- Z
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )

      setColor(color, 0.6d) // +/- X
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      GL11.glVertex3d(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
    }

    GL11.glEnd()
  }

  def renderLinks(links: Seq[VLink], width: Float, onlyP2P: Boolean): Unit = {
    GL11.glLineWidth(width)
    GL11.glBegin(GL11.GL_LINES)

    for (
      link <- links if (!onlyP2P) || link.flags.contains(VLinkFlags.COMPRESSED)
    ) {
      if (link.flags.contains(VLinkFlags.COMPRESSED)) {
        GL11.glColor3f(1, 0, 1)
      } else if (link.flags.contains(VLinkFlags.DENSE)) {
        GL11.glColor3f(1, 1, 0)
      } else {
        GL11.glColor3f(0, 0, 1)
      }

      GL11.glVertex3d(
        link.node1.x + 0.5d,
        link.node1.y + 0.5d,
        link.node1.z + 0.5d
      )
      GL11.glVertex3d(
        link.node2.x + 0.5d,
        link.node2.y + 0.5d,
        link.node2.z + 0.5d
      )
    }

    GL11.glEnd()
  }

  val renderNodesModes = Set(
    VisualisationModes.NODES,
    VisualisationModes.FULL,
    VisualisationModes.NONUM
  )
  val renderLinksModes = Set(
    VisualisationModes.CHANNELS,
    VisualisationModes.FULL,
    VisualisationModes.NONUM,
    VisualisationModes.P2P
  )

  override def doRender(partialTicks: Float): Unit = {
    if (Client.player != null) {
      val stack = Client.player.inventory.getCurrentItem
      if (stack != null && stack.getItem == ItemVisualiser) {

        // Do not render if in a different dimension from the bound network
        val networkDim = stack.getTagCompound.getInteger("dim")
        if (networkDim != Client.world.provider.dimensionId) {
          return
        }

        val mode = ItemVisualiser.getMode(stack)

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        if (needListRefresh) {
          needListRefresh = false
          GL11.glNewList(staticList, GL11.GL_COMPILE)

          if (renderNodesModes.contains(mode))
            renderNodes()

          GL11.glEnable(GL11.GL_LINE_SMOOTH)
          GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

          if (renderLinksModes.contains(mode)) {
            renderLinks(dense, 16f, mode == VisualisationModes.P2P)
            renderLinks(normal, 4f, mode == VisualisationModes.P2P)
          }

          GL11.glEndList()
        }

        GL11.glCallList(staticList)

        // Labels are rendered every frame because they need to face the camera

        if (mode == VisualisationModes.FULL) {
          for (link <- currentLinks.links if link.channels > 0) {
            OverlayRenderHandler.renderFloatingText(
              link.channels.toString,
              (link.node1.x + link.node2.x) / 2d + 0.5d,
              (link.node1.y + link.node2.y) / 2d + 0.5d,
              (link.node1.z + link.node2.z) / 2d + 0.5d,
              0xffffff
            )
          }
        }

        GL11.glPopAttrib()
      }
    }
  }
}
