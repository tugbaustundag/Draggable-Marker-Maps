package com.smality.dragmarkermaps

import android.Manifest
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var currentMarker: Marker? = null
    private lateinit var mMap: GoogleMap
    var currentLocation: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation()
    }

    private fun getLocation() {
        //Konum ile ilgili izinlermizi tanımlıyoruz
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        //Kullanıcının bulunduğu yerin konumunu alıyoruz
        val task = fusedLocationProviderClient!!.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                //Xml'de ekledğimiz SupportMapFragment sınıfını burada kullanarak haritayı arayüzde gösteriyoruz.
                val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
                supportMapFragment.getMapAsync(this@MainActivity)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        //Haritada Marker ilk oluşturulur ve oanki adres bilgisini haritada gösterir
        moveMarker(latLng)

        //Marker'ın sürüklendiği anı dinleyen fonksiyon
        googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            //Marker haritada sürüklendikten sonra bırakıldığı anı yakalayan fonksiyon
            override fun onMarkerDragEnd(marker: Marker) {

                if (currentMarker != null) {
                    currentMarker?.remove()
                }
                //Haritada bırakıldığı noktadakı enlem boylam bilgisini alıp, marker oluşturma fonksyonu gerçekleşir
                val newlatLng = LatLng(marker.position.latitude, marker.position.longitude)
                moveMarker(newlatLng)
            }
            override fun onMarkerDrag(marker: Marker) {}
        })
    }
    //Marker(işaretçi) oluşturma ve özelliklerini tanımlama
    private fun moveMarker(latLng: LatLng) {
        //Marker başlık, konumundaki adresi belirtme ve drag(sürükleme) özelliğini aktif etme
        val markerOptions = MarkerOptions().position(latLng).title("Konumum")
            .snippet(getTheAddress(latLng!!.latitude, latLng!!.longitude)).draggable(true)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        //Marker hareket ettiğinde ilgili noktaya zoom yapmayı sağlar
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        currentMarker = mMap.addMarker(markerOptions)
        currentMarker?.showInfoWindow()
    }
    //Fonksiyona gelen enlem, boylam bilgisine göre açık adres değerini verir
    private fun getTheAddress(latitude: Double, longitude: Double): String? {
        var retVal = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            retVal = addresses[0].getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return retVal
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
    }
}
