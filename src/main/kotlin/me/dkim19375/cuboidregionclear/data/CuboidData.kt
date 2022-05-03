/*
 *     CuboidRegionClear, A spigot plugin that clears a cuboid region
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.cuboidregionclear.data

import me.dkim19375.cuboidregionclear.util.ConfigurationException
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.mattstudios.config.annotations.Name
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import kotlin.math.max
import kotlin.math.min

data class CuboidData(
    var world: String = "world",
    @Name("min-x")
    var minX: Int = 0,
    @Name("min-y")
    var minY: Int = 0,
    @Name("min-z")
    var minZ: Int = 0,
    @Name("max-x")
    var maxX: Int = 0,
    @Name("max-y")
    var maxY: Int = 0,
    @Name("max-z")
    var maxZ: Int = 0
) {
    val bukkitWorld: World
        get() = Bukkit.getWorld(world) ?: throw ConfigurationException("World $world does not exist")

    fun getMinLocation(): Location = LocationWrapper(
        world = bukkitWorld,
        x = min(minX, maxX),
        y = min(minY, maxY),
        z = min(minZ, maxZ)
    ).getLocation()

    fun getMaxLocation(): Location = LocationWrapper(
        world = bukkitWorld,
        x = max(minX, maxX),
        y = max(minY, maxY),
        z = max(minZ, maxZ)
    ).getLocation()

    // ty to WorldEdit
    fun iterator(): Iterator<Location> = object : Iterator<Location> {
        private val min = getMinLocation()
        private val max = getMaxLocation()
        private var nextX = min.blockX
        private var nextY = min.blockY
        private var nextZ = min.blockZ

        override fun hasNext(): Boolean = nextX != Integer.MIN_VALUE

        override fun next(): Location {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val answer = LocationWrapper(bukkitWorld, nextX, nextY, nextZ)
            if (++nextX > max.blockX) {
                nextX = min.blockX
                if (++nextZ > max.blockZ) {
                    nextZ = min.blockZ
                    if (++nextY > max.blockY) {
                        nextX = Integer.MIN_VALUE
                    }
                }
            }
            return answer.getLocation()
        }

    }
}