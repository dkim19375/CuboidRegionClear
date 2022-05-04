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
    private var lastTime: DateData? = null

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val regions = config.get(MainConfigData.REGIONS)
            val time = DateData.getCurrentTime()
            if (lastTime == time) {
                return@Runnable
            }
            lastTime = time
            for ((name, region) in regions) {
                val messages = region.getMessageData(name, config)
                if (region.isCurrent()) {
                    clear(region, messages)
                    continue
                }
                messages ?: continue
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
                for ((warningTime, warning) in messages.warnings) {
                    val warningMins = warningTime.toInt()
                    val currentTimeMins = time.toEpochMinutes()
                    val newTime = DateData.fromEpochMinutes(currentTimeMins + warningMins.toLong())
                    val match = times.firstOrNull { (dateData, calendar) ->
                        dateData == newTime && LocalDateTime.ofInstant(Instant.ofEpochMilli(
                            calendar.also { it.add(Calendar.MINUTE, -warningMins) }.timeInMillis
                        ), ZoneId.systemDefault()).dayOfWeek == dayOfWeek
                    }
                    if (match != null) {
                        broadcastFormatted(warning)
                    }
                }
            }
        }, 10L, 10L)
    }

    private fun clear(region: RegionData, messages: MessageConfigData?) {
        messages?.message?.let { message ->
            broadcastFormatted(message)
        }
        for (location in region.cuboid.iterator()) {
            val block = location.block
            if (block.type.isAir) {
                continue
            }
            block.type = Material.AIR
        }
    }
}