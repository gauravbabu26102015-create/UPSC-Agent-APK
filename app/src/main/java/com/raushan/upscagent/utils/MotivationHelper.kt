package com.raushan.upscagent.utils

import com.raushan.upscagent.data.model.MotivationalQuote
import java.util.Calendar

object MotivationHelper {

    private val quotes = listOf(
        // IAS Toppers & Indian Leaders
        MotivationalQuote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Tina Dabi, IAS Topper 2015", "UPSC"),
        MotivationalQuote("The only way to do great work is to love what you do.", "Dr. APJ Abdul Kalam", "Motivation"),
        MotivationalQuote("Dream is not that which you see while sleeping. It is something that does not let you sleep.", "Dr. APJ Abdul Kalam", "Motivation"),
        MotivationalQuote("In a gentle way, you can shake the world.", "Mahatma Gandhi", "Leadership"),
        MotivationalQuote("Be the change that you wish to see in the world.", "Mahatma Gandhi", "Leadership"),
        MotivationalQuote("The best time to plant a tree was 20 years ago. The second best time is now.", "Indian Proverb", "Motivation"),
        MotivationalQuote("UPSC doesn't test your knowledge, it tests your character.", "Anudeep Durishetty, IAS Topper 2017", "UPSC"),
        MotivationalQuote("Consistency is what transforms average into excellence.", "IAS Preparation Wisdom", "Study"),
        MotivationalQuote("Don't compare your chapter 1 to someone else's chapter 20.", "UPSC Mentor Advice", "Study"),
        MotivationalQuote("The Civil Services exam is not about intelligence, it's about persistence.", "Kanishak Kataria, IAS Topper 2018", "UPSC"),
        MotivationalQuote("Your limitation is only your imagination.", "Study Motivation", "Motivation"),
        MotivationalQuote("Hard work beats talent when talent doesn't work hard.", "Tim Notke", "Motivation"),
        MotivationalQuote("The secret of getting ahead is getting started.", "Mark Twain", "Study"),
        MotivationalQuote("It does not matter how slowly you go as long as you do not stop.", "Confucius", "Motivation"),
        MotivationalQuote("One child, one teacher, one book, one pen can change the world.", "Malala Yousafzai", "Education"),
        MotivationalQuote("Study like there's no tomorrow because if you keep putting off your studies, you'll likely end up with no tomorrow.", "UPSC Wisdom", "Study"),
        MotivationalQuote("Revision is the mother of learning. Do it daily.", "UPSC Strategy", "Study"),
        MotivationalQuote("An investment in knowledge pays the best interest.", "Benjamin Franklin", "Education"),
        MotivationalQuote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar", "Motivation"),
        MotivationalQuote("UPSC is a marathon, not a sprint. Pace yourself.", "IAS Mentor", "UPSC"),
        MotivationalQuote("Make each day your masterpiece.", "John Wooden", "Motivation"),
        MotivationalQuote("Education is the most powerful weapon which you can use to change the world.", "Nelson Mandela", "Education"),
        MotivationalQuote("Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau", "Motivation"),
        MotivationalQuote("Every expert was once a beginner.", "Helen Hayes", "Motivation"),
        MotivationalQuote("The only person you should try to be better than is the person you were yesterday.", "Study Wisdom", "Study")
    )

