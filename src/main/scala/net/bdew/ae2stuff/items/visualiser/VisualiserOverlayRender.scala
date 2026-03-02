/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.items.visualiser

import net.bdew.ae2stuff.items.visualiser
import net.bdew.ae2stuff.misc.{OverlayRenderHandler, WorldOverlayRenderer}
import net.bdew.ae2stuff.network.{MsgVisualisationData, NetHandler}
import net.bdew.lib.Client
import net.bdew.lib.block.BlockRef
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11

object VisualiserOverlayRender extends WorldOverlayRenderer {
  var currentLinks = new VisualisationData()
  var dense, normal = Seq.empty[VLink]

  val staticList = GL11.glGenLists(1)
  var needListRefresh = true

  final val SIZE = 0.2d

  // Abiliy to edit the overlay colors via the resoruce pack override.
  // Example: ae2stuff.visualiser.color.node.missing=0x1AB13E53.
  private def parseArgb(value: String): Option[(Int, Int, Int, Int)] = {
    val raw = value.trim
    val hex =
      if (raw.startsWith("0x") || raw.startsWith("0X")) raw.substring(2)
      else raw
    if (hex.length != 6 && hex.length != 8) return None
    try {
      val v = java.lang.Long.parseLong(hex, 16)
      val a = if (hex.length == 8) ((v >> 24) & 0xff).toInt else 0xff
      val r = ((v >> 16) & 0xff).toInt
      val g = ((v >> 8) & 0xff).toInt
      val b = (v & 0xff).toInt
      Some((r, g, b, a))
    } catch {
      case _: NumberFormatException => None
    }
  }

  private def colorFromLang(
      key: String,
      default: (Int, Int, Int, Int)
  ): (Int, Int, Int, Int) = {
    val raw = I18n.format(key)
    if (raw == key) default
    else parseArgb(raw).getOrElse(default)
  }

  private def shade(
      color: (Int, Int, Int, Int),
      num: Int,
      denom: Int
  ): (Int, Int, Int, Int) =
    (
      color._1 * num / denom,
      color._2 * num / denom,
      color._3 * num / denom,
      color._4
    )

  NetHandler.regClientHandler { case MsgVisualisationData(data) =>
    currentLinks = data
    val (dense1, normal1) =
      currentLinks.links.partition(_.flags.contains(VLinkFlags.DENSE))
    dense = dense1
    normal = normal1
    needListRefresh = true
  }

  def renderNodes(mode: VisualisationModes.Value): Unit = {
    val tess = Tessellator.instance
    tess.startDrawing(GL11.GL_QUADS)

    for (
      node <- currentLinks.nodes if (mode match {
        case VisualisationModes.NODES => !node.flags.contains(VNodeFlags.PROXY)
        case VisualisationModes.CHANNELS => false
        case VisualisationModes.NO_NUM => !node.flags.contains(VNodeFlags.PROXY)
        case VisualisationModes.P2P    => false
        case VisualisationModes.PROXY  => node.flags.contains(VNodeFlags.PROXY)
        case _                         => true
      })
    ) {
      val color =
        if (node.flags.contains(VNodeFlags.MISSING))
          colorFromLang(
            "ae2stuff.visualiser.color.node.missing",
            (255, 0, 0, 255)
          )
        else if (node.flags.contains(VNodeFlags.DENSE))
          colorFromLang(
            "ae2stuff.visualiser.color.node.dense",
            (255, 255, 0, 255)
          )
        else if (node.flags.contains(VNodeFlags.PROXY))
          colorFromLang(
            "ae2stuff.visualiser.color.node.proxy",
            (255, 165, 0, 255)
          )
        else
          colorFromLang(
            "ae2stuff.visualiser.color.node.default",
            (0, 0, 255, 255)
          )

      tess.setColorRGBA(color._1, color._2, color._3, color._4) // +Y
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )

      val shadeHalf = shade(color, 1, 2)
      tess.setColorRGBA(
        shadeHalf._1,
        shadeHalf._2,
        shadeHalf._3,
        shadeHalf._4
      ) // -Y
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      val shade80 = shade(color, 8, 10)
      tess.setColorRGBA(
        shade80._1,
        shade80._2,
        shade80._3,
        shade80._4
      ) // +/- Z
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )

