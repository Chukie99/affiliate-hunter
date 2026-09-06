package com.affiliatehunter.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Product(
    val id: String, val name: String, val price: Long, val sold: Int, val rating: Double,
    val reviewCount: Int, val shopName: String, val shopAgeDays: Int, val imageUrl: String?,
    val link: String, val category: String, val soldPerDay: Double = 0.0,
    val velocityBadge: String? = null, val commissionRate: Double = 0.08,
    val commissionEst: Long = 0L, val score: Double = 0.0, val isOP: Boolean = false,
    val isFlagged: Boolean = false, val flagReason: String? = null
)

@Entity(tableName="favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, val name: String, val price: Long, val sold: Int,
    val rating: Double, val link: String, val affiliateLink: String, val category: String,
    val addedAt: Long = System.currentTimeMillis()
)
