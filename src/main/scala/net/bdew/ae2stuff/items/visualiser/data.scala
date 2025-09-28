/*
 * Copyright (c) bdew, 2014 - 2015
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.ae2stuff.items.visualiser

import java.io._

import net.bdew.ae2stuff.AE2Stuff

object VNodeFlags extends Enumeration {
  val DENSE, MISSING, PROXY = Value
}

object VLinkFlags extends Enumeration {
  val DENSE, COMPRESSED, PROXY = Value
}

case class VNode(x: Int, y: Int, z: Int, flags: VNodeFlags.ValueSet)

case class VLink(
    node1: VNode,
    node2: VNode,
    channels: Int,
    flags: VLinkFlags.ValueSet
)

class VisualisationData(var nodes: Seq[VNode], var links: Seq[VLink])
    extends Externalizable {
  def this() = this(Seq.empty, Seq.empty)

  override def readExternal(in: ObjectInput): Unit = {
    val ver = in.readInt()
    if (ver != VisualisationData.VERSION) {
      AE2Stuff.logWarn(
        "Visualisation data version mismatch, expected %d, got %d - make sure client/server versions are not mismatched"
      )
    } else {
      val nodeCount = in.readInt()
      val linkCount = in.readInt()
      nodes = Vector.empty ++ (for (x <- 0 until nodeCount) yield {
        val x = in.readInt()
        val y = in.readInt()
        val z = in.readInt()
        val f = in.readByte()
        VNode(x, y, z, VNodeFlags.ValueSet.fromBitMask(Array(f.toLong)))
      })

      links = for (x <- 0 until linkCount) yield {
        val n1 = in.readInt()
        val n2 = in.readInt()
        val c = in.readInt()
        val f = in.readByte()
        VLink(
          nodes(n1),
          nodes(n2),
          c,
          VLinkFlags.ValueSet.fromBitMask(Array(f.toLong))
        )
      }
    }
  }

  override def writeExternal(out: ObjectOutput): Unit = {
    out.writeInt(VisualisationData.VERSION)
    out.writeInt(nodes.size)
    out.writeInt(links.size)
    for (n <- nodes) {
      out.writeInt(n.x)
      out.writeInt(n.y)
      out.writeInt(n.z)
      out.writeByte(n.flags.toBitMask(0).toByte)
    }

    val nodeMap = nodes.zipWithIndex.toMap

    for (l <- links) {
      out.writeInt(nodeMap(l.node1))
      out.writeInt(nodeMap(l.node2))
      out.writeInt(l.channels)
      out.writeByte(l.flags.toBitMask(0).toByte)
    }
  }
}

object VisualisationData {
  final val VERSION = 1
}

object VisualisationModes extends Enumeration {
  val FULL, NODES, CHANNELS, NONUM, P2P, PROXY = Value
}
