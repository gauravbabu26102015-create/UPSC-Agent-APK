package com.raushan.upscagent.ui.agent

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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raushan.upscagent.R
import com.raushan.upscagent.data.model.Document
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.ui.reader.ReaderActivity
import com.raushan.upscagent.utils.MotivationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AgentFragment : Fragment() {

    private lateinit var repository: AppRepository
    private lateinit var docAdapter: DocumentAdapter

    private val pickDocument = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Take persistable permission
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "document"
                val mimeType = requireContext().contentResolver.getType(uri) ?: ""
                val fileType = when {
                    mimeType.contains("pdf") -> "pdf"
                    mimeType.contains("html") -> "html"
                    mimeType.contains("text") -> "txt"
                    mimeType.contains("word") || mimeType.contains("docx") -> "doc"
                    mimeType.contains("epub") -> "epub"
                    else -> "other"
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    repository.insertDocument(
                        Document(
                            name = fileName,
                            filePath = uri.toString(),
                            fileType = fileType
                        )
                    )
                    Toast.makeText(requireContext(), "Document added: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_agent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository(requireContext())

        // ====== MOTIVATION SECTION ======
        val quote = MotivationHelper.getDailyQuote()
        view.findViewById<TextView>(R.id.tv_agent_quote).text = "\"${quote.quote}\""
        view.findViewById<TextView>(R.id.tv_agent_quote_author).text = "— ${quote.author}"

        view.findViewById<MaterialCardView>(R.id.card_new_motivation).setOnClickListener {
            val newQuote = MotivationHelper.getRandomQuote()
            view.findViewById<TextView>(R.id.tv_agent_quote).text = "\"${newQuote.quote}\""
            view.findViewById<TextView>(R.id.tv_agent_quote_author).text = "— ${newQuote.author}"
        }

        // ====== STUDY TIP ======
        view.findViewById<TextView>(R.id.tv_agent_tip).text = MotivationHelper.getRandomStudyTip()
        view.findViewById<MaterialCardView>(R.id.card_new_tip).setOnClickListener {
            view.findViewById<TextView>(R.id.tv_agent_tip).text = MotivationHelper.getRandomStudyTip()
        }

        // ====== SUBJECT ADVICE ======
        view.findViewById<MaterialCardView>(R.id.card_subject_advice).setOnClickListener {
            showSubjectAdviceDialog()
        }

        // ====== DAILY ADVICE ======
        view.findViewById<TextView>(R.id.tv_agent_daily_advice).text = MotivationHelper.getDailyAdvice()

        // ====== DOCUMENT READER SECTION ======
        docAdapter = DocumentAdapter(
            onOpen = { doc -> openDocument(doc) },
            onDelete = { doc -> deleteDocument(doc) }
        )

        view.findViewById<RecyclerView>(R.id.rv_documents).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = docAdapter
        }

        view.findViewById<MaterialCardView>(R.id.card_add_document).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf",
                    "text/plain",
                    "text/html",
                    "application/xhtml+xml",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/epub+zip"
                ))
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickDocument.launch(intent)
        }

        // Observe documents
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllDocuments().collectLatest { docs ->
                docAdapter.submitList(docs)
                view.findViewById<TextView>(R.id.tv_empty_docs)?.visibility =
                    if (docs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showSubjectAdviceDialog() {
        val subjects = arrayOf(
            "History", "Geography", "Polity", "Economy",
            "Science", "Environment", "Art & Culture",
            "Ethics", "Current Affairs", "CSAT"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Get Subject-Specific Advice")
            .setItems(subjects) { _, which ->
                val advice = MotivationHelper.getSubjectAdvice(subjects[which])
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("📖 ${subjects[which]} Strategy")
                    .setMessage(advice)
                    .setPositiveButton("Got it!", null)
                    .show()
            }
            .show()
    }

    private fun openDocument(doc: Document) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.updateDocumentLastOpened(doc.id)
        }
        val intent = Intent(requireContext(), ReaderActivity::class.java).apply {
            putExtra("file_uri", doc.filePath)
            putExtra("file_name", doc.name)
            putExtra("file_type", doc.fileType)
        }
        startActivity(intent)
    }

    private fun deleteDocument(doc: Document) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Document")
            .setMessage("Remove \"${doc.name}\" from your library?")
            .setPositiveButton("Remove") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.deleteDocument(doc)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ==================== DOCUMENT ADAPTER ====================

class DocumentAdapter(
    private val onOpen: (Document) -> Unit,
    private val onDelete: (Document) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

    private var documents = listOf<Document>()

    fun submitList(list: List<Document>) {
        documents = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(documents[position])
    override fun getItemCount() = documents.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(doc: Document) {
            val icon = when (doc.fileType) {
                "pdf" -> "📕"
                "html" -> "🌐"
                "txt" -> "📄"
                "doc" -> "📘"
                else -> "📎"
            }
            itemView.findViewById<TextView>(R.id.tv_doc_icon).text = icon
            itemView.findViewById<TextView>(R.id.tv_doc_name).text = doc.name
            itemView.findViewById<TextView>(R.id.tv_doc_type).text = doc.fileType.uppercase()
            itemView.setOnClickListener { onOpen(doc) }
            itemView.setOnLongClickListener { onDelete(doc); true }
        }
    }
}
