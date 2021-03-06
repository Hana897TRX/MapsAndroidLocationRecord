package mx.tec.p14maps.DB.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class LocationModel(
        @PrimaryKey(autoGenerate = true)
        @NotNull
        @ColumnInfo(name="id")
        val id : Int,
        @ColumnInfo(name="latitude")
        val latitude : Double,
        @ColumnInfo(name="longitude")
        val longitude : Double)