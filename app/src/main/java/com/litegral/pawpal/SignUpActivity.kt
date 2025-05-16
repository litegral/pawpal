package com.litegral.pawpal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var alreadyHaveAccountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_sign_up) // Corrected based on file name

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.button)
        alreadyHaveAccountTextView = findViewById(R.id.textView2)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // TODO: Add password strength validation (e.g., min length)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { sendVerificationTask ->
                                if (sendVerificationTask.isSuccessful) {
                                    Toast.makeText(baseContext, "Registration successful. Verification email sent to ${user.email}. Please verify and sign in.",
                                        Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(baseContext, "Registration successful, but failed to send verification email: ${sendVerificationTask.exception?.message}",
                                        Toast.LENGTH_LONG).show()
                                }
                                // Navigate to SignInActivity regardless of verification email sending status
                                val intent = Intent(this, SignInActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }

        alreadyHaveAccountTextView.setOnClickListener {
            // Navigate to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            // Optionally finish this activity if you don't want it in the back stack
            // finish()
        }
    }
} 