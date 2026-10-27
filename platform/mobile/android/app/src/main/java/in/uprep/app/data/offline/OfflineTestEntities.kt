package `in`.uprep.app.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey

// A test downloaded for offline use, explicitly triggered by the student
// while online (mirrors the SD-card "pick a folder" mental model — nothing
// auto-caches silently). Mirrors the shape of GET /api/tests/{id} minus
// anything answer-related.
@Entity(tableName = "cached_tests")
data class CachedTest(
    @PrimaryKey val testId: String,
    val name: String,
    val durationMin: Int,
    val totalMarks: Int,
    val code: String?,
    val downloadedAt: Long
)

// One row per question in a downloaded test. `optionsJson` holds the
// options array pre-serialized (Gson) rather than going through a Room
// TypeConverter — options are small and never queried independently, so a
// plain JSON column is simpler than wiring converters for one field.
// Correct answers are never part of this (or any) offline-cached shape —
// GET /api/tests/{id} already omits them server-side (see submit route's
// comment on the separate `answers` collection).
@Entity(tableName = "cached_questions")
data class CachedQuestion(
    @PrimaryKey val id: String,
    val testId: String,
    val content: String,
    val type: String,
    val board: String?,
    val orderIndex: Int,
    val optionsJson: String
)