      val shade60 = shade(color, 6, 10)
      tess.setColorRGBA(
        shade60._1,
        shade60._2,
        shade60._3,
        shade60._4
      ) // +/- X
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d + SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d + SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d + SIZE,
        node.z + 0.5d - SIZE
      )
      tess.addVertex(
        node.x + 0.5d - SIZE,
        node.y + 0.5d - SIZE,
        node.z + 0.5d - SIZE
      )
    }

    tess.draw()
  }

  def renderLinks(
      links: Seq[VLink],
      width: Float,
      mode: VisualisationModes.Value,
      loc: BlockRef
  ): Unit = {
    GL11.glLineWidth(width)
    val tess = Tessellator.instance
    tess.startDrawing(GL11.GL_LINES)

    for (
      link <- links if (mode match {
        case VisualisationModes.NODES => false
        case VisualisationModes.CHANNELS =>
          !link.flags.contains(VLinkFlags.PROXY)
        case VisualisationModes.NO_NUM => !link.flags.contains(VLinkFlags.PROXY)
        case VisualisationModes.NODES_ONE_CHANNEL |
            VisualisationModes.ONE_CHANNEL =>
          isLocPartOfLink(link, loc) && !isLinkBetweenAdjacentBlocks(link)
        case VisualisationModes.P2P =>
          link.flags.contains(VLinkFlags.COMPRESSED)
        case VisualisationModes.PROXY => link.flags.contains(VLinkFlags.PROXY)
        case _                        => true
      })
    ) {
      val color =
        if (link.flags.contains(VLinkFlags.COMPRESSED))
          colorFromLang(
            "ae2stuff.visualiser.color.link.compressed",
            (255, 0, 255, 255)
          )
        else if (link.flags.contains(VLinkFlags.DENSE))
          colorFromLang(
            "ae2stuff.visualiser.color.link.dense",
            (255, 255, 0, 255)
          )
        else if (link.flags.contains(VLinkFlags.PROXY))
          colorFromLang(
            "ae2stuff.visualiser.color.link.proxy",
            (255, 165, 0, 255)
          )
        else
          colorFromLang(
            "ae2stuff.visualiser.color.link.default",
            (0, 0, 255, 255)
          )
      tess.setColorRGBA(color._1, color._2, color._3, color._4)

      tess.addVertex(
        link.node1.x + 0.5d,
        link.node1.y + 0.5d,
        link.node1.z + 0.5d
      )
      tess.addVertex(
        link.node2.x + 0.5d,
        link.node2.y + 0.5d,
        link.node2.z + 0.5d
      )
    }

    tess.draw()
  }

  private def isLinkBetweenAdjacentBlocks(link: VLink): Boolean = {
    val dx = math.abs(link.node1.x - link.node2.x)
    val dy = math.abs(link.node1.y - link.node2.y)
    val dz = math.abs(link.node1.z - link.node2.z)

    (dx + dy + dz) == 1
  }

  private def isLocPartOfLink(link: VLink, loc: BlockRef) =
    isNodeLoc(link.node1, loc) || isNodeLoc(link.node2, loc)

  private def isNodeLoc(vnode: VNode, loc: BlockRef): Boolean =
    vnode.x == loc.x && vnode.y == loc.y && vnode.z == loc.z

  val renderNodesModes = Set(
    VisualisationModes.NODES,
    VisualisationModes.FULL,
    VisualisationModes.NO_NUM,
    VisualisationModes.NODES_ONE_CHANNEL,
    VisualisationModes.PROXY
  )
  val renderLinksModes = Set(
    VisualisationModes.CHANNELS,
    VisualisationModes.FULL,
    VisualisationModes.NO_NUM,
    VisualisationModes.NODES_ONE_CHANNEL,
    VisualisationModes.ONE_CHANNEL,
    VisualisationModes.P2P,
    VisualisationModes.PROXY
  )

  override def doRender(
      partialTicks: Float,
      viewX: Double,
      viewY: Double,
      viewZ: Double
  ): Unit = {
    val stack = Client.player.inventory.getCurrentItem
    if (
      !(stack != null && stack.getItem == ItemVisualiser && stack.hasTagCompound) || !stack.getTagCompound
        .hasKey("dim")
    ) {
      return
    }
    // Do not render if in a different dimension from the bound network
    val networkDim = stack.getTagCompound.getInteger("dim")
    if (networkDim != Client.world.provider.dimensionId) {
      return
    }

    val mode = ItemVisualiser.getMode(stack)
    val loc = ItemVisualiser.getLocation(stack)

    GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    if (needListRefresh) {
      needListRefresh = false
      GL11.glNewList(staticList, GL11.GL_COMPILE)

      if (renderNodesModes.contains(mode))
        renderNodes(mode)

      GL11.glEnable(GL11.GL_LINE_SMOOTH)
      GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

      if (renderLinksModes.contains(mode)) {
        renderLinks(dense, 16f, mode, loc)
        renderLinks(normal, 4f, mode, loc)
      }

      GL11.glEndList()
    }

    GL11.glCallList(staticList)

    // Labels are rendered every frame because they need to face the camera

    if (mode == VisualisationModes.FULL) {
      for (link <- currentLinks.links if link.channels > 0) {
        val linkX = (link.node1.x + link.node2.x) / 2d + 0.5d
        val linkY = (link.node1.y + link.node2.y) / 2d + 0.5d
        val linkZ = (link.node1.z + link.node2.z) / 2d + 0.5d
        val distSq =
          (viewX - linkX) * (viewX - linkX) + (viewY - linkY) * (viewY - linkY) + (viewZ - linkZ) * (viewZ - linkZ)
        if (distSq < 256d) { // 16 blocks
          OverlayRenderHandler.renderFloatingText(
            link.channels.toString,
            linkX,
            linkY,
            linkZ,
            0xffffff
          )
        }
      }
    }

    GL11.glPopAttrib()
  }
}
