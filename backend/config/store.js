const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const DATA_DIR = path.join(__dirname, '..', 'data');
const DATA_FILE = path.join(DATA_DIR, 'capai_store.json');

// Ensure data directory exists
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

// Initial default schema
const defaultData = {
  users: [],
  analyses: [],
  knowledgeMaps: [],
  generatedQuestions: [],
  quizAttempts: [],
  revisions: [],
  bookmarks: [],
  readingHistory: [],
  topicPerformance: []
};

function readDb() {
  try {
    if (!fs.existsSync(DATA_FILE)) {
      fs.writeFileSync(DATA_FILE, JSON.stringify(defaultData, null, 2), 'utf8');
      return JSON.parse(JSON.stringify(defaultData));
    }
    const raw = fs.readFileSync(DATA_FILE, 'utf8');
    return JSON.parse(raw);
  } catch (err) {
    console.error('Error reading JSON store, initializing default:', err);
    return JSON.parse(JSON.stringify(defaultData));
  }
}

function writeDb(data) {
  try {
    fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), 'utf8');
  } catch (err) {
    console.error('Error writing JSON store:', err);
  }
}

class Collection {
  constructor(name) {
    this.name = name;
  }

  async find(query = {}) {
    const db = readDb();
    const items = db[this.name] || [];
    return items.filter(item => matchQuery(item, query));
  }

  async findOne(query = {}) {
    const db = readDb();
    const items = db[this.name] || [];
    return items.find(item => matchQuery(item, query)) || null;
  }

  async findById(id) {
    return this.findOne({ _id: id });
  }

  async create(doc) {
    const db = readDb();
    if (!db[this.name]) db[this.name] = [];

    const newDoc = {
      _id: doc._id || crypto.randomUUID(),
      ...doc,
      createdAt: doc.createdAt || new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    db[this.name].push(newDoc);
    writeDb(db);
    return newDoc;
  }

  async updateOne(query, update) {
    const db = readDb();
    const items = db[this.name] || [];
    const index = items.findIndex(item => matchQuery(item, query));
    if (index === -1) return { matchedCount: 0, modifiedCount: 0 };

    const current = items[index];
    const updatePayload = update.$set ? { ...update.$set } : { ...update };
    delete updatePayload.$set;

    items[index] = {
      ...current,
      ...updatePayload,
      updatedAt: new Date().toISOString()
    };

    writeDb(db);
    return { matchedCount: 1, modifiedCount: 1, doc: items[index] };
  }

  async deleteOne(query) {
    const db = readDb();
    const items = db[this.name] || [];
    const index = items.findIndex(item => matchQuery(item, query));
    if (index === -1) return { deletedCount: 0 };

    db[this.name].splice(index, 1);
    writeDb(db);
    return { deletedCount: 1 };
  }

  async deleteMany(query) {
    const db = readDb();
    const items = db[this.name] || [];
    const remaining = items.filter(item => !matchQuery(item, query));
    const deletedCount = items.length - remaining.length;
    db[this.name] = remaining;
    writeDb(db);
    return { deletedCount };
  }
}

function matchQuery(item, query) {
  for (const key of Object.keys(query)) {
    if (query[key] !== undefined && item[key] !== query[key]) {
      return false;
    }
  }
  return true;
}

const db = {
  users: new Collection('users'),
  analyses: new Collection('analyses'),
  knowledgeMaps: new Collection('knowledgeMaps'),
  generatedQuestions: new Collection('generatedQuestions'),
  quizAttempts: new Collection('quizAttempts'),
  revisions: new Collection('revisions'),
  bookmarks: new Collection('bookmarks'),
  readingHistory: new Collection('readingHistory'),
  topicPerformance: new Collection('topicPerformance')
};

module.exports = db;
