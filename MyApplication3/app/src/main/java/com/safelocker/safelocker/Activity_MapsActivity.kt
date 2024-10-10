package com.safelocker.safelocker

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.Activity_MainActivity
import com.safelocker.R
import com.safelocker.Activity_Tela_Login
import com.safelocker.databinding.ActivityMapsBinding
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class Activity_MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val db = FirebaseFirestore.getInstance() // Referência ao Firestore
    private var currentLatitude: Double = 0.0 // Latitude atual do dispositivo
    private var currentLongitude: Double = 0.0 // Longitude atual do dispositivo
    private lateinit var mMap: GoogleMap // Objeto do Google Map
    private lateinit var binding: ActivityMapsBinding // Objeto de vinculação da atividade
    private lateinit var lastLocation: Location // Última localização conhecida
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Cliente de provedor de localização
    private lateinit var autocompleteFragment: AutocompleteSupportFragment // Fragmento de autocompletar lugares
    private var auth = FirebaseAuth.getInstance() // Objeto de autenticação do Firebase

    // Constante para o código de solicitação de permissão de localização
    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Inflar o layout da atividade
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar o serviço Places
        Places.initialize(applicationContext,getString(R.string.google_map_api_key))

        // Configurar o fragmento de autocompletar lugares
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autoComplete) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                Toast.makeText(this@Activity_MapsActivity,"Erro na busca",Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng!!
                zoomOnMap(latLng)
            }
        })

        // Obter referência ao fragmento do mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar o cliente de provedor de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar o botão de voltar
        binding.btnVoltar.setOnClickListener {
            val intent = Intent(this, Activity_MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    // Função para fazer zoom no mapa para uma determinada LatLng
    fun zoomOnMap(latLng: LatLng){
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,12f)
        mMap?.animateCamera(newLatLngZoom)
    }

    // Método chamado quando o mapa está pronto para ser usado
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Consultar o Firestore para obter informações sobre os armários
        val armariosRef = db.collection("unidades")
        val listaDataClassArmarios = mutableListOf<DataClass_Armario>()
        armariosRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Extrair informações sobre o armário de cada documento
                    val id = document.id
                    val nome = document.getString("referencia")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val codigo = document.getString("codigo_armario")
                    val numero = document.getDouble("numero")
                    val preco = document.getString("preco")
                    val preco_trinta_min = document.getString("preco_trinta_min")
                    val preco_uma_hora = document.getString("preco_uma_hora")
                    val preco_duas_horas = document.getString("preco_duas_horas")
                    val preco_quatro_horas = document.getString("preco_quatro_horas")
                    val referencia = document.getString("referencia")
                    val rua = document.getString("rua")

                    // Verificar se todos os campos necessários estão presentes e não são nulos
                    if (nome != null && latitude != null && longitude != null && codigo != null && numero != null &&
                        referencia != null && rua != null && preco != null && preco_trinta_min != null &&
                        preco_uma_hora != null && preco_duas_horas != null && preco_quatro_horas != null) {

                        // Criar objeto Armario com os dados extraídos
                        val dataClassArmario = DataClass_Armario(id, nome, latitude, longitude, codigo, numero, preco, preco_trinta_min,
                            preco_uma_hora, preco_duas_horas,preco_quatro_horas,referencia, rua)

                        // Adicionar o armário à lista de armários
                        listaDataClassArmarios.add(dataClassArmario)

                        // Adicionar marcador no mapa para o armário
                        mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title(nome))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Erro ao obter documentos: ", exception)
            }

        // Configurar o ouvinte de clique nos marcadores do mapa
        mMap.setOnMarkerClickListener { marker ->
            val lat = marker.position.latitude
            val long = marker.position.longitude
            val distance = calculateDistance(currentLatitude, currentLongitude, lat, long)

            val clickedArmario = listaDataClassArmarios.find { armario ->
                armario.latitude == lat && armario.longitude == long
            }

            // Exibir as informações do armário em um bottom sheet
            clickedArmario?.let { armario ->
                val bottomSheet: View = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
                val distanciaFormatada = String.format("%.2f", distance)

                val numero = armario.numero
                val numeroSemDecimal = if (numero % 1 == 0.0) {
                    // Se o número for um número inteiro, converte para Int
                    numero.toInt().toString()
                } else {
                    // Se o número tiver decimais, mantém como Double
                    numero.toString()
                }

                // Configurar os textos do bottom sheet
                bottomSheet.findViewById<TextView>(R.id.tvAba).text = "A $distanciaFormatada Km de você"
                bottomSheet.findViewById<TextView>(R.id.tvAba2).text = "Ponto ${armario.codigo_armario}"
                bottomSheet.findViewById<TextView>(R.id.tvAba3).text = "${armario.rua}, ${numeroSemDecimal}. Próximo a ${armario.referencia}"

                // Configurar o botão de rota para o armário
                bottomSheet.findViewById<Button>(R.id.btnRota).setOnClickListener {
                    val origemLatitude = currentLatitude
                    val origemLongitude = currentLongitude
                    val destinoLatitude = lat
                    val destinoLongitude = long

                    val uri = "http://maps.google.com/maps?saddr=$origemLatitude,$origemLongitude&daddr=$destinoLatitude,$destinoLongitude"

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@Activity_MapsActivity, "Aplicativo do Google Maps não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }

                // Exibir o bottom sheet
                val dialog = BottomSheetDialog(this@Activity_MapsActivity)
                dialog.setContentView(bottomSheet)
                dialog.show()

                // Verificar se o usuário está perto o suficiente para alugar o armário
                if (distance != null && distance > 0.5) {
                    // Se estiver muito longe, mostrar mensagem
                    bottomSheet.findViewById<Button>(R.id.btnAlugar).setOnClickListener {
                        Toast.makeText(this@Activity_MapsActivity, "Você está muito longe desse local", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    // Se estiver perto o suficiente, verificar se o usuário está logado
                    val user = auth.currentUser
                    if (user == null){
                        // Se não estiver logado, redirecionar para a tela de login
                        bottomSheet.findViewById<Button>(R.id.btnAlugar).setOnClickListener {
                            val intent = Intent(this,Activity_Tela_Login::class.java)
                            startActivity(intent)
                        }
                    }
                    else{
                        // Se estiver logado, permitir alugar o armário
                        bottomSheet.findViewById<Button>(R.id.btnAlugar).setOnClickListener {
                            val intent = Intent(this, Activity_Alugar_Armario::class.java)
                            intent.putExtra("id", armario.id)
                            intent.putExtra("nome", armario.nome)
                            intent.putExtra("latitude", lat)
                            intent.putExtra("longitude", long)
                            intent.putExtra("codigo_armario", armario.codigo_armario)
                            intent.putExtra("numero", armario.numero)
                            intent.putExtra("preco", armario.preco)
                            intent.putExtra("preco_trinta_min", armario.preco_trinta_min)
                            intent.putExtra("preco_uma_hora", armario.preco_uma_hora)
                            intent.putExtra("preco_duas_horas", armario.preco_duas_horas)
                            intent.putExtra("preco_quatro_horas", armario.preco_quatro_horas)
                            intent.putExtra("referencia", armario.referencia)
                            intent.putExtra("rua", armario.rua)
                            intent.putExtra("distancia", distance)
                            startActivity(intent)
                        }
                    }
                }
            }
            true

        }

        setUpMap()
    }

    // Método chamado quando a permissão de localização é solicitada ao usuário
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Acesso à localização negado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,Activity_MainActivity::class.java))
                } else {
                    setUpMap()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Configurar o mapa
    private fun setUpMap(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)

            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location !=null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
            else {
                Toast.makeText(this@Activity_MapsActivity, "Não foi possível obter a localização atual.", Toast.LENGTH_SHORT).show()
            }
            currentLatitude = location.latitude
            currentLongitude = location.longitude

        }
    }

    // Calcular a distância entre duas coordenadas geográficas usando a fórmula de Haversine
    fun calculateDistance(lat1 : Double?, long1 : Double?, lat2 : Double, long2 : Double): Double? {
        val R = 6371  // Raio médio da Terra em quilômetros

        var distance: Double? = null

        if (lat1 != null && long1 != null) {

            val lat1Rad = Math.toRadians(lat1)
            val lon1Rad = Math.toRadians(long1)
            val lat2Rad = Math.toRadians(lat2)
            val lon2Rad = Math.toRadians(long2)

            val deltaLat = lat2Rad - lat1Rad
            val deltaLon = lon2Rad - lon1Rad

            val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            distance = R * c  // Distância em quilômetros

        }
        return distance
    }

    // Adiciona um marcador no mapa para uma determinada coordenada
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap.addMarker(markerOptions)
    }

    // Callback chamado quando um marcador é clicado
    override fun onMarkerClick(p0: Marker) = false

}
