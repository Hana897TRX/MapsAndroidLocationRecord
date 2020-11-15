package mx.tec.p14maps.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mx.tec.p14maps.DB.dao.LocationDao
import mx.tec.p14maps.DB.model.LocationModel

@Database(entities = [LocationModel::class], version = 1)
abstract class AppDB : RoomDatabase(){
    abstract fun locationDao(): LocationDao
    companion object{
        private var INSTANCE: AppDB? = null

        fun getInstance(context: Context) : AppDB{
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context, AppDB::class.java, "LocationStory").build()
            }
            return INSTANCE as AppDB
        }
    }
}