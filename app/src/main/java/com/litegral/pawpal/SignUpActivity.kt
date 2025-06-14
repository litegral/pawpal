package com.litegral.pawpal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    // Deklarasi untuk Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Deklarasi untuk View
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var alreadyHaveAccountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_sign_up) // Pastikan nama layout ini benar

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.button)
        alreadyHaveAccountTextView = findViewById(R.id.textView2)

        signUpButton.setOnClickListener {
            performSignUp()
        }

        alreadyHaveAccountTextView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSignUp() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nama, Email, dan Password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal harus 6 karakter.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Membuat pengguna di Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("SignUpActivity", "Registrasi di Firebase Auth BERHASIL.")
                    val user = auth.currentUser

                    if (user != null) {
                        // 2. Simpan profil ke Firestore (berjalan di background)
                        createUserProfileInFirestore(user, name)

                        // --- LOGIKA PENGIRIMAN EMAIL VERIFIKASI DITAMBAHKAN DI SINI ---
                        user.sendEmailVerification()
                            .addOnCompleteListener { sendTask ->
                                if (sendTask.isSuccessful) {
                                    Toast.makeText(baseContext, "Registrasi berhasil. Silakan cek email Anda untuk verifikasi.",
                                        Toast.LENGTH_LONG).show()
                                } else {
                                    Log.w("SignUpActivity", "Gagal mengirim email verifikasi.", sendTask.exception)
                                    Toast.makeText(baseContext, "Registrasi berhasil, namun gagal mengirim email verifikasi.",
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        // --- AKHIR LOGIKA PENGIRIMAN EMAIL ---
                    }

                    // 3. Langsung arahkan ke halaman Sign In
                    navigateToSignIn()

                } else {
                    Log.w("SignUpActivity", "Registrasi di Firebase Auth GAGAL", task.exception)
                    Toast.makeText(baseContext, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Fungsi untuk menyimpan data profil ke Cloud Firestore (tetap sama)
    private fun createUserProfileInFirestore(user: FirebaseUser, name: String) {
        val userId = user.uid
        val userDocumentRef = db.collection("users").document(userId)
        val userData = hashMapOf(
            "uid" to userId,
            "displayName" to name,
            "email" to user.email,
            "profilePhotoUrl" to "", // <-- TAMBAHKAN INI: beri nilai awal kosong
            "createdAt" to FieldValue.serverTimestamp()
        )
        userDocumentRef.set(userData).addOnSuccessListener {
            Log.d("SignUpActivity", "Profil pengguna BERHASIL dibuat di Firestore.")
        }.addOnFailureListener { e ->
            Log.e("SignUpActivity", "Error saat membuat profil di Firestore", e)
        }
    }

    // Fungsi untuk navigasi ke halaman Sign In (tetap sama)
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}