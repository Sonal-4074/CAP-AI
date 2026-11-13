const CurrentAffairs = require('../models/CurrentAffairs');

// Curated seed data for competitive exam current affairs
const SEED_CURRENT_AFFAIRS = [
  {
    title: 'India Unveils National Mission on Deep Sea Exploration & Blue Economy',
    summary: 'The Cabinet Committee on Economic Affairs has approved enhanced budgetary outlays for the Deep Ocean Mission focusing on polymetallic nodules, marine biodiversity conservation, and offshore renewable energy.',
    content: 'The Ministry of Earth Sciences is coordinating the multi-agency Deep Ocean Mission. Core pillars include manned submersibles (Matsya 6000), deep-sea mining technology trials in the Central Indian Ocean Basin, and mapping underwater geohazards.',
    category: 'Science & Tech',
    source: 'Press Information Bureau (PIB)',
    date: new Date(),
    examRelevance: 'GS Paper III: Science & Technology, Oceanography, Environmental Conservation',
    keyFacts: [
      'Matsya 6000 is designed for a 6,000-meter depth with 3 crew members.',
      'India was granted exploration rights by the International Seabed Authority (ISA) in 1987.',
      'Blue Economy accounts for approx. 4% of India’s Gross Domestic Product.'
    ],
    tags: ['Deep Ocean Mission', 'Matsya 6000', 'Blue Economy', 'ISA'],
    imageUrl: 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80'
  },
  {
    title: 'Reserve Bank of India Keeps Repo Rate Steady, Enhances UPI Lite Limit',
    summary: 'The Monetary Policy Committee (MPC) maintained the policy repo rate at 6.50% while emphasizing continuous vigilance on food inflation trajectories and global trade volatility.',
    content: 'The 6-member MPC evaluated headline consumer price inflation (CPI) trends. Key announcements included increasing the per-transaction limit for offline UPI payments via UPI Lite and expanding the Digital Rupee (e-Rupee) cross-border pilot.',
    category: 'Economy',
    source: 'The Hindu BusinessLine',
    date: new Date(Date.now() - 3600000 * 12),
    examRelevance: 'GS Paper III: Indian Economy, Monetary Policy, Banking & Financial Inclusion',
    keyFacts: [
      'MPC was constituted under Section 45ZB of the amended RBI Act, 1934.',
      'Inflation targeting framework aims for 4% CPI with a +/- 2% tolerance band.',
      'Repo rate is the rate at which commercial banks borrow from the central bank.'
    ],
    tags: ['RBI', 'Monetary Policy', 'Repo Rate', 'Inflation Targeting', 'UPI Lite'],
    imageUrl: 'https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80'
  },
  {
    title: 'India-EU Trade and Technology Council (TTC) Advances Clean Energy Standards',
    summary: 'Senior delegates met in New Delhi to harmonize green hydrogen certifications, semiconductor supply chain resilience, and digital public infrastructure (DPI) interoperability.',
    content: 'The bilateral forum focused on reducing carbon border adjustment mechanisms (CBAM) friction, promoting mutual recognition of green credentials, and establishing joint research in Artificial Intelligence safety frameworks.',
    category: 'International',
    source: 'Ministry of External Affairs',
    date: new Date(Date.now() - 3600000 * 24),
    examRelevance: 'GS Paper II: Bilateral, Regional, and Global Groupings involving India',
    keyFacts: [
      'TTC is only the second bilateral mechanism of its kind for the European Union (first with the US).',
      'EU Carbon Border Adjustment Mechanism (CBAM) imposes carbon tariffs on imported steel, cement, and aluminium.',
      'India is EU’s second largest trading partner in South Asia.'
    ],
    tags: ['India-EU', 'TTC', 'CBAM', 'Digital Public Infrastructure', 'Diplomacy'],
    imageUrl: 'https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?auto=format&fit=crop&w=800&q=80'
  },
  {
    title: 'Supreme Court Upholds Sub-Classification within Scheduled Castes and Tribes',
    summary: 'A 7-judge Constitution Bench ruled by 6:1 majority that states have constitutional authority to create sub-classifications within reserved categories to benefit more disadvantaged castes.',
    content: 'Interpreting Articles 14, 15, 16, 341, and 342, the apex court clarified that sub-classification does not violate the presidential list under Article 341, provided states present verifiable empirical data demonstrating inadequate representation.',
    category: 'Polity',
    source: 'LiveLaw / Supreme Court Reports',
    date: new Date(Date.now() - 3600000 * 36),
    examRelevance: 'GS Paper II: Indian Constitution, Judicial Pronouncements, Social Justice',
    keyFacts: [
      'Overruled the 2004 5-judge bench verdict in E.V. Chinnaiah v. State of Andhra Pradesh.',
      'Reiterated that the creamy layer principle applies to affirmative action jurisprudence.',
      'Article 341 empowers the President to specify Scheduled Castes in consultation with State Governors.'
    ],
    tags: ['Supreme Court', 'Sub-Classification', 'Article 341', 'Reservation', 'Affirmative Action'],
    imageUrl: 'https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=800&q=80'
  },
  {
    title: 'India Records 31% Increase in Renewable Energy Capacity Addition',
    summary: 'Solar and wind installations reached record highs in the fiscal year, driven by the PM-Surya Ghar: Muft Bijli Yojana and round-the-clock renewable tenders.',
    content: 'The Ministry of New and Renewable Energy reported that non-fossil installed capacity crossed 200 GW, on track toward the 500 GW target pledged at COP26 for the year 2030.',
    category: 'Environment',
    source: 'Press Information Bureau (PIB)',
    date: new Date(Date.now() - 3600000 * 48),
    examRelevance: 'GS Paper III: Conservation, Environmental Pollution & Degradation, Renewable Energy',
    keyFacts: [
      'PM-Surya Ghar aims to power 1 crore households with rooftop solar.',
      'India ranks 4th globally in total renewable energy installed capacity.',
      'Panchamrit targets pledge 50% electric power from non-fossil sources by 2030.'
    ],
    tags: ['Renewable Energy', 'COP26', 'PM Surya Ghar', 'Solar Power', 'Panchamrit'],
    imageUrl: 'https://images.unsplash.com/photo-1509391365360-2e959784a276?auto=format&fit=crop&w=800&q=80'
  }
];

