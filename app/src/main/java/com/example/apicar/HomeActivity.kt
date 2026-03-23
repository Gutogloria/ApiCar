package com.example.apicar

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apicar.model.Car
import com.example.apicar.adapter.CarAdapter
import com.example.apicar.databinding.ActivityHomeBinding
import com.example.apicar.service.RetrofitClient
import com.example.apicar.service.safeApiCall
import com.example.apicar.service.Result
import com.example.apicar.database.model.DatabaseBuilder
import com.example.apicar.database.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationPermissionLauncher()
        checkLocationPermission()

        configurarRecyclerView()
        carregarDadosDaApi()

        binding.swipeRefreshLayout.setOnRefreshListener {
            carregarDadosDaApi()
        }

        binding.addCta.setOnClickListener {
            val intent = Intent(this, NewCarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun obterESalvarLocalizacao() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = DatabaseBuilder.getInstance(this@HomeActivity).userLocationDao()
                        dao.insert(UserLocation(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            createdAt = Date()
                        ))
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                obterESalvarLocalizacao()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obterESalvarLocalizacao()
        } else {
            locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDadosDaApi()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_loggout -> {
                onLoggout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLoggout(){
        FirebaseAuth.getInstance().signOut()
        val intent= MainActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun configurarRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun carregarDadosDaApi() {
        binding.swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall<List<Car>> { RetrofitClient.apiService.getCars() }
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Success -> {
                        // AQUI ESTAVA O PROBLEMA - ADICIONADO O CLIQUE:
                        binding.recyclerView.adapter = CarAdapter(result.data) { carro ->
                            val intent = Intent(this@HomeActivity, CarDetailActivity::class.java)
                            intent.putExtra("CAR_ID", carro.id)
                            startActivity(intent)
                        }
                    }
                    is Result.Error -> {
                        Toast.makeText(this@HomeActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}