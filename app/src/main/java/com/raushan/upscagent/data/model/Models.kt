package com.raushan.upscagent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ==================== ALARM / STUDY SCHEDULE ====================

@Entity(tableName = "study_alarms")
data class StudyAlarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Mon-Sun
    val alarmTone: String = "default",
    val vibrate: Boolean = true,
    val notes: String = "",
    val colorHex: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== QUIZ ====================

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val sourceFile: String = "",
    val totalQuestions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String = "",
    val questionNumber: Int = 0
)

// ==================== QUIZ RESULT ====================

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: Int,
    val quizTitle: String,
    val subject: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val skipped: Int,
    val timeTakenSeconds: Long,
    val percentage: Float,
    val attemptedAt: Long = System.currentTimeMillis()
)

// ==================== CURRENT AFFAIRS ====================

@Entity(tableName = "current_affairs")
data class CurrentAffair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summary: String,
    val content: String = "",
    val source: String,
    val sourceUrl: String,
    val category: String, // "National", "International", "Economy", "Science", "Environment"
    val imageUrl: String = "",
    val publishedDate: String,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val fetchedAt: Long = System.currentTimeMillis()
)

// ==================== STUDY PROGRESS ====================

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val durationMinutes: Int,
    val date: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== MOTIVATION ====================

data class MotivationalQuote(
    val quote: String,
    val author: String,
    val category: String = "General"
)

// ==================== DOCUMENTS ====================

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String,
    val fileType: String, // "pdf", "html", "txt", "doc"
    val fileSize: Long = 0,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

// ==================== UPSC SUBJECT CATEGORIES ====================

data class SubjectCategory(
    val name: String,
    val iconResId: Int,
    val colorHex: String,
    val subtopics: List<String>
)

// ==================== TYPE CONVERTERS ====================

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return Gson().toJson(list)
    }
}
