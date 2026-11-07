package com.example.data

import com.example.data.model.Article
import com.example.data.model.ExamQuestion
import com.example.data.model.TopicPerformance
import com.example.data.repository.PyqRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapUnitTests {

    @Test
    fun pyqRepository_filtersCorrectlyByExam() = runBlocking {
        val repo = PyqRepository()
        val allPyqs = repo.getPyqs()
        assertTrue("Total PYQs should be non-empty", allPyqs.isNotEmpty())

        val upscPyqs = repo.getPyqs(examination = "UPSC")
        assertTrue(upscPyqs.all { it.examination == "UPSC" })

        val sscPyqs = repo.getPyqs(examination = "SSC CGL")
        assertTrue(sscPyqs.all { it.examination == "SSC CGL" })
    }

    @Test
    fun pyqRepository_availableFilters_arePopulated() = runBlocking {
        val repo = PyqRepository()
        val exams = repo.getAvailableExaminations()
        assertTrue(exams.contains("UPSC"))
        assertTrue(exams.contains("SSC CGL"))
        assertTrue(exams.contains("Banking"))

        val subjects = repo.getAvailableSubjects()
        assertTrue(subjects.contains("Economy"))
        assertTrue(subjects.contains("Polity"))
    }

    @Test
    fun topicPerformance_masteryLevelValues() {
        val weak = TopicPerformance(
            id = "tp_1",
            userId = "user_test",
            topic = "Macroeconomics",
            attempted = 10L,
            correct = 4L,
            incorrect = 6L,
            accuracy = 40.0,
            masteryLevel = "Weak"
        )
        assertEquals("Weak", weak.masteryLevel)
        assertTrue(weak.accuracy < 50.0)

        val strong = TopicPerformance(
            id = "tp_2",
            userId = "user_test",
            topic = "Polity",
            attempted = 20L,
            correct = 18L,
            incorrect = 2L,
            accuracy = 90.0,
            masteryLevel = "Strong"
        )
        assertEquals("Strong", strong.masteryLevel)
        assertTrue(strong.accuracy >= 85.0)
    }

    @Test
    fun articleAndExamQuestion_modelsInstantiateCorrectly() {
        val article = Article(
            id = "art_1",
            title = "RBI Repo Rate Announcement",
            description = "Monetary policy committee maintains rate at 6.5%",
            content = "Full detailed article content on the reserve bank...",
            category = "Economy",
            source = "PIB Delhi",
            publishedDate = "2026-11-06",
            keyFacts = listOf("Repo Rate 6.5%", "SDF 6.25%", "MSF 6.75%"),
            examRelevance = "UPSC GS-III Economy, Banking Exams"
        )
        assertEquals("Economy", article.category)
        assertEquals(3, article.keyFacts.size)

        val question = ExamQuestion(
            id = "q_1",
            question = "What is the primary objective of repo rate?",
            options = listOf("Liquidity regulation", "Tax collection", "Export subsidy", "Foreign aid"),
            correctIndex = 0,
            explanation = "Repo rate is the benchmark lending rate used by central banks to control liquidity and inflation.",
            difficulty = "Medium",
            category = "Economy",
            relatedConcept = "Monetary Policy",
            examType = "UPSC"
        )
        assertEquals(0, question.correctIndex)
        assertEquals(4, question.options.size)
        assertEquals("Medium", question.difficulty)
    }
}
