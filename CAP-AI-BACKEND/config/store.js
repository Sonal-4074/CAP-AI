const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const mongoose = require('mongoose');

const DATA_DIR = path.join(__dirname, '..', 'data');
const DATA_FILE = path.join(DATA_DIR, 'capai_store.json');

// Ensure persistent data directory exists
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

const defaultData = {
  users: [],
  currentAffairs: [],
  knowledgeMaps: [],
  questions: [],
  quizAttempts: [],
  userPerformances: [],
  revisionHistories: [],
  bookmarks: []
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
    console.error('Error reading JSON store:', err.message);
    return JSON.parse(JSON.stringify(defaultData));
  }
}

function writeDb(data) {
  try {
    fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), 'utf8');
  } catch (err) {
    console.error('Error writing JSON store:', err.message);
  }
}

function matchQuery(item, query) {
  for (const [k, v] of Object.entries(query)) {
    if (k === '_id' || k === 'id') {
      const itemId = item._id || item.id;
      if (itemId !== v && itemId !== String(v)) return false;
      continue;
    }
    if (typeof v === 'object' && v !== null) {
      if (v.$regex && !new RegExp(v.$regex, v.$options || '').test(item[k] || '')) return false;
      if (v.$in && !v.$in.includes(item[k])) return false;
    } else if (item[k] !== v) {
      return false;
    }
  }
  return true;
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

  async insertOne(doc) {
    const db = readDb();
    if (!db[this.name]) db[this.name] = [];
    const item = {
      _id: doc._id || crypto.randomUUID(),
      ...doc,
      createdAt: doc.createdAt || new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    db[this.name].push(item);
    writeDb(db);
    return item;
  }

  async updateOne(query, updates) {
    const db = readDb();
    if (!db[this.name]) db[this.name] = [];
    const idx = db[this.name].findIndex(item => matchQuery(item, query));
    if (idx === -1) return null;

    const current = db[this.name][idx];
    const updated = {
      ...current,
      ...(updates.$set || updates),
      updatedAt: new Date().toISOString()
    };
    db[this.name][idx] = updated;
    writeDb(db);
    return updated;
  }

  async deleteOne(query) {
    const db = readDb();
    if (!db[this.name]) return false;
    const initialLen = db[this.name].length;
    db[this.name] = db[this.name].filter(item => !matchQuery(item, query));
    writeDb(db);
    return db[this.name].length < initialLen;
  }
}

module.exports = {
  isMongoActive: () => mongoose.connection.readyState === 1,
  users: new Collection('users'),
  currentAffairs: new Collection('currentAffairs'),
  knowledgeMaps: new Collection('knowledgeMaps'),
  questions: new Collection('questions'),
  quizAttempts: new Collection('quizAttempts'),
  userPerformances: new Collection('userPerformances'),
  revisionHistories: new Collection('revisionHistories'),
  bookmarks: new Collection('bookmarks'),
  readDb,
  writeDb
};
