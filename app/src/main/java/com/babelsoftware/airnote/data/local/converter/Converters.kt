package com.babelsoftware.airnote.data.local.converter

import androidx.room.TypeConverter
import com.babelsoftware.airnote.domain.model.Participant
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromParticipantName(name: String?): Participant? {
        return name?.let { Participant.valueOf(it) }
    }

    @TypeConverter
    fun participantToName(participant: Participant?): String? {
        return participant?.name
    }
}