    private val studyTips = listOf(
        "📚 Start with NCERT books - they form the foundation of UPSC preparation.",
        "🧠 Use the Pomodoro Technique: 25 min study + 5 min break. Every 4th break is 15-20 min.",
        "📝 Make short notes while studying. They'll be invaluable during revision.",
        "📰 Read the newspaper daily - The Hindu or Indian Express for Current Affairs.",
        "🔄 Revise what you studied yesterday before starting new topics today.",
        "✍️ Practice answer writing daily - at least 2-3 answers in 250 words.",
        "🗺️ Keep an Atlas handy while studying Geography and mapping questions.",
        "🏛️ Link History with Art & Culture - UPSC loves integrated questions.",
        "📊 For Economy, follow the Economic Survey and Budget highlights.",
        "🌍 For International Relations, follow MEA website and Rajya Sabha TV.",
        "💡 Use mind maps to connect different topics across subjects.",
        "🎯 Previous Year Questions (PYQs) are your best guide - solve at least 10 years.",
        "⏰ Study your weakest subject during your peak productivity hours.",
        "🔗 Always think about interlinkages - UPSC loves cross-subject questions.",
        "📋 Make a realistic timetable and stick to it. Quality > Quantity.",
        "🧘 Take care of your health - a healthy mind needs a healthy body.",
        "📖 For Polity, read Laxmikanth thoroughly and make comparative tables.",
        "🌳 For Environment, follow ENVIS portal and IUCN Red List updates.",
        "✅ Solve mock tests regularly - it builds exam temperament.",
        "💪 Don't skip weekends entirely - light revision keeps the momentum."
    )

    private val dailyAdvice = listOf(
        "Today's Focus: Ensure you revise yesterday's topics before starting anything new. Spaced repetition is key!",
        "Strategy Tip: While reading Current Affairs, always think 'How can UPSC frame a question from this?'",
        "Prelims Tip: For CSAT, practice reading comprehension daily. Speed and accuracy matter.",
        "Mains Tip: Structure your answers with Introduction → Body → Conclusion. Use diagrams where possible.",
        "Mental Health: It's okay to feel overwhelmed. Take a walk, talk to someone, and come back stronger.",
        "Interview Tip: Start building your DAF (Detailed Application Form) story from now. Know your hobbies deeply.",
        "Ethics Tip: Read case studies from previous years. Practice writing ethical dilemma solutions.",
        "Smart Study: Don't read everything. Be selective and focus on high-yield topics.",
        "Time Management: Track how much time you actually study vs plan. Be honest with yourself.",
        "Community: Join a study group. Teaching others reinforces your own understanding."
    )

    fun getRandomQuote(): MotivationalQuote {
        return quotes.random()
    }

    fun getDailyQuote(): MotivationalQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }

    fun getRandomStudyTip(): String {
        return studyTips.random()
    }

    fun getDailyAdvice(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return dailyAdvice[dayOfYear % dailyAdvice.size]
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 5 -> "Burning the midnight oil? 🌙 Great dedication, but get some rest too!"
            hour < 9 -> "Good Morning! ☀️ Early bird gets the IAS! Let's make today count."
            hour < 12 -> "Good Morning! 📚 Peak study time - make the most of it!"
            hour < 15 -> "Good Afternoon! 🎯 Stay focused, you're doing great!"
            hour < 18 -> "Good Afternoon! ☕ Time for some revision and current affairs."
            hour < 21 -> "Good Evening! 📰 Perfect time for newspaper analysis and notes."
            else -> "Good Night! 🌟 Review your day's study. Rest well, tomorrow awaits!"
        }
    }

    fun getSubjectAdvice(subject: String): String {
        return when (subject.lowercase()) {
            "history" -> "Focus on themes and chronology. Link events across periods. Don't memorize dates blindly."
            "geography" -> "Always use maps. Physical geography concepts help understand human geography patterns."
            "polity" -> "Read Laxmikanth line by line. Compare and contrast constitutional provisions."
            "economy" -> "Focus on concepts over data. Understand RBI, SEBI policies and budget terminology."
            "science" -> "Focus on application-based questions. Link science with current affairs developments."
            "environment" -> "Follow IUCN, COP conferences. Know about Ramsar sites and Tiger Reserves."
            "art & culture" -> "Use visual learning. Visit virtual museums. Link architecture with dynasties."
            "ethics" -> "Practice case studies daily. Build your own ethical framework with examples."
            "current affairs" -> "Daily newspaper + monthly magazine. Make thematic notes, not date-wise."
            "csat" -> "Practice comprehension daily. Focus on accuracy over speed initially."
            else -> "Stay consistent with your preparation. Every subject matters in UPSC."
        }
    }
}
