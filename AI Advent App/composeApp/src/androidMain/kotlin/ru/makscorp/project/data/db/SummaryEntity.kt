package ru.makscorp.project.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.makscorp.project.domain.model.ContextSummary

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val originalMessageCount: Int,
    val estimatedTokens: Int,
    val createdAt: Long
) {
    fun toDomainModel(): ContextSummary = ContextSummary(
        id = id,
        content = content,
        originalMessageCount = originalMessageCount,
        estimatedTokens = estimatedTokens
    )

    companion object {
        fun fromDomainModel(summary: ContextSummary, createdAt: Long): SummaryEntity = SummaryEntity(
            id = summary.id,
            content = summary.content,
            originalMessageCount = summary.originalMessageCount,
            estimatedTokens = summary.estimatedTokens,
            createdAt = createdAt
        )
    }
}
