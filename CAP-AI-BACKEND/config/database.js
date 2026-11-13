const mongoose = require('mongoose');

let isConnected = false;

/**
 * Connect to MongoDB Atlas using Mongoose.
 * Provides clear logging of connection state and diagnostic guidance.
 */
const connectDB = async () => {
  const mongoURI = process.env.MONGODB_URI;

  if (!mongoURI || mongoURI.includes('username:password')) {
    console.warn('\n⚠️ [Database Notice]: MONGODB_URI is not set or contains placeholder credentials.');
    console.warn('ℹ️ To connect MongoDB Atlas:');
    console.warn('   1. Create a free cluster at https://cloud.mongodb.com');
    console.warn('   2. Set MONGODB_URI in your .env file: mongodb+srv://<user>:<password>@cluster0.abcde.mongodb.net/cap_ai');
    console.warn('   3. In-memory / persistent file store is currently active for local testing.\n');
    return false;
  }

  try {
    const conn = await mongoose.connect(mongoURI, {
      serverSelectionTimeoutMS: 5000,
    });
    isConnected = true;
    console.log(`✅ [MongoDB Atlas Connected]: ${conn.connection.host} (Database: ${conn.connection.name})`);
    return true;
  } catch (error) {
    isConnected = false;
    console.error(`❌ [MongoDB Connection Error]: ${error.message}`);
    console.warn('ℹ️ Local development dual-store remains active so frontend requests continue to function.');
    return false;
  }
};

const getIsConnected = () => isConnected;

module.exports = { connectDB, getIsConnected };
