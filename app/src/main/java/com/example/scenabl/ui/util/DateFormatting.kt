package com.example.scenabl.ui.util

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")

fun Timestamp.toLocalDate(): LocalDate =
    toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun Timestamp.formatDateTime(): String =
    toDate().toInstant().atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

fun Timestamp.formatDate(): String =
    toDate().toInstant().atZone(ZoneId.systemDefault()).format(dateFormatter)

/** DateRangePicker works in UTC millis at midnight, independent of device time zone. */
fun LocalDate.toUtcPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.fromUtcPickerMillis(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
