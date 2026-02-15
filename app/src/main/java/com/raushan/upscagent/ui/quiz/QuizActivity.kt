package com.raushan.upscagent.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raushan.upscagent.R
import com.raushan.upscagent.data.model.Question
import com.raushan.upscagent.data.model.QuizResult
import com.raushan.upscagent.data.repository.AppRepository
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var repository: AppRepository
    private var questions = listOf<Question>()
    private var currentIndex = 0
    private var score = 0
    private var wrongCount = 0
    private var skippedCount = 0
    private var selectedAnswer = ""
    private var hasAnswered = false
    private var startTime = 0L

    private lateinit var tvQuestion: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestionNum: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardA: CardView
    private lateinit var cardB: CardView
    private lateinit var cardC: CardView
    private lateinit var cardD: CardView
    private lateinit var tvOptionA: TextView
    private lateinit var tvOptionB: TextView
    private lateinit var tvOptionC: TextView
    private lateinit var tvOptionD: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        repository = AppRepository(this)
        val quizId = intent.getIntExtra("quiz_id", -1)
        val quizTitle = intent.getStringExtra("quiz_title") ?: "Quiz"

        findViewById<TextView>(R.id.tv_quiz_activity_title).text = quizTitle
        findViewById<ImageButton>(R.id.btn_back_quiz).setOnClickListener { finish() }

        // Init views
        tvQuestion = findViewById(R.id.tv_question_text)
        tvProgress = findViewById(R.id.tv_quiz_progress)
        tvQuestionNum = findViewById(R.id.tv_question_number)
        progressBar = findViewById(R.id.progress_quiz)
        cardA = findViewById(R.id.card_option_a)
        cardB = findViewById(R.id.card_option_b)
        cardC = findViewById(R.id.card_option_c)
        cardD = findViewById(R.id.card_option_d)
        tvOptionA = findViewById(R.id.tv_option_a)
        tvOptionB = findViewById(R.id.tv_option_b)
        tvOptionC = findViewById(R.id.tv_option_c)
        tvOptionD = findViewById(R.id.tv_option_d)
        tvExplanation = findViewById(R.id.tv_explanation)
        btnNext = findViewById(R.id.btn_next_question)
        btnSkip = findViewById(R.id.btn_skip_question)

        // Option click listeners
        cardA.setOnClickListener { selectOption("A") }
        cardB.setOnClickListener { selectOption("B") }
        cardC.setOnClickListener { selectOption("C") }
        cardD.setOnClickListener { selectOption("D") }

        btnNext.setOnClickListener {
            if (!hasAnswered && selectedAnswer.isNotEmpty()) {
                checkAnswer()
            } else if (hasAnswered) {
                nextQuestion()
            }
        }

        btnSkip.setOnClickListener {
            skippedCount++
            nextQuestion()
        }

        startTime = SystemClock.elapsedRealtime()

        // Load questions
        lifecycleScope.launch {
            questions = repository.getQuestionsForQuiz(quizId)
            if (questions.isEmpty()) {
                Toast.makeText(this@QuizActivity, "No questions found in this quiz", Toast.LENGTH_LONG).show()
                finish()
            } else {
                displayQuestion()
            }
        }
    }

    private fun displayQuestion() {
        hasAnswered = false
        selectedAnswer = ""

        val q = questions[currentIndex]
        tvQuestion.text = q.questionText
        tvQuestionNum.text = "Q${currentIndex + 1}"
        tvProgress.text = "${currentIndex + 1} / ${questions.size}"
        progressBar.max = questions.size
        progressBar.progress = currentIndex + 1

        tvOptionA.text = "A. ${q.optionA}"
        tvOptionB.text = "B. ${q.optionB}"
        tvOptionC.text = "C. ${q.optionC}"
        tvOptionD.text = "D. ${q.optionD}"

        // Reset card colors
        resetCardColors()
        tvExplanation.visibility = View.GONE
        btnNext.text = "Check Answer"
        btnSkip.visibility = View.VISIBLE
    }

    private fun selectOption(option: String) {
        if (hasAnswered) return
        selectedAnswer = option
        resetCardColors()

        val selectedCard = when (option) {
            "A" -> cardA; "B" -> cardB; "C" -> cardC; "D" -> cardD
            else -> return
        }
        selectedCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
    }

    private fun checkAnswer() {
        if (selectedAnswer.isEmpty()) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        hasAnswered = true
        val q = questions[currentIndex]
        val correct = q.correctAnswer.uppercase()

        if (selectedAnswer == correct) {
            score++
            getCard(selectedAnswer).setCardBackgroundColor(Color.parseColor("#C8E6C9")) // Green
        } else {
            wrongCount++
            getCard(selectedAnswer).setCardBackgroundColor(Color.parseColor("#FFCDD2")) // Red
            getCard(correct).setCardBackgroundColor(Color.parseColor("#C8E6C9")) // Green
        }

        if (q.explanation.isNotEmpty()) {
            tvExplanation.text = "💡 ${q.explanation}"
            tvExplanation.visibility = View.VISIBLE
        }

        btnNext.text = if (currentIndex < questions.size - 1) "Next Question →" else "View Results"
        btnSkip.visibility = View.GONE
    }

    private fun nextQuestion() {
        currentIndex++
        if (currentIndex < questions.size) {
            displayQuestion()
        } else {
            showResults()
        }
    }

    private fun showResults() {
        val timeTaken = (SystemClock.elapsedRealtime() - startTime) / 1000
        val percentage = if (questions.isNotEmpty()) (score.toFloat() / questions.size) * 100 else 0f
        val quizTitle = intent.getStringExtra("quiz_title") ?: "Quiz"
        val quizSubject = intent.getStringExtra("quiz_subject") ?: "General"
        val quizId = intent.getIntExtra("quiz_id", -1)

        // Save result
        lifecycleScope.launch {
            repository.insertResult(
                QuizResult(
                    quizId = quizId,
                    quizTitle = quizTitle,
                    subject = quizSubject,
                    totalQuestions = questions.size,
                    correctAnswers = score,
                    wrongAnswers = wrongCount,
                    skipped = skippedCount,
                    timeTakenSeconds = timeTaken,
                    percentage = percentage
                )
            )
        }

        val emoji = when {
            percentage >= 80 -> "🏆"
            percentage >= 60 -> "👏"
            percentage >= 40 -> "💪"
            else -> "📚"
        }

        val message = """
            $emoji Results for: $quizTitle
            
            ✅ Correct: $score / ${questions.size}
            ❌ Wrong: $wrongCount
            ⏭️ Skipped: $skippedCount
            📊 Score: ${"%.1f".format(percentage)}%
            ⏱️ Time: ${timeTaken / 60}m ${timeTaken % 60}s
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("$emoji Quiz Complete!")
            .setMessage(message)
            .setPositiveButton("Done") { _, _ -> finish() }
            .setNeutralButton("Retry") { _, _ ->
                currentIndex = 0; score = 0; wrongCount = 0; skippedCount = 0
                startTime = SystemClock.elapsedRealtime()
                displayQuestion()
            }
            .setCancelable(false)
            .show()
    }

    private fun getCard(option: String): CardView = when (option) {
        "A" -> cardA; "B" -> cardB; "C" -> cardC; "D" -> cardD
        else -> cardA
    }

    private fun resetCardColors() {
        val defaultColor = Color.parseColor("#FFFFFF")
        cardA.setCardBackgroundColor(defaultColor)
        cardB.setCardBackgroundColor(defaultColor)
        cardC.setCardBackgroundColor(defaultColor)
        cardD.setCardBackgroundColor(defaultColor)
    }
}
