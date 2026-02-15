package com.raushan.upscagent.utils

import com.raushan.upscagent.data.model.CurrentAffair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

object CurrentAffairsFetcher {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    suspend fun fetchFromMultipleSources(): List<CurrentAffair> = withContext(Dispatchers.IO) {
        val affairs = mutableListOf<CurrentAffair>()

        try { affairs.addAll(fetchFromInsightsIAS()) } catch (_: Exception) {}
        try { affairs.addAll(fetchFromPIB()) } catch (_: Exception) {}
        try { affairs.addAll(fetchFromGenericRSS()) } catch (_: Exception) {}

        affairs.distinctBy { it.title.take(50) }
    }

    private fun fetchFromInsightsIAS(): List<CurrentAffair> {
        val affairs = mutableListOf<CurrentAffair>()
        try {
            val doc = Jsoup.connect("https://www.insightsonindia.com/category/current-affairs/")
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            val articles = doc.select("article")
            for (article in articles.take(15)) {
                val titleEl = article.selectFirst("h2 a, h3 a, .entry-title a")
                val title = titleEl?.text()?.trim() ?: continue
                val link = titleEl.attr("href")
                val summary = article.selectFirst(".entry-content p, .entry-summary p")?.text()?.trim() ?: ""
                val dateStr = article.selectFirst(".entry-date, time")?.text()?.trim() ?: dateFormat.format(Date())

                affairs.add(
                    CurrentAffair(
                        title = title,
                        summary = summary.take(500),
                        source = "Insights on India",
                        sourceUrl = link,
                        category = categorizeAffair(title + " " + summary),
                        publishedDate = dateStr
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return affairs
    }

    private fun fetchFromPIB(): List<CurrentAffair> {
        val affairs = mutableListOf<CurrentAffair>()
        try {
            val doc = Jsoup.connect("https://pib.gov.in/allRel.aspx")
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            val items = doc.select(".content_area ul li, .release_list li")
            for (item in items.take(15)) {
                val linkEl = item.selectFirst("a")
                val title = linkEl?.text()?.trim() ?: continue
                val link = linkEl.attr("abs:href")
                val dateStr = item.selectFirst(".date, .rel_date")?.text()?.trim() ?: dateFormat.format(Date())

                affairs.add(
                    CurrentAffair(
                        title = title,
                        summary = title,
                        source = "PIB India",
                        sourceUrl = link,
                        category = categorizeAffair(title),
                        publishedDate = dateStr
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return affairs
    }

    private fun fetchFromGenericRSS(): List<CurrentAffair> {
        val affairs = mutableListOf<CurrentAffair>()
        val rssFeeds = listOf(
            "https://www.thehindu.com/news/national/feeder/default.rss" to "The Hindu",
            "https://indianexpress.com/section/explained/feed/" to "Indian Express"
        )

        for ((url, source) in rssFeeds) {
            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get()

                val items = doc.select("item")
                for (item in items.take(10)) {
                    val title = item.selectFirst("title")?.text()?.trim() ?: continue
                    val link = item.selectFirst("link")?.text()?.trim() ?: ""
                    val description = item.selectFirst("description")?.text()?.trim() ?: ""
                    val pubDate = item.selectFirst("pubDate")?.text()?.trim() ?: dateFormat.format(Date())

                    affairs.add(
                        CurrentAffair(
                            title = cleanHtml(title),
                            summary = cleanHtml(description).take(500),
                            source = source,
                            sourceUrl = link,
                            category = categorizeAffair(title + " " + description),
                            publishedDate = pubDate
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return affairs
    }

    private fun categorizeAffair(text: String): String {
        val lowerText = text.lowercase()
        return when {
            lowerText.containsAny("economy", "gdp", "rbi", "inflation", "budget", "tax", "trade", "fiscal", "monetary", "bank", "stock", "market", "finance") -> "Economy"
            lowerText.containsAny("science", "isro", "nasa", "technology", "ai ", "artificial", "satellite", "research", "innovation", "digital") -> "Science & Tech"
            lowerText.containsAny("environment", "climate", "pollution", "forest", "wildlife", "biodiversity", "carbon", "emission", "green", "ecology") -> "Environment"
            lowerText.containsAny("international", "un ", "china", "usa", "pakistan", "russia", "global", "world", "foreign", "diplomacy", "bilateral", "summit") -> "International"
            lowerText.containsAny("defence", "army", "navy", "air force", "military", "security", "border", "missile") -> "Defence"
            lowerText.containsAny("social", "education", "health", "women", "caste", "poverty", "scheme", "welfare", "development") -> "Social Issues"
            lowerText.containsAny("polity", "constitution", "supreme court", "parliament", "law", "amendment", "judiciary", "governor", "election") -> "Polity"
            else -> "National"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it) }

    private fun cleanHtml(text: String): String =
        Jsoup.parse(text).text()
}
