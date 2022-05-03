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

import me.dkim19375.cuboidregionclear.util.toDayOfWeek
import me.dkim19375.dkimcore.extension.containsIgnoreCase
import me.dkim19375.dkimcore.file.YamlFile
import me.mattstudios.config.annotations.Name
import java.time.DayOfWeek
import java.time.LocalDate

data class RegionData(
    var cuboid: CuboidData = CuboidData(),
    @Name("day-of-week")
    var dayOfWeek: String = "mondays",
    @Name("time-of-days")
    var timeOfDays: Set<String> = setOf(
        "18:00"
    ),
) {
    fun getDayOfWeek(): DayOfWeek? = dayOfWeek.toDayOfWeek()

    fun getMessageData(name: String, config: YamlFile): MessageConfigData? = config.get(MainConfigData.MESSAGES)
        .values
        .firstOrNull { configData ->
            configData.regions.containsIgnoreCase(name)
        }

    fun isCurrent(): Boolean {
        val dayOfWeek = getDayOfWeek()
        if (dayOfWeek != null) {
            if (LocalDate.now().dayOfWeek != dayOfWeek) {
                return false
            }
        }
        val now = DateData.getCurrentTime()
        val dates = timeOfDays.map { DateData.fromString(it) }
        return dates.any(now::equals)
    }
}