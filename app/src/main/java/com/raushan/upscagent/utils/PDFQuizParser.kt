package com.raushan.upscagent.utils

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.raushan.upscagent.data.model.Question
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * Parses PDF/Text files containing questions and converts them to Quiz format.
 *
 * Supported question formats:
 * 1. Which of the following...?
 *    (a) Option A
 *    (b) Option B
 *    (c) Option C
 *    (d) Option D
 *    Answer: (a) or Answer: A
 *
 * Also supports: A), a., 1., numbered formats
 */
object PDFQuizParser {

    data class ParseResult(
        val questions: List<Question>,
        val title: String,
        val errors: List<String>
    )

    fun parseFromUri(context: Context, uri: Uri, quizId: Int): ParseResult {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return when {
            mimeType.contains("pdf") -> parsePdfUri(context, uri, quizId)
            mimeType.contains("text") || mimeType.contains("html") -> parseTextUri(context, uri, quizId)
            else -> parseTextUri(context, uri, quizId) // Try text parsing as fallback
        }
    }

    private fun parsePdfUri(context: Context, uri: Uri, quizId: Int): ParseResult {
        val errors = mutableListOf<String>()
        val text = StringBuilder()

        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val renderer = PdfRenderer(pfd)
                // Note: PdfRenderer renders pages as images, not text
                // For text extraction, we use a simpler approach
                renderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            errors.add("PDF rendering error: ${e.message}")
        }

        // Try reading as text stream (works for text-based PDFs)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line = reader.readLine()
                while (line != null) {
                    text.appendLine(line)
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            errors.add("Stream reading error: ${e.message}")
        }

        val questions = parseQuestionsFromText(text.toString(), quizId)
        val title = extractTitle(text.toString())
        return ParseResult(questions, title, errors)
    }

    private fun parseTextUri(context: Context, uri: Uri, quizId: Int): ParseResult {
        val errors = mutableListOf<String>()
        val text = StringBuilder()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line = reader.readLine()
                while (line != null) {
                    text.appendLine(line)
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            errors.add("File reading error: ${e.message}")
        }

        val questions = parseQuestionsFromText(text.toString(), quizId)
        val title = extractTitle(text.toString())
        return ParseResult(questions, title, errors)
    }

    fun parseQuestionsFromText(text: String, quizId: Int): List<Question> {
        val questions = mutableListOf<Question>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var i = 0
        var questionNumber = 0

        while (i < lines.size) {
            // Try to find a question line
            val questionMatch = findQuestionLine(lines, i)
            if (questionMatch != null) {
                val (questionText, nextIndex) = questionMatch
                i = nextIndex

                // Try to find 4 options
                val options = mutableListOf<String>()
                var optionIndex = i
                while (options.size < 4 && optionIndex < lines.size) {
                    val optionMatch = findOptionLine(lines[optionIndex])
                    if (optionMatch != null) {
                        options.add(optionMatch)
                        optionIndex++
                    } else if (options.isNotEmpty()) {
                        // If we started finding options but this line isn't one, break
                        break
                    } else {
                        optionIndex++
                    }
                }
                i = optionIndex

                // Try to find answer
                var correctAnswer = "A"
                var explanation = ""
                if (i < lines.size) {
                    val answerMatch = findAnswerLine(lines[i])
                    if (answerMatch != null) {
                        correctAnswer = answerMatch.first
                        explanation = answerMatch.second
                        i++
                    }
                    // Check next line for explanation
                    if (i < lines.size && lines[i].lowercase().startsWith("explanation")) {
                        explanation = lines[i].substringAfter(":").trim()
                        i++
                    }
                }

                if (options.size >= 4) {
                    questionNumber++
                    questions.add(
                        Question(
                            quizId = quizId,
                            questionText = questionText,
                            optionA = options[0],
                            optionB = options[1],
                            optionC = options[2],
                            optionD = options[3],
                            correctAnswer = correctAnswer,
                            explanation = explanation,
                            questionNumber = questionNumber
                        )
                    )
                }
            } else {
                i++
            }
        }

        return questions
    }

    private fun findQuestionLine(lines: List<String>, startIndex: Int): Pair<String, Int>? {
        if (startIndex >= lines.size) return null
        val line = lines[startIndex]

        // Pattern: "Q1.", "Q.1", "1.", "1)", "Question 1:", etc.
        val patterns = listOf(
            Pattern.compile("^Q\\.?\\s*\\d+[.):;]?\\s*(.+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^Question\\s*\\d*[.):;]?\\s*(.+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^\\d+[.):]\\s*(.+)"),
            Pattern.compile("^(?:Which|What|Who|When|Where|How|Why|Consider|Select|Choose|Identify|Name|The|In|With|Under|According)\\s+.+\\?", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                val questionText = if (matcher.groupCount() > 0) {
                    matcher.group(1) ?: line
                } else {
                    line
                }

                // Check if question continues on next lines (before options appear)
                val fullQuestion = StringBuilder(questionText)
                var nextIdx = startIndex + 1
                while (nextIdx < lines.size) {
                    val nextLine = lines[nextIdx]
                    if (findOptionLine(nextLine) != null || findQuestionLine(listOf(nextLine), 0) != null) {
                        break
                    }
                    if (nextLine.isBlank()) {
                        nextIdx++
                        break
                    }
                    fullQuestion.append(" $nextLine")
                    nextIdx++
                }
                return Pair(fullQuestion.toString().trim(), nextIdx)
            }
        }
        return null
    }

    private fun findOptionLine(line: String): String? {
        val patterns = listOf(
            Pattern.compile("^\\(?[aA]\\)?[.)]?\\s*(.+)"),
            Pattern.compile("^\\(?[bB]\\)?[.)]?\\s*(.+)"),
            Pattern.compile("^\\(?[cC]\\)?[.)]?\\s*(.+)"),
            Pattern.compile("^\\(?[dD]\\)?[.)]?\\s*(.+)"),
            Pattern.compile("^[1-4][.)]\\s*(.+)")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                return matcher.group(1)?.trim() ?: line
            }
        }
        return null
    }

    private fun findAnswerLine(line: String): Pair<String, String>? {
        val patterns = listOf(
            Pattern.compile("^(?:Answer|Ans|Correct)[.:\\s]*\\(?([A-Da-d])\\)?[.\\s]*(.*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(?:Answer|Ans|Correct)[.:\\s]*([1-4])[.\\s]*(.*)", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                val raw = matcher.group(1)?.uppercase() ?: "A"
                val answer = when (raw) {
                    "1" -> "A"; "2" -> "B"; "3" -> "C"; "4" -> "D"
                    else -> raw
                }
                val explanation = matcher.group(2)?.trim() ?: ""
                return Pair(answer, explanation)
            }
        }
        return null
    }

    private fun extractTitle(text: String): String {
        val firstLines = text.lines().take(5).map { it.trim() }.filter { it.isNotEmpty() }
        return firstLines.firstOrNull()?.take(100) ?: "Imported Quiz"
    }
}
