package com.example.apicar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.apicar.R
import com.example.apicar.databinding.ActivityCarDetailBinding
import com.example.apicar.model.Car
import com.example.apicar.service.Result
import com.example.apicar.service.RetrofitClient
import com.example.apicar.service.safeApiCall
import com.example.apicar.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private var car: Car? = null
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Uso correto do DataBinding para a sua tag <layout>
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_detail)

        setupView()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadCar()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.deleteCTA.setOnClickListener { deleteCar() }
        binding.editCTA.setOnClickListener { editCar() }
    }

    private fun loadCar() {
        val carId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            // Agora o safeApiCall espera uma List<Car>
            val result = safeApiCall { RetrofitClient.apiService.getCar(carId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        // Se o servidor mandou uma lista, pegamos o primeiro item dela [0]
                        val listaDeCarros = result.data
                        if (listaDeCarros.isNotEmpty()) {
                            car = listaDeCarros[0]
                            handleOnSuccess()
                        } else {
                            Toast.makeText(this@CarDetailActivity, "Carro não encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is Result.Error -> {
                        Toast.makeText(this@CarDetailActivity, "Erro: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleOnSuccess() {
        car?.let {
            // Preenchimento dos campos igual ao padrão que você usa
            binding.name.text = it.name
            binding.age.text = "Ano: ${it.year}"
            binding.profession.setText(it.licence)
            binding.image.loadUrl(it.imageUrl)

            // O mapa é o único detalhe extra do APICAR
            atualizarMapa()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        atualizarMapa()
    }

    private fun atualizarMapa() {
        val currentCar = car ?: return
        val googleMap = mMap ?: return

        currentCar.place?.let {
            val location = LatLng(it.lat, it.long)
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(location).title(currentCar.name))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun deleteCar() {
        val currentCar = car ?: return // Só deleta se o carro existir
        CoroutineScope(Dispatchers.IO).launch {
            // Chamada para a API
            val result = safeApiCall { RetrofitClient.apiService.deleteCar(currentCar.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(this@CarDetailActivity, "Carro removido com sucesso!", Toast.LENGTH_SHORT).show()
                        finish() // Fecha a tela e volta para a lista
                    }
                    is Result.Error -> {
                        Toast.makeText(this@CarDetailActivity, "Erro ao deletar: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun editCar() {
        val currentCar = car ?: return
        // Pega o que o usuário digitou no campo de placa (profession)
        val novaPlaca = binding.profession.text.toString()

        // Cria uma cópia do carro com a placa nova
        val carroAtualizado = currentCar.copy(licence = novaPlaca)

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateCar(currentCar.id, carroAtualizado)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(this@CarDetailActivity, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                        finish() // Volta para atualizar a lista na Home
                    }
                    is Result.Error -> {
                        Toast.makeText(this@CarDetailActivity, "Erro ao editar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_ID = "arg_id"
        fun newIntent(context: Context, carId: String): Intent {
            return Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, carId)
            }
        }
    }
}
