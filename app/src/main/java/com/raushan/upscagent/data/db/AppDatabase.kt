package com.raushan.upscagent.data.db

import android.content.Context
import androidx.room.*
import com.raushan.upscagent.data.model.*
import kotlinx.coroutines.flow.Flow

// ==================== DAOs ====================

@Dao
interface StudyAlarmDao {
    @Query("SELECT * FROM study_alarms ORDER BY startHour, startMinute")
    fun getAllAlarms(): Flow<List<StudyAlarm>>

    @Query("SELECT * FROM study_alarms WHERE isEnabled = 1 ORDER BY startHour, startMinute")
    suspend fun getEnabledAlarms(): List<StudyAlarm>

    @Query("SELECT * FROM study_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): StudyAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: StudyAlarm): Long

    @Update
    suspend fun updateAlarm(alarm: StudyAlarm)

    @Delete
    suspend fun deleteAlarm(alarm: StudyAlarm)

    @Query("UPDATE study_alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleAlarm(id: Int, enabled: Boolean)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: Int): Quiz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY questionNumber")
    suspend fun getQuestionsForQuiz(quizId: Int): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long
}

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results ORDER BY attemptedAt DESC")
    fun getAllResults(): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE subject = :subject ORDER BY attemptedAt DESC")
    fun getResultsBySubject(subject: String): Flow<List<QuizResult>>

    @Query("SELECT AVG(percentage) FROM quiz_results WHERE subject = :subject")
    suspend fun getAverageBySubject(subject: String): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResult): Long
}

@Dao
interface CurrentAffairDao {
    @Query("SELECT * FROM current_affairs ORDER BY fetchedAt DESC")
    fun getAllAffairs(): Flow<List<CurrentAffair>>

    @Query("SELECT * FROM current_affairs WHERE category = :category ORDER BY fetchedAt DESC")
    fun getAffairsByCategory(category: String): Flow<List<CurrentAffair>>

    @Query("SELECT * FROM current_affairs WHERE isBookmarked = 1 ORDER BY fetchedAt DESC")
    fun getBookmarkedAffairs(): Flow<List<CurrentAffair>>

    @Query("SELECT * FROM current_affairs WHERE id = :id")
    suspend fun getAffairById(id: Int): CurrentAffair?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffairs(affairs: List<CurrentAffair>)

    @Update
    suspend fun updateAffair(affair: CurrentAffair)

    @Query("UPDATE current_affairs SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE current_affairs SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: Int, bookmarked: Boolean)

    @Query("DELETE FROM current_affairs WHERE fetchedAt < :olderThan AND isBookmarked = 0")
    suspend fun deleteOldAffairs(olderThan: Long)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date = :date")
    suspend fun getTotalMinutesForDate(date: String): Int?

    @Query("SELECT subject, SUM(durationMinutes) as total FROM study_sessions GROUP BY subject")
    suspend fun getSubjectWiseTotal(): List<SubjectStudyTime>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long
}

data class SubjectStudyTime(
    val subject: String,
    val total: Int
)

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastOpenedAt DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY lastOpenedAt DESC")
    fun getFavoriteDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("UPDATE documents SET lastOpenedAt = :time WHERE id = :id")
    suspend fun updateLastOpened(id: Int, time: Long)
}

// ==================== DATABASE ====================

@Database(
    entities = [
        StudyAlarm::class,
        Quiz::class,
        Question::class,
        QuizResult::class,
        CurrentAffair::class,
        StudySession::class,
        Document::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyAlarmDao(): StudyAlarmDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun currentAffairDao(): CurrentAffairDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upsc_agent_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
