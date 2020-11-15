package mx.tec.p14maps.DB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import mx.tec.p14maps.DB.model.LocationModel

@Dao
interface LocationDao {
    @Query("SELECT * FROM LocationModel")
    fun getLocations() : List<LocationModel>
    @Insert
    fun insertLocation(location : LocationModel)
    @Query("DELETE FROM LocationModel")
    fun deleteLocations()
}