package mx.tec.p14maps.DB.AccessMethods

import android.content.Context
import android.os.AsyncTask
import mx.tec.p14maps.DB.AppDB
import mx.tec.p14maps.DB.model.LocationModel

class LoadData(context : Context) : AsyncTask<String, Void, Boolean>() {
    val context = context
    var data : List<LocationModel>? = null
    var onDataLoaded : OnDataLoaded

    init{
        onDataLoaded = context as OnDataLoaded
    }

    interface OnDataLoaded{
        fun onDataLoaded(data : List<LocationModel>)
    }

    override fun doInBackground(vararg params: String?): Boolean {
        val db = AppDB.getInstance(context)
        data = db.locationDao().getLocations()

        if(data == null)
            return false
        return true
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)

        if(result!! && data != null)
            onDataLoaded.onDataLoaded(data!!)
    }
}