package com.example.apicar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.apicar.R
import com.example.apicar.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var mVerificationId: String? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        binding.btnSendCode.setOnClickListener {
            val phone = binding.editPhone.text.toString()
            if (phone.isNotEmpty()) {
                enviarCodigoSms(phone)
            } else {
                Toast.makeText(this, "Digite o número (+55...)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVerifyCode.setOnClickListener {
            val code = binding.editCode.text.toString()
            if (code.length == 6 && mVerificationId != null) {
                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                fazerLogin(credential)
            } else {
                Toast.makeText(this, "Digite os 6 dígitos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarCodigoSms(phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    fazerLogin(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    mVerificationId = verificationId
                    binding.btnSendCode.visibility = View.GONE
                    binding.containerVerify.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity, "Código enviado!", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun fazerLogin(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Sucesso! Você entrou.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Código incorreto ou expirado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, MainActivity::class.java)
    }
}