package com.example.ui.screens.knowledgemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KnowledgeMapData
import com.example.data.model.MapNode
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftPurple
import com.example.ui.viewmodel.KnowledgeMapUiState
import com.example.ui.viewmodel.KnowledgeMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeMapScreen(
    topic: String,
    contextText: String,
    viewModel: KnowledgeMapViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(topic) {
        if (uiState is KnowledgeMapUiState.Idle) {
            viewModel.generateKnowledgeMap(topic, contextText)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Knowledge Map: $topic",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveKnowledgeMap(topic, topic)
                        isSaved = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Save Knowledge Map",
                            tint = if (isSaved) ElectricCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("knowledge_map_screen")
        ) {
            when (val state = uiState) {
                is KnowledgeMapUiState.Loading, is KnowledgeMapUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = IndigoPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Constructing Knowledge Graph...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gemini is mapping concept linkages, institutions, policies, and exam relationships.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is KnowledgeMapUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Failed to Generate Mind Map",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.generateKnowledgeMap(topic, contextText) },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is KnowledgeMapUiState.Success -> {
                    InteractiveGraphCanvas(
                        mapData = state.mapData,
                        onNodeClick = { viewModel.selectNode(it) }
                    )
                }
            }

            // Node Detail Bottom Sheet
            if (selectedNode != null) {
                val node = selectedNode!!
                ModalBottomSheet(
                    onDismissRequest = { viewModel.selectNode(null) },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = getNodeColor(node.type).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = node.type.uppercase(),
                                    color = getNodeColor(node.type),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.selectNode(null) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = node.label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = node.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveGraphCanvas(
    mapData: KnowledgeMapData,
    onNodeClick: (MapNode) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Compute positions in concentric / tree layout
    val nodePositions = remember(mapData) {
        calculateNodePositions(mapData.nodes)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1427))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 2.8f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        // Mind map canvas rendering edges and glow lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + offsetX, size.height / 2f + offsetY)

            // Draw Relationships / Edges
            for (edge in mapData.relationships) {
                val fromPos = nodePositions[edge.fromId]
                val toPos = nodePositions[edge.toId]
                if (fromPos != null && toPos != null) {
                    val p1 = Offset(center.x + fromPos.x * scale, center.y + fromPos.y * scale)
                    val p2 = Offset(center.x + toPos.x * scale, center.y + toPos.y * scale)

                    drawLine(
                        color = Color(0xFF334E84),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.5f * scale
                    )
                }
            }
        }

        // Overlay node cards on the canvas
        Box(modifier = Modifier.fillMaxSize()) {
            mapData.nodes.forEach { node ->
                val pos = nodePositions[node.id] ?: Offset.Zero
                val nodeColor = getNodeColor(node.type)

                Card(
                    modifier = Modifier
                        .padding(
                            start = (200f + pos.x * scale + offsetX).coerceAtLeast(10f).dp,
                            top = (250f + pos.y * scale + offsetY).coerceAtLeast(10f).dp
                        )
                        .clickable { onNodeClick(node) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF14203D)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(nodeColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = node.label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = node.type,
                                color = nodeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Zoom / Reset Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = NavyDeep,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { scale = (scale + 0.25f).coerceAtMost(2.8f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }
            }

            Surface(
                shape = CircleShape,
                color = NavyDeep,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { scale = (scale - 0.25f).coerceAtLeast(0.6f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
            }

            Surface(
                shape = CircleShape,
                color = NavyDeep,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.FitScreen, contentDescription = "Reset View", tint = ElectricCyan)
                }
            }
        }

        // Legend
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = NavyDeep.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendPill("Concept", RoyalBlue)
                LegendPill("Org", Color(0xFF10B981))
                LegendPill("Policy", Color(0xFFF59E0B))
                LegendPill("Event", Color(0xFFEC4899))
            }
        }
    }
}

@Composable
fun LegendPill(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

fun getNodeColor(type: String): Color {
    return when (type.lowercase()) {
        "concept" -> RoyalBlue
        "organization", "org" -> Color(0xFF10B981)
        "policy", "scheme", "law" -> Color(0xFFF59E0B)
        "event", "milestone" -> Color(0xFFEC4899)
        "fact" -> ElectricCyan
        else -> SoftPurple
    }
}

fun calculateNodePositions(nodes: List<MapNode>): Map<String, Offset> {
    val positions = mutableMapOf<String, Offset>()
    if (nodes.isEmpty()) return positions

    // Root node at (0, 0)
    val root = nodes.firstOrNull { it.parentId == null } ?: nodes.first()
    positions[root.id] = Offset(0f, -120f)

    // Children positioned radially / across levels
    val children = nodes.filter { it.id != root.id }
    val count = children.size
    val radius = 180f

    for (i in children.indices) {
        val angle = (2.0 * Math.PI * i / count) - (Math.PI / 2.0)
        val x = (radius * Math.cos(angle)).toFloat()
        val y = (radius * Math.sin(angle)).toFloat()
        positions[children[i].id] = Offset(x, y)
    }

    return positions
}
