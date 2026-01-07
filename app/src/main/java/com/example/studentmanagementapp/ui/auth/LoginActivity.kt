package com.example.studentmanagementapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.MainActivity
import com.example.studentmanagementapp.databinding.ActivityLoginBinding
import com.example.studentmanagementapp.viewmodel.AuthResult
import com.example.studentmanagementapp.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (authViewModel.isLoggedIn()) {
            navigateToHome()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            binding.btnLogin.isEnabled = false
            authViewModel.login(email, password) { result ->
                binding.btnLogin.isEnabled = true
                when (result) {
                    is AuthResult.Success -> navigateToHome()
                    is AuthResult.NetworkUnavailable -> showToast(getString(com.example.studentmanagementapp.R.string.network_required))
                    is AuthResult.Error -> showToast(result.message)
                }
            }
        }

        binding.tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
