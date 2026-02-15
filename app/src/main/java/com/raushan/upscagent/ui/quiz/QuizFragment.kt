package com.raushan.upscagent.ui.quiz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.raushan.upscagent.R
import com.raushan.upscagent.data.model.Question
import com.raushan.upscagent.data.model.Quiz
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.utils.PDFQuizParser
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private lateinit var repository: AppRepository
    private lateinit var adapter: QuizListAdapter

    private val pickFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> importQuizFromFile(uri) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository(requireContext())

        adapter = QuizListAdapter(
            onPlay = { quiz -> startQuiz(quiz) },
            onDelete = { quiz -> deleteQuiz(quiz) }
        )

        view.findViewById<RecyclerView>(R.id.rv_quizzes).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@QuizFragment.adapter
        }

        // FAB - Import from PDF
        view.findViewById<FloatingActionButton>(R.id.fab_import_quiz).setOnClickListener {
            showImportOptions()
        }

        // Observe quizzes
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllQuizzes().collectLatest { quizzes ->
                adapter.submitList(quizzes)
                view.findViewById<TextView>(R.id.tv_empty_quiz)?.visibility =
                    if (quizzes.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showImportOptions() {
        val options = arrayOf(
            "📄 Import from PDF / Text File",
            "✍️ Create Quiz Manually"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Quiz")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openFilePicker()
                    1 -> showCreateQuizDialog()
                }
            }
            .show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "text/plain",
                "text/html",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }
        pickFile.launch(intent)
    }

    private fun importQuizFromFile(uri: android.net.Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val tempQuizId = 0
                val result = PDFQuizParser.parseFromUri(requireContext(), uri, tempQuizId)

                if (result.questions.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "No questions found. Ensure your file has proper Q&A format (Q1., a), b), c), d), Answer: a)",
                        Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Create the quiz
                val quiz = Quiz(
                    title = result.title,
                    subject = "Imported",
                    sourceFile = uri.lastPathSegment ?: "imported_file",
                    totalQuestions = result.questions.size
                )
                val quizId = repository.insertQuiz(quiz).toInt()

                // Insert questions with correct quizId
                val questions = result.questions.map { it.copy(quizId = quizId) }
                repository.insertQuestions(questions)

                Toast.makeText(requireContext(),
                    "✅ Imported ${questions.size} questions successfully!",
                    Toast.LENGTH_LONG).show()

                if (result.errors.isNotEmpty()) {
                    Toast.makeText(requireContext(),
                        "Warnings: ${result.errors.joinToString("; ")}",
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Import failed: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showCreateQuizDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_quiz, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.et_quiz_title)
        val etSubject = dialogView.findViewById<EditText>(R.id.et_quiz_subject)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Quiz")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                val subject = etSubject.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val quiz = Quiz(title = title, subject = subject.ifEmpty { "General" })
                        val quizId = repository.insertQuiz(quiz).toInt()
                        showAddQuestionDialog(quizId, 1)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddQuestionDialog(quizId: Int, questionNum: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_question, null)

        val etQuestion = dialogView.findViewById<EditText>(R.id.et_question_text)
        val etOptionA = dialogView.findViewById<EditText>(R.id.et_option_a)
        val etOptionB = dialogView.findViewById<EditText>(R.id.et_option_b)
        val etOptionC = dialogView.findViewById<EditText>(R.id.et_option_c)
        val etOptionD = dialogView.findViewById<EditText>(R.id.et_option_d)
        val rgCorrect = dialogView.findViewById<RadioGroup>(R.id.rg_correct_answer)
        val etExplanation = dialogView.findViewById<EditText>(R.id.et_explanation)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Question #$questionNum")
            .setView(dialogView)
            .setPositiveButton("Save & Add Next") { _, _ ->
                val correctAnswer = when (rgCorrect.checkedRadioButtonId) {
                    R.id.rb_a -> "A"; R.id.rb_b -> "B"; R.id.rb_c -> "C"; R.id.rb_d -> "D"
                    else -> "A"
                }
                val question = Question(
                    quizId = quizId,
                    questionText = etQuestion.text.toString().trim(),
                    optionA = etOptionA.text.toString().trim(),
                    optionB = etOptionB.text.toString().trim(),
                    optionC = etOptionC.text.toString().trim(),
                    optionD = etOptionD.text.toString().trim(),
                    correctAnswer = correctAnswer,
                    explanation = etExplanation.text.toString().trim(),
                    questionNumber = questionNum
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.insertQuestion(question)
                }
                showAddQuestionDialog(quizId, questionNum + 1)
            }
            .setNegativeButton("Done") { _, _ ->
                val correctAnswer = when (rgCorrect.checkedRadioButtonId) {
                    R.id.rb_a -> "A"; R.id.rb_b -> "B"; R.id.rb_c -> "C"; R.id.rb_d -> "D"
                    else -> "A"
                }
                if (etQuestion.text.toString().isNotBlank()) {
                    val question = Question(
                        quizId = quizId,
                        questionText = etQuestion.text.toString().trim(),
                        optionA = etOptionA.text.toString().trim(),
                        optionB = etOptionB.text.toString().trim(),
                        optionC = etOptionC.text.toString().trim(),
                        optionD = etOptionD.text.toString().trim(),
                        correctAnswer = correctAnswer,
                        explanation = etExplanation.text.toString().trim(),
                        questionNumber = questionNum
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.insertQuestion(question)
                    }
                }
                Toast.makeText(requireContext(), "Quiz created with $questionNum questions!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun startQuiz(quiz: Quiz) {
        val intent = Intent(requireContext(), QuizActivity::class.java).apply {
            putExtra("quiz_id", quiz.id)
            putExtra("quiz_title", quiz.title)
            putExtra("quiz_subject", quiz.subject)
        }
        startActivity(intent)
    }

    private fun deleteQuiz(quiz: Quiz) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Quiz")
            .setMessage("Delete \"${quiz.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.deleteQuiz(quiz)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ==================== QUIZ LIST ADAPTER ====================

class QuizListAdapter(
    private val onPlay: (Quiz) -> Unit,
    private val onDelete: (Quiz) -> Unit
) : RecyclerView.Adapter<QuizListAdapter.ViewHolder>() {

    private var quizzes = listOf<Quiz>()

    fun submitList(list: List<Quiz>) {
        quizzes = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(quizzes[position])
    override fun getItemCount() = quizzes.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(quiz: Quiz) {
            itemView.findViewById<TextView>(R.id.tv_quiz_title).text = quiz.title
            itemView.findViewById<TextView>(R.id.tv_quiz_subject).text = quiz.subject
            itemView.findViewById<TextView>(R.id.tv_quiz_questions).text = "${quiz.totalQuestions} questions"
            itemView.findViewById<Button>(R.id.btn_start_quiz).setOnClickListener { onPlay(quiz) }
            itemView.setOnLongClickListener { onDelete(quiz); true }
        }
    }
}
