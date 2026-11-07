package com.example.data.model

data class KnowledgeMapData(
    val mainTopic: String,
    val nodes: List<MapNode>,
    val relationships: List<MapEdge> = emptyList()
)

data class MapNode(
    val id: String,
    val label: String,
    val type: String, // "concept", "organization", "policy", "event", "fact"
    val description: String,
    val parentId: String? = null
)

data class MapEdge(
    val fromId: String,
    val toId: String,
    val label: String
)
