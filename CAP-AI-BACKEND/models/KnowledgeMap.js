const mongoose = require('mongoose');

const mapNodeSchema = new mongoose.Schema({
  id: { type: String, required: true },
  label: { type: String, required: true },
  type: {
    type: String,
    enum: ['concept', 'organization', 'policy', 'event', 'fact'],
    default: 'concept'
  },
  description: { type: String, default: '' },
  parentId: { type: String, default: null }
});

const mapEdgeSchema = new mongoose.Schema({
  fromId: { type: String, required: true },
  toId: { type: String, required: true },
  label: { type: String, default: '' }
});

const knowledgeMapSchema = new mongoose.Schema(
  {
    topic: {
      type: String,
      required: true,
      trim: true
    },
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      default: null
    },
    mainTopic: {
      type: String,
      required: true
    },
    nodes: [mapNodeSchema],
    relationships: [mapEdgeSchema],
    articleId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'CurrentAffairs',
      default: null
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.models.KnowledgeMap || mongoose.model('KnowledgeMap', knowledgeMapSchema);
