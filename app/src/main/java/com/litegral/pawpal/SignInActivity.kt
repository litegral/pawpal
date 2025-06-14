package com.litegral.pawpal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views untuk login email
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var navigateToSignUpTextView: TextView
    private lateinit var googleSignInButton: Button // Tombol untuk Google Sign-In

    // Properti untuk Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Panggil fungsi untuk mengonfigurasi Google Sign-In
        configureGoogleSignIn()

        // Inisialisasi Views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signInButton = findViewById(R.id.button)
        navigateToSignUpTextView = findViewById(R.id.textView2)
        googleSignInButton = findViewById(R.id.googleSignInButton) // Pastikan ID ini ada di XML

        // Setup Listener untuk login email biasa
        signInButton.setOnClickListener { performEmailSignIn() }

        // Setup Listener untuk teks "Don't have an account?"
        navigateToSignUpTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Setup Listener untuk tombol Google Sign-In
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Wajib untuk integrasi Firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Launcher untuk menangani hasil dari pop-up Google Sign-In
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                    Toast.makeText(this, "Google Sign-In Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isNewUser && user != null) {
                        // Jika ini pertama kali login via Google, simpan datanya ke Firestore
                        saveUserToFirestore(user)
                    } else {
                        Log.d(TAG, "Pengguna lama, login dengan Google berhasil.")
                    }
                    // Langsung navigasi ke MainActivity
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Autentikasi Firebase dengan Google Gagal.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser) {
        val userId = user.uid
        val userDocumentRef = db.collection("users").document(userId)

        val userData = hashMapOf(
            "uid" to userId,
            "displayName" to user.displayName,
            "email" to user.email,
            "profilePhotoUrl" to user.photoUrl?.toString(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        userDocumentRef.set(userData)
            .addOnSuccessListener { Log.d(TAG, "Profil Google Sign-In berhasil dibuat di Firestore.") }
            .addOnFailureListener { e -> Log.e(TAG, "Error membuat profil Google di Firestore", e) }
    }

    private fun performEmailSignIn() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    navigateToMain()
                } else {
                    Toast.makeText(baseContext, "Harap verifikasi email Anda terlebih dahulu.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            } else {
                Toast.makeText(baseContext, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToMain() {
        Toast.makeText(this, "Login berhasil.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}