package com.example.data.repository

import com.example.data.model.PyqQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PyqRepository {

    private val verifiedPyqs = listOf(
        PyqQuestion(
            id = "pyq_upsc_2023_repo",
            examination = "UPSC",
            year = 2023,
            subject = "Economy",
            topic = "Monetary Policy & Central Banking",
            question = "In the context of Indian economy, which of the following is/are the purpose/purposes of 'Marginal Standing Facility' and 'Standing Deposit Facility'?",
            options = listOf(
                "1 and 2 only",
                "To absorb liquidity and inject overnight liquidity into the banking system without requiring collateral in the case of SDF",
                "To regulate foreign portfolio investment inflows exclusively",
                "To determine the minimum support price for agricultural produce"
            ),
            correctIndex = 1,
            explanation = "Under the revised liquidity management framework, the Standing Deposit Facility (SDF) allows the RBI to absorb surplus liquidity from commercial banks without pledging collateral (government securities). The MSF serves as an overnight liquidity injection window against eligible securities.",
            sourceReference = "UPSC Civil Services Examination (Prelims) 2023 - General Studies Paper I",
            difficulty = "Hard"
        ),
        PyqQuestion(
            id = "pyq_upsc_2022_g20",
            examination = "UPSC",
            year = 2022,
            subject = "International Relations",
            topic = "Global Groupings",
            question = "In which one of the following groups are all four countries members of G20?",
            options = listOf(
                "Argentina, Mexico, South Africa and Turkey",
                "Australia, Canada, Malaysia and New Zealand",
                "Brazil, Iran, Saudi Arabia and Vietnam",
                "Indonesia, Japan, Singapore and South Korea"
            ),
            correctIndex = 0,
            explanation = "Argentina, Mexico, South Africa, and Turkey are all official members of the Group of Twenty (G20). Malaysia, New Zealand, Iran, and Singapore are not G20 members.",
            sourceReference = "UPSC Civil Services (Prelims) 2022 - General Studies Paper I",
            difficulty = "Medium"
        ),
        PyqQuestion(
            id = "pyq_ssc_2023_solar",
            examination = "SSC CGL",
            year = 2023,
            subject = "Science and Technology",
            topic = "Renewable Energy & Physics",
            question = "Which photovoltaic material is most commonly used in commercial solar cells deployed under the National Solar Mission in India?",
            options = listOf(
                "Crystalline Silicon",
                "Germanium Arsenide",
                "Titanium Dioxide",
                "Lead Sulphide"
            ),
            correctIndex = 0,
            explanation = "Crystalline Silicon (mono-crystalline and multi-crystalline) constitutes over 90% of global and Indian commercial photovoltaic solar panel installations due to optimal bandgap, abundance, and commercial efficiency.",
            sourceReference = "SSC CGL Tier-1 2023 (General Awareness)",
            difficulty = "Easy"
        ),
        PyqQuestion(
            id = "pyq_ssc_2022_article",
            examination = "SSC CGL",
            year = 2022,
            subject = "Polity",
            topic = "Constitutional Provisions",
            question = "Under which Article of the Constitution of India is the Annual Financial Statement (commonly known as the Union Budget) laid before both Houses of Parliament?",
            options = listOf(
                "Article 110",
                "Article 112",
                "Article 280",
                "Article 360"
            ),
            correctIndex = 1,
            explanation = "Article 112 of the Indian Constitution mandates that the President shall in respect of every financial year cause to be laid before both Houses of Parliament an Annual Financial Statement.",
            sourceReference = "SSC CGL Tier-1 2022 (General Awareness)",
            difficulty = "Easy"
        ),
        PyqQuestion(
            id = "pyq_banking_2023_rbi",
            examination = "Banking",
            year = 2023,
            subject = "Banking Awareness",
            topic = "RBI Regulations & Capital Adequacy",
            question = "Under Basel III norms implemented by the Reserve Bank of India, what is the minimum Common Equity Tier 1 (CET1) capital ratio required for commercial banks in India?",
            options = listOf(
                "4.5%",
                "5.5%",
                "7.0%",
                "9.0%"
            ),
            correctIndex = 1,
            explanation = "Under RBI guidelines for Basel III in India, commercial banks must maintain a minimum CET1 ratio of 5.5% of risk-weighted assets, which is higher than the global Basel baseline of 4.5%.",
            sourceReference = "IBPS PO Mains 2023 (Banking & Financial Awareness)",
            difficulty = "Medium"
        ),
        PyqQuestion(
            id = "pyq_railway_2022_vande",
            examination = "Railway",
            year = 2022,
            subject = "National Affairs",
            topic = "Railway Infrastructure",
            question = "At which manufacturing facility in India was the indigenous semi-high speed train set 'Vande Bharat Express' originally conceptualized and manufactured under Project Train 18?",
            options = listOf(
                "Integral Coach Factory (ICF), Chennai",
                "Rail Coach Factory (RCF), Kapurthala",
                "Modern Coach Factory (MCF), Raebareli",
                "Chittaranjan Locomotive Works (CLW)"
            ),
            correctIndex = 0,
            explanation = "The Vande Bharat Express (Train 18) was designed and manufactured by the Integral Coach Factory (ICF) in Perambur, Chennai, under the Make in India initiative.",
            sourceReference = "RRB NTPC CBT-2 2022 (General Awareness)",
            difficulty = "Easy"
        )
    )

    suspend fun getPyqs(
        examination: String? = null,
        year: Int? = null,
        subject: String? = null
    ): List<PyqQuestion> = withContext(Dispatchers.IO) {
        verifiedPyqs.filter { q ->
            (examination == null || examination == "All" || q.examination.equals(examination, ignoreCase = true)) &&
            (year == null || q.year == year) &&
            (subject == null || subject == "All" || q.subject.equals(subject, ignoreCase = true))
        }
    }

    suspend fun getAvailableExaminations(): List<String> = withContext(Dispatchers.IO) {
        listOf("All", "UPSC", "SSC CGL", "Banking", "Railway", "State PSC")
    }

    suspend fun getAvailableSubjects(): List<String> = withContext(Dispatchers.IO) {
        listOf("All", "Economy", "Polity", "Science and Technology", "International Relations", "Banking Awareness", "National Affairs")
    }
}
