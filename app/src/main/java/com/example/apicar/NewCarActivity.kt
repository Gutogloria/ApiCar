package com.example.apicar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.apicar.R
import com.example.apicar.databinding.ActivityNewItemBinding
import com.example.apicar.model.Car
import com.example.apicar.model.CarLocation
import com.example.apicar.service.RetrofitClient
import com.example.apicar.service.Result
import com.example.apicar.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private lateinit var imageUri: Uri
    private var imageFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            binding.imageUrl.setText("Imagem capturada com sucesso!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupView()
        setupGoogleMap()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.saveCta.setOnClickListener {
            if (validateForm()) uploadImageToFirebase()
        }

        binding.takePictureCta.setOnClickListener { checkCameraPermission() }
    }

    // --- LÓGICA DO MAPA ---
    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE

        // Tenta pegar a localização atual para centralizar o mapa
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val current = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f))
            }
        }

        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Local do Carro"))
        }
    }

    // --- LÓGICA DA CÂMERA ---
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1002)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

        return FileProvider.getUriForFile(this, "com.example.apicar.fileprovider", imageFile!!)
    }

    // --- SALVAMENTO (FIREBASE + API) ---
    private fun uploadImageToFirebase() {
        if (imageFile == null) {
            saveData() // Se não tirou foto, tenta salvar sem (ou trate como erro)
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("cars/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeFile(imageFile!!.path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos) // 70% para não ser pesado

        binding.loadImageProgress.visibility = View.VISIBLE

        storageRef.putBytes(baos.toByteArray())
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    binding.imageUrl.setText(uri.toString())
                    saveData()
                }
            }
            .addOnFailureListener {
                binding.loadImageProgress.visibility = View.GONE
                Toast.makeText(this, "Erro no upload da imagem", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveData() {
        val carLocation = selectedMarker?.position?.let {
            CarLocation(it.latitude, it.longitude)
        }

        val newCar = Car(
            id = System.currentTimeMillis().toString(), // Gerando um ID temporário
            name = binding.name.text.toString(),
            year = binding.age.text.toString(),         // Mapeado do campo 'age' do XML
            licence = binding.profession.text.toString(), // Mapeado do campo 'profession' do XML
            imageUrl = binding.imageUrl.text.toString(),
            place = carLocation
        )

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.addCar(newCar) }
            withContext(Dispatchers.Main) {
                binding.loadImageProgress.visibility = View.GONE
                if (result is Result.Success) {
                    Toast.makeText(this@NewCarActivity, "Carro salvo!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@NewCarActivity, "Erro ao salvar na API", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.name.text.isNullOrBlank()) return false
        if (binding.age.text.isNullOrBlank()) return false
        if (binding.profession.text.isNullOrBlank()) return false
        if (selectedMarker == null) {
            Toast.makeText(this, "Selecione o local no mapa", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}