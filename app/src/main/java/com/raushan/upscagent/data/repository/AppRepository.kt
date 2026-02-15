package com.raushan.upscagent.data.repository

import android.content.Context
import com.raushan.upscagent.data.db.AppDatabase
import com.raushan.upscagent.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val alarmDao = db.studyAlarmDao()
    private val quizDao = db.quizDao()
    private val resultDao = db.quizResultDao()
    private val affairDao = db.currentAffairDao()
    private val sessionDao = db.studySessionDao()
    private val documentDao = db.documentDao()

    // Alarms
    fun getAllAlarms(): Flow<List<StudyAlarm>> = alarmDao.getAllAlarms()
    suspend fun getEnabledAlarms() = alarmDao.getEnabledAlarms()
    suspend fun getAlarmById(id: Int) = alarmDao.getAlarmById(id)
    suspend fun insertAlarm(alarm: StudyAlarm) = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: StudyAlarm) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: StudyAlarm) = alarmDao.deleteAlarm(alarm)
    suspend fun toggleAlarm(id: Int, enabled: Boolean) = alarmDao.toggleAlarm(id, enabled)

    // Quizzes
    fun getAllQuizzes(): Flow<List<Quiz>> = quizDao.getAllQuizzes()
    suspend fun getQuizById(id: Int) = quizDao.getQuizById(id)
    suspend fun insertQuiz(quiz: Quiz) = quizDao.insertQuiz(quiz)
    suspend fun deleteQuiz(quiz: Quiz) = quizDao.deleteQuiz(quiz)
    suspend fun getQuestionsForQuiz(quizId: Int) = quizDao.getQuestionsForQuiz(quizId)
    suspend fun insertQuestions(questions: List<Question>) = quizDao.insertQuestions(questions)
    suspend fun insertQuestion(question: Question) = quizDao.insertQuestion(question)

    // Results
    fun getAllResults(): Flow<List<QuizResult>> = resultDao.getAllResults()
    fun getResultsBySubject(subject: String) = resultDao.getResultsBySubject(subject)
    suspend fun getAverageBySubject(subject: String) = resultDao.getAverageBySubject(subject)
    suspend fun insertResult(result: QuizResult) = resultDao.insertResult(result)

    // Current Affairs
    fun getAllAffairs(): Flow<List<CurrentAffair>> = affairDao.getAllAffairs()
    fun getAffairsByCategory(category: String) = affairDao.getAffairsByCategory(category)
    fun getBookmarkedAffairs() = affairDao.getBookmarkedAffairs()
    suspend fun getAffairById(id: Int) = affairDao.getAffairById(id)
    suspend fun insertAffairs(affairs: List<CurrentAffair>) = affairDao.insertAffairs(affairs)
    suspend fun updateAffair(affair: CurrentAffair) = affairDao.updateAffair(affair)
    suspend fun markAsRead(id: Int) = affairDao.markAsRead(id)
    suspend fun toggleBookmark(id: Int, bookmarked: Boolean) = affairDao.toggleBookmark(id, bookmarked)

    // Study Sessions
    fun getAllSessions(): Flow<List<StudySession>> = sessionDao.getAllSessions()
    suspend fun getTotalMinutesForDate(date: String) = sessionDao.getTotalMinutesForDate(date)
    suspend fun getSubjectWiseTotal() = sessionDao.getSubjectWiseTotal()
    suspend fun insertSession(session: StudySession) = sessionDao.insertSession(session)

    // Documents
    fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()
    fun getFavoriteDocuments() = documentDao.getFavoriteDocuments()
    suspend fun insertDocument(document: Document) = documentDao.insertDocument(document)
    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)
    suspend fun updateDocumentLastOpened(id: Int) = documentDao.updateLastOpened(id, System.currentTimeMillis())
}
