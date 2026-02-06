package net.bdew.ae2stuff.machines.wireless

import net.bdew.lib.block.BlockRef
import scala.collection.mutable

object WirelessConnectionRenderer {
  private val pinnedConnections = mutable.Set.empty[(BlockRef, BlockRef)]
  private val pinnedHubs = mutable.Set.empty[BlockRef]
  private val hubConnections =
    mutable.Map.empty[BlockRef, mutable.Set[BlockRef]]
  private val actualHubConnections =
    mutable.Map.empty[BlockRef, mutable.Set[BlockRef]]

  def pinConnection(from: BlockRef, to: BlockRef): Unit = {
    pinnedConnections.add((from, to))
  }

  def unpinConnection(from: BlockRef, to: BlockRef): Unit = {
    pinnedConnections.remove((from, to))
  }

  def pinHub(hubPos: BlockRef): Unit = {
    pinnedHubs.add(hubPos)
    hubConnections.put(
      hubPos,
      mutable.Set
        .empty[BlockRef] ++ actualHubConnections.getOrElse(hubPos, Set.empty)
    )
  }

  def unpinHub(hubPos: BlockRef): Unit = {
    pinnedHubs.remove(hubPos)
    hubConnections.remove(hubPos)
  }

  def isHubPinned(hubPos: BlockRef): Boolean = {
    pinnedHubs.contains(hubPos)
  }

  def addActualHubConnection(hubPos: BlockRef, connectedPos: BlockRef): Unit = {
    if (!actualHubConnections.contains(hubPos)) {
      actualHubConnections.put(hubPos, mutable.Set.empty[BlockRef])
    }
    actualHubConnections(hubPos).add(connectedPos)

    if (isHubPinned(hubPos)) {
      if (!hubConnections.contains(hubPos)) {
        hubConnections.put(hubPos, mutable.Set.empty[BlockRef])
      }
      hubConnections(hubPos).add(connectedPos)
    }
  }

  def removeActualHubConnection(
      hubPos: BlockRef,
      connectedPos: BlockRef
  ): Unit = {
    actualHubConnections.get(hubPos).foreach { connections =>
      connections.remove(connectedPos)
      if (connections.isEmpty) {
        actualHubConnections.remove(hubPos)
      }
    }

    if (isHubPinned(hubPos)) {
      hubConnections.get(hubPos).foreach { connections =>
        connections.remove(connectedPos)
        if (connections.isEmpty) {
          hubConnections.remove(hubPos)
        }
      }
    }
  }

  def getActualHubConnections(hubPos: BlockRef): Set[BlockRef] = {
    actualHubConnections.get(hubPos).map(_.toSet).getOrElse(Set.empty)
  }

  def getHubConnections(hubPos: BlockRef): Set[BlockRef] = {
    hubConnections.get(hubPos).map(_.toSet).getOrElse(Set.empty)
  }

  def getPinnedConnections: Set[(BlockRef, BlockRef)] = pinnedConnections.toSet
  def getPinnedHubs: Set[BlockRef] = pinnedHubs.toSet

  def hasPinnedConnection(pos: BlockRef): Boolean = {
    pinnedConnections.exists { case (from, to) =>
      from == pos || to == pos
    } || pinnedHubs.contains(pos)
  }

  def findConnection(
      pos1: BlockRef,
      pos2: BlockRef
  ): Option[(BlockRef, BlockRef)] = {
    pinnedConnections.find { case (from, to) =>
      (from == pos1 && to == pos2) || (from == pos2 && to == pos1)
    }
  }
}
