package com.example.data.repository

import com.example.data.local.ArticleEntity
import com.example.data.local.CapDao
import com.example.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsRepository(private val dao: CapDao) {

    val articles: Flow<List<Article>> = dao.getAllArticles().map { entities ->
        entities.map { it.toArticle() }
    }

    suspend fun getArticleById(id: String): Article? = withContext(Dispatchers.IO) {
        dao.getArticleById(id)?.toArticle()
    }

    suspend fun toggleBookmark(articleId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.updateBookmarkStatus(articleId, !currentStatus)
    }

    suspend fun markAsRead(articleId: String) = withContext(Dispatchers.IO) {
        dao.updateReadStatus(articleId, true)
    }

    suspend fun seedInitialArticlesIfEmpty() = withContext(Dispatchers.IO) {
        val seededArticles = listOf(
            ArticleEntity(
                id = "art_rbi_monetary_policy",
                title = "RBI Monetary Policy Committee Keeps Repo Rate Unchanged at 6.50%",
                description = "The Reserve Bank of India MPC voted to keep the key benchmark lending rate steady while maintaining focus on withdrawal of accommodation to align inflation with the 4% target.",
                content = """
                    The Reserve Bank of India’s (RBI) Monetary Policy Committee (MPC) concluded its bi-monthly meeting, unanimously deciding to maintain the benchmark repurchase (repo) rate at 6.50%.
                    
                    Governor Shaktikanta Das highlighted that while domestic economic activity remains resilient, sustained vigil is required on food price shocks to anchor inflation expectations firmly around the mandated 4.0% median target with a tolerance band of +/- 2%.
                    
                    Key Highlights:
                    1. The Standing Deposit Facility (SDF) rate remains at 6.25%, and the Marginal Standing Facility (MSF) rate stands at 6.75%.
                    2. GDP growth for the financial year was projected at 7.2%, driven by strong rural consumption, robust capital expenditure by the central government, and buoyant services sector exports.
                    3. Headline retail inflation (CPI) was projected at 4.5% with food inflation risks remaining contingent on spatial monsoon distribution.
                    4. The MPC reaffirmed its policy stance as 'Withdrawal of Accommodation' to support growth while ensuring price stability.
                """.trimIndent(),
                category = "Economy",
                source = "RBI Bulletin & Press Information Bureau",
                publishedDate = "Today, 11:30 AM",
                keyFactsJson = "[\"Repo Rate: 6.50%\", \"SDF Rate: 6.25%\", \"MSF Rate: 6.75%\", \"Projected GDP Growth: 7.2%\", \"Target Inflation: 4.0% +/- 2%\"]",
                examRelevance = "High relevance for UPSC GS-3 (Indian Economy, Monetary Policy), SSC CGL (Financial Awareness), and RBI Grade B / SBI PO Mains.",
                fetchTimestamp = System.currentTimeMillis()
            ),
            ArticleEntity(
                id = "art_isro_chandrayaan4",
                title = "Union Cabinet Approves ISRO's Chandrayaan-4 Lunar Sample Return Mission",
                description = "The Cabinet has cleared INR 2,104 crore for the Chandrayaan-4 mission, designed to land on the Moon, collect surface samples, and return safely to Earth.",
                content = """
                    In a major boost to India's planetary exploration program, the Union Cabinet chaired by Prime Minister Narendra Modi has formally approved the Chandrayaan-4 mission.
                    
                    The ambitious lunar sample return mission is slated for launch around 2028 and aims to demonstrate critical indigenous space technologies including lunar docking, sample collection, lunar ascent, and safe atmospheric re-entry.
                    
                    Key Technical Highlights:
                    1. Complex Multi-Module Architecture: Comprises five modules across two separate launch vehicle stacks (LVM3): Ascender Module, Descender Module, Re-entry Module, Transfer Module, and Propulsion Module.
                    2. Docking In Lunar Orbit: Modules will perform automated docking in lunar orbit, a milestone essential for India's planned Bharatiya Antariksh Station (BAS) by 2035 and crewed lunar landing by 2040.
                    3. Scientific Return: Samples from the south polar region will provide pristine insights into the volatile composition and water-ice trapped in permanently shadowed craters.
                """.trimIndent(),
                category = "Science and Technology",
                source = "Department of Space / PIB",
                publishedDate = "Today, 09:15 AM",
                keyFactsJson = "[\"Budget: ₹2,104 Crore\", \"Target Year: 2028\", \"Launch Vehicle: LVM3\", \"BAS Target: 2035\", \"Crewed Moon Landing: 2040\"]",
                examRelevance = "Directly relevant for UPSC GS-3 (Science & Tech, Space Technology), SSC CGL Science GK, and Defence exams.",
                fetchTimestamp = System.currentTimeMillis() - 3600000L
            ),
            ArticleEntity(
                id = "art_cop29_climate_finance",
                title = "New Collective Quantified Goal (NCQG) on Climate Finance Debated at COP29",
                description = "Developing nations, including India, reiterate that the global climate finance goal must scale into trillions of dollars annually, emphasizing public finance and grant-based support.",
                content = """
                    At the UN Climate Change Conference, deliberations centered on finalizing the New Collective Quantified Goal (NCQG) to replace the previous USD 100 billion per year target established in 2009.
                    
                    India and the Like-Minded Developing Countries (LMDC) group emphasized Common But Differentiated Responsibilities and Respective Capabilities (CBDR-RC), demanding that developed nations provide non-debt-creating grant-based finance to fulfill Paris Agreement commitments.
                    
                    Key Pillars of Discussion:
                    1. Quantum of Finance: Developing nations demand at least USD 1.3 trillion annually from 2025 onwards.
                    2. Climate Justice & Loss and Damage: Operationalization of the Loss and Damage Fund to compensate vulnerable ecosystems.
                    3. Adaptation vs Mitigation: Ensuring balanced resource allocation between emission reduction and climate-resilient agriculture and infrastructure.
                """.trimIndent(),
                category = "Environment",
                source = "UNFCCC & Ministry of Environment, Forest and Climate Change",
                publishedDate = "Yesterday",
                keyFactsJson = "[\"Target: USD 1.3 Trillion/year\", \"Core Principle: CBDR-RC\", \"Paris Agreement Goal: 1.5°C ceiling\"]",
                examRelevance = "Crucial for UPSC GS-3 (Environment & Ecology, Climate Change Negotiations) and State PSC General Studies.",
                fetchTimestamp = System.currentTimeMillis() - 86400000L
            ),
            ArticleEntity(
                id = "art_pm_surya_ghar",
                title = "PM-Surya Ghar Muft Bijli Yojana Crosses Milestone 1.3 Crore Registrations",
                description = "The rooftop solar scheme launched with a financial outlay of ₹75,021 crore provides up to 300 units of free electricity per month for 1 crore households.",
                content = """
                    The Ministry of New and Renewable Energy (MNRE) announced that the PM-Surya Ghar: Muft Bijli Yojana has surpassed 1.3 crore consumer registrations across states, marking rapid adoption of decentralized clean energy.
                    
                    The scheme offers central financial assistance (subsidy) up to 60% of the system cost for 2 kW systems and 40% of additional cost for systems between 2 to 3 kW capacity.
                    
                    Socio-Economic & Environmental Impacts:
                    1. Direct electricity savings of up to ₹15,000 per year per household through net-metering.
                    2. Estimated addition of 30 GW of rooftop solar capacity, reducing carbon emissions by 720 million tonnes over their 25-year lifespan.
                    3. Creation of approximately 17 lakh direct green jobs in manufacturing, installation, operation, and maintenance.
                """.trimIndent(),
                category = "Government Schemes",
                source = "Ministry of New and Renewable Energy",
                publishedDate = "2 days ago",
                keyFactsJson = "[\"Outlay: ₹75,021 Crore\", \"Free Power: Up to 300 units/month\", \"Target: 1 Crore households\", \"Carbon reduction: 720M tonnes\"]",
                examRelevance = "Direct questions expected in UPSC GS-2 & GS-3 (Govt Policies, Renewable Energy) and SSC/Banking General Awareness.",
                fetchTimestamp = System.currentTimeMillis() - 172800000L
            ),
            ArticleEntity(
                id = "art_india_semiconductor",
                title = "India Semiconductor Mission: Five Fabrication & ATMP Plants Under Active Construction",
                description = "Under the ₹76,000 crore ISM initiative, advanced wafer fabrication and packaging units in Gujarat and Assam advance India toward self-reliant electronics manufacturing.",
                content = """
                    The India Semiconductor Mission (ISM) has made historic progress with construction underway at five mega projects, including Tata Electronics' commercial fabrication unit in Dholera, Gujarat, and packaging plants in Sanand and Morigaon, Assam.
                    
                    The initiative addresses global supply chain vulnerabilities and positions India as a major hub for automotive, telecommunications, and industrial semiconductor chips.
                    
                    Strategic Dimensions:
                    1. 50% Fiscal Support: The Central Government provides 50% capital expenditure support on a pari-passu basis across all technology nodes.
                    2. Talent Pipeline: Over 100 universities have upgraded specialized VLSI design and microelectronics curricula.
                    3. Geopolitical Significance: Complements the Quad Semiconductor Supply Chain Initiative and India-US Critical and Emerging Technology (iCET) framework.
                """.trimIndent(),
                category = "National Affairs",
                source = "Ministry of Electronics and Information Technology",
                publishedDate = "3 days ago",
                keyFactsJson = "[\"Fiscal Outlay: ₹76,000 Crore\", \"Central Subsidy: 50% CapEx\", \"Key Hubs: Dholera, Sanand, Morigaon\"]",
                examRelevance = "High priority for UPSC GS-3 (Indigenization of Technology) and SSC CGL.",
                fetchTimestamp = System.currentTimeMillis() - 259200000L
            ),
            ArticleEntity(
                id = "art_defence_agni5",
                title = "DRDO Successfully Conducts User Trial of Agni-5 Ballistic Missile with MIRV Technology",
                description = "Mission Divyastra verified indigenous Multiple Independently Targetable Re-entry Vehicle (MIRV) capability on the 5,000+ km range Agni-5 intercontinental missile.",
                content = """
                    The Defence Research and Development Organisation (DRDO) successfully validated the operational readiness of the Agni-5 intercontinental ballistic missile equipped with Multiple Independently Targetable Re-entry Vehicles (MIRV).
                    
                    Dubbed 'Mission Divyastra', the flight test from Dr. APJ Abdul Kalam Island, Odisha, proved that a single missile can deploy multiple warheads to strike distinct enemy targets separated by hundreds of kilometers.
                    
                    Strategic Significance:
                    1. Deterrence Posture: Elevates India into the select group of nations (USA, Russia, China, UK, France) possessing indigenous MIRV warhead capability.
                    2. Counter-Measure Penetration: MIRVs significantly degrade the effectiveness of adversary anti-ballistic missile (ABM) defence shields.
                    3. Solid-Fuel Propulsion: Three-stage composite rocket motor allows rapid canisterized road-mobile deployment.
                """.trimIndent(),
                category = "Defence",
                source = "Ministry of Defence / DRDO",
                publishedDate = "4 days ago",
                keyFactsJson = "[\"Code Name: Mission Divyastra\", \"Range: 5,000+ km\", \"Technology: MIRV\", \"Test Site: Dr. APJ Abdul Kalam Island\"]",
                examRelevance = "Directly relevant for UPSC GS-3 (Security Challenges, Defence Indigenization), CDS, and AFCAT.",
                fetchTimestamp = System.currentTimeMillis() - 345600000L
            )
        )
        dao.insertArticles(seededArticles)
    }

    private fun ArticleEntity.toArticle(): Article {
        val facts = if (keyFactsJson.isNotBlank()) {
            try {
                val arr = org.json.JSONArray(keyFactsJson)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        return Article(
            id = id,
            title = title,
            description = description,
            content = content,
            category = category,
            source = source,
            publishedDate = publishedDate,
            url = url,
            imageUrl = imageUrl,
            keyFacts = facts,
            examRelevance = examRelevance,
            isBookmarked = isBookmarked,
            isRead = isRead
        )
    }
}