class NewsService {
  /**
   * Get filtered, paginated list of current affairs articles.
   */
  async getArticles({ category, search, page = 1, limit = 10 } = {}) {
    try {
      const query = {};
      if (category && category !== 'All') {
        query.category = category;
      }
      if (search) {
        query.$or = [
          { title: { $regex: search, $options: 'i' } },
          { summary: { $regex: search, $options: 'i' } },
          { tags: { $in: [new RegExp(search, 'i')] } }
        ];
      }

      // If connected to MongoDB, query the collection
      const mongoose = require('mongoose');
      if (mongoose.connection.readyState === 1) {
        const skip = (page - 1) * limit;
        const total = await CurrentAffairs.countDocuments(query);
        const articles = await CurrentAffairs.find(query)
          .sort({ date: -1 })
          .skip(skip)
          .limit(limit)
          .lean();

        if (articles.length > 0) {
          return {
            articles: articles.map(this.formatArticle),
            total,
            page: Number(page),
            pages: Math.ceil(total / limit)
          };
        }
      }

      // Fallback to seed articles
      let filtered = [...SEED_CURRENT_AFFAIRS];
      if (category && category !== 'All') {
        filtered = filtered.filter(a => a.category.toLowerCase() === category.toLowerCase());
      }
      if (search) {
        const s = search.toLowerCase();
        filtered = filtered.filter(
          a =>
            a.title.toLowerCase().includes(s) ||
            a.summary.toLowerCase().includes(s) ||
            a.tags.some(t => t.toLowerCase().includes(s))
        );
      }

      const startIndex = (page - 1) * limit;
      const sliced = filtered.slice(startIndex, startIndex + limit);

      return {
        articles: sliced.map((a, idx) => ({ ...a, id: `seed_${idx + 1}` })),
        total: filtered.length,
        page: Number(page),
        pages: Math.ceil(filtered.length / limit)
      };
    } catch (error) {
      console.error('Error fetching articles:', error.message);
      return {
        articles: SEED_CURRENT_AFFAIRS.map((a, idx) => ({ ...a, id: `seed_${idx + 1}` })),
        total: SEED_CURRENT_AFFAIRS.length,
        page: 1,
        pages: 1
      };
    }
  }

  /**
   * Get single article by ID.
   */
  async getArticleById(id) {
    try {
      const mongoose = require('mongoose');
      if (mongoose.connection.readyState === 1 && mongoose.Types.ObjectId.isValid(id)) {
        const article = await CurrentAffairs.findById(id).lean();
        if (article) return this.formatArticle(article);
      }
    } catch {
      // Ignore id casting errors
    }

    // Look in seed articles
    const index = parseInt(id.replace('seed_', ''), 10) - 1;
    if (index >= 0 && index < SEED_CURRENT_AFFAIRS.length) {
      return { ...SEED_CURRENT_AFFAIRS[index], id };
    }

    return { ...SEED_CURRENT_AFFAIRS[0], id: id || 'seed_1' };
  }

  /**
   * Helper to format Mongoose document to clean JSON
   */
  formatArticle(doc) {
    return {
      id: doc._id.toString(),
      title: doc.title,
      summary: doc.summary,
      content: doc.content,
      category: doc.category,
      source: doc.source,
      date: doc.date,
      examRelevance: doc.examRelevance,
      keyFacts: doc.keyFacts || [],
      tags: doc.tags || [],
      imageUrl: doc.imageUrl
    };
  }
}

module.exports = new NewsService();
