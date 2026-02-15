# 📚 UPSC Study Agent - Android App

A complete UPSC preparation companion app built in **Kotlin** with all 6 requested features.

---

## ✅ Features Implemented

### 1. ⏰ Multiple Study Alarm System
- Set subject-wise alarms: e.g., "7-8 AM Physics", "8-10 AM Geography"
- **Start alarm** rings when your session begins (e.g., 8 AM for Geography)
- **End alarm** notifies when session completes
- Select which days of the week each alarm repeats
- Choose from preset subjects or type custom ones
- Toggle alarms on/off, edit, and delete
- Survives device reboot (alarms reschedule automatically)
- Fullscreen alarm ring screen with motivational quote

### 2. 📝 PDF to Digital Quiz Converter
- Import any PDF/Text file containing questions
- Smart parser recognizes multiple question formats:
  - `Q1. ...` / `1. ...` / `Question 1:` formats
  - Options: `(a)`, `A)`, `a.`, `1.` formats
  - Answer: `Answer: A` / `Ans: (b)` formats
- Also supports manual quiz creation (question by question)
- Interactive quiz with timer, scoring, and explanations
- Results saved with percentage, time taken, and detailed breakdown

### 3. 📰 Daily Current Affairs Auto-Collector
- Fetches from multiple UPSC-relevant sources:
  - Insights on India
  - PIB (Press Information Bureau)
  - The Hindu RSS
  - Indian Express Explained
- Auto-categorizes: National, International, Economy, Science & Tech, Environment, Polity, Defence
- Background fetch every 6 hours via WorkManager
- Push notification before study: "Read current affairs!"
- Bookmark important articles
- Pull-to-refresh for manual update

### 4. 🤖 UPSC Agent (Motivation, Knowledge & Advice)
- **Daily Motivational Quotes** from IAS toppers and Indian leaders
- **Study Tips** - practical UPSC preparation strategies
- **Subject-Specific Advice** for all 10 major UPSC subjects
- **Daily Strategic Advice** - rotates daily
- **Greeting** that changes based on time of day
- Notification-based motivation every 12 hours

### 5. 📖 Multi-Format Document Reader
- **PDF Reader** with zoom, swipe, anti-aliasing (via AndroidPdfViewer)
- **HTML Reader** with WebView, zoom, JavaScript support
- **Text/TXT Reader** with selectable text
- **DOC support** (basic text extraction)
- Document library with recently opened tracking
- Add documents from device storage
- Persistent URI permissions for reliable access

### 6. 🏠 Dashboard Home Screen
- Time-based greeting with study motivation
- Daily quote card
- Today's strategy advice
- Rotating study tips with refresh button
- Quick action cards to navigate to all features

---

## 🏗️ Architecture

```
com.raushan.upscagent/
├── UPSCAgentApp.kt          # Application class, notification channels
├── data/
│   ├── db/AppDatabase.kt     # Room database with 7 tables, all DAOs
│   ├── model/Models.kt        # All data models (StudyAlarm, Quiz, Question, etc.)
│   └── repository/AppRepository.kt  # Single repository for all data access
├── receiver/
│   ├── AlarmReceiver.kt       # Handles alarm broadcasts
│   └── BootReceiver.kt        # Reschedules alarms after reboot
├── service/
│   └── AlarmService.kt        # Foreground service for alarm sound/vibration
├── worker/
│   └── CurrentAffairsWorker.kt # Background current affairs fetch
├── utils/
│   ├── AlarmHelper.kt         # Alarm scheduling logic
│   ├── CurrentAffairsFetcher.kt # Web scraping with Jsoup
│   ├── MotivationHelper.kt    # 25+ quotes, 20 tips, subject advice
│   └── PDFQuizParser.kt       # PDF/Text to Quiz converter
└── ui/
    ├── home/                   # MainActivity + HomeFragment (dashboard)
    ├── alarm/                  # AlarmFragment + AlarmRingActivity
    ├── quiz/                   # QuizFragment + QuizActivity
    ├── currentaffairs/         # CurrentAffairsFragment
    ├── agent/                  # AgentFragment (motivation + docs)
    └── reader/                 # ReaderActivity (PDF/HTML/TXT)
```

---

## 🔧 Setup Instructions

1. **Open in Android Studio** (Hedgehog or newer)
   - File → Open → Select the `UPSCQuizApp` folder

2. **Sync Gradle** - Android Studio will download all dependencies

3. **Run on device/emulator** (API 26+ / Android 8.0+)

4. **Grant Permissions** when prompted:
   - Notifications (for alarms & current affairs)
   - Exact Alarms (for study schedule)

---

## 📦 Key Dependencies

| Library | Purpose |
|---------|---------|
| Room | Local database for alarms, quizzes, results |
| WorkManager | Background current affairs fetching |
| Retrofit + OkHttp | Network requests |
| Jsoup | HTML parsing & web scraping |
| AndroidPdfViewer | PDF rendering |
| Material Components | Modern UI design |
| Coroutines + Flow | Async operations |
| DataStore | Preferences storage |
| Glide | Image loading |
| Lottie | Animations |

---

## 📱 Min SDK: 26 (Android 8.0) | Target SDK: 34 (Android 14)

Built with ❤️ for UPSC aspirants. All the best, Raushan! 🇮🇳
