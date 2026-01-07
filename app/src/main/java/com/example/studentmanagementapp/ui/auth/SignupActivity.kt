package com.example.studentmanagementapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.MainActivity
import com.example.studentmanagementapp.databinding.ActivitySignupBinding
import com.example.studentmanagementapp.viewmodel.AuthResult
import com.example.studentmanagementapp.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty()
            val email = binding.etEmail.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            binding.btnSignup.isEnabled = false
            authViewModel.signup(name, email, password) { result ->
                binding.btnSignup.isEnabled = true
                when (result) {
                    is AuthResult.Success -> navigateHome()
                    is AuthResult.NetworkUnavailable -> showToast(getString(com.example.studentmanagementapp.R.string.network_required))
                    is AuthResult.Error -> showToast(result.message)
                }
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
