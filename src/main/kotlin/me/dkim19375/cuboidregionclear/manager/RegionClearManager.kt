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

package me.dkim19375.cuboidregionclear.manager

import me.dkim19375.cuboidregionclear.CuboidRegionClear
import me.dkim19375.cuboidregionclear.data.DateData
import me.dkim19375.cuboidregionclear.data.MainConfigData
import me.dkim19375.cuboidregionclear.data.MessageConfigData
import me.dkim19375.cuboidregionclear.data.RegionData
import me.dkim19375.cuboidregionclear.util.broadcastFormatted
import me.dkim19375.cuboidregionclear.util.getCalendarValue
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.dkim19375.dkimbukkitcore.data.toWrapper
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimcore.extension.concurrentMapOf
import me.dkim19375.dkimcore.extension.setDecimalPlaces
import me.dkim19375.dkimcore.file.YamlFile
import org.bukkit.Bukkit
import org.bukkit.Material
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

class RegionClearManager(private val plugin: CuboidRegionClear) {
    private val config: YamlFile
        get() = plugin.mainConfig
    var lastTime: DateData? = null
    private val blocksToClear = concurrentMapOf<LocationWrapper, Boolean>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val regions = config.get(MainConfigData.REGIONS)
            val time = DateData.getCurrentTime()
            if (lastTime == time) {
                return@Runnable
            }
            lastTime = time
            val sentMessages = mutableMapOf<String, Set<Int>>()
            for ((name, region) in regions) {
                if (!region.enabled) {
                    continue
                }
                val messages = region.getMessageData(name, config)
                if (region.isCurrent()) {
                    clear(region, messages, sentMessages)
                    continue
                }
                messages ?: continue
                fun getTimeSet(): Set<Int> = sentMessages[messages.first] ?: emptySet()
                val times = mutableListOf<Pair<DateData, Calendar>>()
                for (timeOfDays in region.timeOfDays) {
                    val dateData = DateData.fromString(timeOfDays)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dateData.toEpochMillis()
                    val dayOfWeeks = region.getDayOfWeeks()
                    if (dayOfWeeks.isEmpty()) {
                        times.add(dateData to calendar.clone() as Calendar)
                        continue
                    }
                    for (dayOfWeek in dayOfWeeks) {
                        val clonedCalendar = calendar.clone() as Calendar
                        clonedCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek.getCalendarValue())
                        times.add(dateData to clonedCalendar)
                    }
                }
                val dayOfWeek = LocalDateTime.now().dayOfWeek
                for ((warningTime, warning) in messages.second.warnings) {
                    val warningMins = warningTime.toInt()
                    val currentTimeMins = time.toEpochMinutes()
                    val newTime = DateData.fromEpochMinutes(currentTimeMins + warningMins.toLong())
                    val match = times.firstOrNull { (dateData, calendar) ->
                        dateData == newTime && LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                calendar.also { it.add(Calendar.MINUTE, -warningMins) }.timeInMillis
                            ), ZoneId.systemDefault()
                        ).dayOfWeek == dayOfWeek
                    }
                    if (match != null) {
                        if (warningMins !in getTimeSet()) {
                            broadcastFormatted(warning)
                            sentMessages[messages.first] = getTimeSet() + warningMins
                        }
                    }
                }
            }
        }, 10L, 10L)
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val start = System.nanoTime()
            val amountToClear = plugin.mainConfig.get(MainConfigData.BLOCKS_PER_TICK)
            val blocks = blocksToClear.take(amountToClear).ifEmpty { return@Runnable }
            for ((location, updatePhysics) in blocks) {
                val state = location.getLocation().block.state
                state.type = Material.AIR
                state.update(true, updatePhysics)
            }
            val end = System.nanoTime()
            val time = ((end.toDouble() - start) / 1000000).setDecimalPlaces(3)
            logInfo("Cleared ${blocks.size} blocks in $time ms")
        }, 1L, 1L)
    }

    private fun <K, V> MutableMap<K, V>.take(amount: Int): Map<K, V> {
        val map = mutableMapOf<K, V>()
        for ((key, value) in this) {
            if (map.size >= amount) {
                break
            }
            map[key] = value
            remove(key)
        }
        return map
    }

    fun clear(
        region: RegionData,
        messages: Pair<String, MessageConfigData>? = null,
        sentMessages: MutableMap<String, Set<Int>>? = null,
    ) {
        if (messages != null) {
            val set = sentMessages?.get(messages.first) ?: emptySet()
            if (-1 !in set) {
                messages.second.message.let { message ->
                    broadcastFormatted(message)
                    sentMessages?.put(messages.first, set + -1)
                }
            }
        }
        val start = System.nanoTime()
        var amount = 0
        val edgeX = setOf(region.cuboid.minX, region.cuboid.maxX)
        val edgeY = setOf(region.cuboid.minY, region.cuboid.maxY)
        val edgeZ = setOf(region.cuboid.minZ, region.cuboid.maxZ)
        for (location in region.cuboid.iterator()) {
            val block = location.block
            if (block.type.isAir) {
                continue
            }
            amount++
            val update = location.blockX in edgeX || location.blockY in edgeY || location.blockZ in edgeZ
            blocksToClear[location.toWrapper()] = update
        }
        val end = System.nanoTime()
        val time = ((end.toDouble() - start) / 1000000).setDecimalPlaces(3)
        logInfo("Queued $amount blocks in $time ms")
    }

}