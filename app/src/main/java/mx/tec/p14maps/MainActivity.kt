package mx.tec.p14maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import mx.tec.p14maps.DB.AppDB
import mx.tec.p14maps.DB.AccessMethods.LoadData
import mx.tec.p14maps.DB.model.LocationModel

class MainActivity : AppCompatActivity(), LocationListener, LoadData.OnDataLoaded {
    private lateinit var locationManager : LocationManager
    private lateinit var mapa: GoogleMap
    private var saveDB = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full Screen config
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        configureMapAndMenu()
    }

    private fun configureMapAndMenu(){
        var nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        verifyPermissions(this)

        var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{googleMap ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.isMyLocationEnabled = true
                mapa = googleMap
            }
        }

        // Menu Configuration
        nav.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.start -> enableLocationRecord()
                R.id.stop -> disableLocationRecord()
                R.id.restore -> loadPoints()
                R.id.clear -> clearMapMenu()
                R.id.deleteDB -> deleteDB()
            }
            true
        }
    }

    private fun enableLocationRecord(){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.enable_record)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, which ->
                saveDB = true

                Toast.makeText(this@MainActivity, R.string.enable_record_confirmation, Toast.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle(R.string.enable_record_title)
        alert.show()
    }

    private fun disableLocationRecord(){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.stop_record)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, which ->
                saveDB = false

                Toast.makeText(this@MainActivity, R.string.stop_record_confirmation, Toast.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle(R.string.stop_record_title)
        alert.show()
    }

    private fun loadPoints(){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.load_points)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, which ->

                var loadData = LoadData(this@MainActivity)
                loadData.execute()
            }
            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()

            })

        val alert = dialogBuilder.create()
        alert.setTitle(R.string.load_points_title)
        alert.show()
    }

    private fun deleteDB(){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.delete_db)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, which ->

                //  DELETE DB
                Thread{
                    val db = AppDB.getInstance(this)
                    db.locationDao().deleteLocations()
                }.start()

                // Message to User
                Toast.makeText(this, R.string.erased_confirmation, Toast.LENGTH_SHORT)
                    .show()

                // Clear map points
                clearMap()
            }
            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()

            })
        val alert = dialogBuilder.create()
        alert.setTitle(R.string.delete_db_title)
        alert.show()
    }

    private fun clearMapMenu(){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.clear_map)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, which ->

                // Clear map points
                clearMap()

                // Message to User
                Toast.makeText(this@MainActivity, R.string.clear_map_confirmation, Toast.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()

            })

        val alert = dialogBuilder.create()
        alert.setTitle(R.string.delete_db_title)
        alert.show()
    }

    private fun clearMap(){
        mapa.clear()
    }

    private fun registerLocation(location : LocationModel){
        Thread{
            val db = AppDB.getInstance(this)
            db.locationDao().insertLocation(location)
        }.start()
    }

    override fun onLocationChanged(location: Location) {
        if(saveDB) {
            mapa.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
            val location = LocationModel(0, location.latitude, location.longitude)
            registerLocation(location)
        }
    }

    private fun verifyPermissions(context : Activity){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)){
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.location_needed)
                    .setTitle(R.string.permission_needed)
                    .setPositiveButton(R.string.accept){_, _->
                        ActivityCompat.requestPermissions(context,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
                    }
                    .show()
            }
            else{
                ActivityCompat.requestPermissions(context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
            }
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this@MainActivity)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            10 -> {
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){

                }
                else{
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this@MainActivity)
                }
            }
        }
    }

    override fun onDataLoaded(data: List<LocationModel>) {
        if(data != null && data.size > 0){
            for(locat in data)
                mapa.addMarker(MarkerOptions().position(LatLng(locat.latitude, locat.longitude)))

            Toast.makeText(this, R.string.points_loaded, Toast.LENGTH_SHORT)
                .show()
        }
        else
            Toast.makeText(this, R.string.no_points_stored, Toast.LENGTH_SHORT)
                .show()
    }
}