package com.sanchit.groupchatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sanchit.groupchatapp.ChatRoomActivity
import com.sanchit.groupchatapp.R
import kotlinx.android.synthetic.main.fragment_sign_in.*


/**
 * This fragment subclass represents a sign in page
 * through which user can login or create an account
 * using google account or input email address directly.
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */
class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        val rootView = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val emailIdEt: EditText = rootView.findViewById(R.id.emailIdEt) as EditText
        val passwordEt: EditText = rootView.findViewById(R.id.passwordEt) as EditText
        val nextBtnsignin: Button = rootView.findViewById(R.id.nextBtnsignin) as Button
        val signInBtn: SignInButton = rootView.findViewById(R.id.signInBtn) as SignInButton
        val visibilityimg: ImageView = rootView.findViewById(R.id.visibilityimg) as ImageView

        auth = FirebaseAuth.getInstance()

        /**
         * Configures sign-in options through google account.
         */
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        var visibility = false

        /**
         * Controls visibility of the user password.
         */
        visibilityimg.setOnClickListener {
            visibility = !visibility
            if (visibility == true) {
                visibilityimg.setImageResource(R.drawable.ic_visible)
                passwordEt.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                visibilityimg.setImageResource(R.drawable.ic_invisible)
                passwordEt.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        var check1 = false

        /**
         * Checking correct order of email address entered by user.
         */
        emailIdEt.addTextChangedListener {
            check1 = emailIdEt.text.contains("@gmail.com")
        }

        /**
         * Checking correct order of password entered by user
         * to enable next button.
         */
        passwordEt.addTextChangedListener {
            val check2 = passwordEt.length() >= 8 && !passwordEt.equals("")
            nextBtnsignin.isEnabled = check1 && check2
        }

        /**
         * Implements sign in or creating user account
         * through email address and password entered by user.
         */
        nextBtnsignin.setOnClickListener {
            auth.signInWithEmailAndPassword(emailIdEt.text.toString(), passwordEt.text.toString())
                .addOnSuccessListener {
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                    startActivity(intent)
                }.addOnFailureListener {
                    auth.createUserWithEmailAndPassword(
                        emailIdEt.text.toString(),
                        passwordEt.text.toString()
                    )
                        .addOnSuccessListener {
                            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Check your email address and password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }

        signInBtn.setOnClickListener {
            signIn()
        }

        return rootView
    }

    /**
     * Starting the intent for sign in activity through google account.
     */
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            RC_SIGN_IN
        )
    }

    /**
     * Gives the result of the signInIntent from googleSignInClient.
     *
     * @param requestCode Taking requestCode from intent.
     * @param resultCode Code for getting the result.
     * @param data Getting user data.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /**
         * Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
         */
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                /**
                 * If google Sign In was successful, authenticate with Firebase.
                 */
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                /**
                 * If google Sign In failed, update UI appropriately.
                 */
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        /**
         * Check if user is signed in (non-null) and update UI accordingly.
         */
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    /**
     * Getting credentials of the signed in user.
     *
     * @param idToken Getting idToken of the user.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    /**
                     * If sign in is successful, update UI with the signed-in user's information
                     */
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    /**
                     * If sign in fails, display a message to the user.
                     */
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                    updateUI(null)
                }
            }
    }

    /**
     * Updates UI with the current status of the user.
     *
     * @param user Getting current user.
     */
    private fun updateUI(user: FirebaseUser?) {

        if (user != null) {
            emailIdEt.visibility = View.INVISIBLE
            passwordEt.visibility = View.INVISIBLE
            visibilityimg.visibility = View.INVISIBLE
            nextBtnsignin.visibility = View.INVISIBLE
            ortv.visibility = View.INVISIBLE
            check()
        } else {
            signInBtn.visibility = View.VISIBLE
        }
    }

    /**
     * Checking whether user has signed up already or not.
     */
    private fun check() {
        val database: FirebaseFirestore? = FirebaseFirestore.getInstance()
        val doc = database?.collection("users")?.document(auth.uid.toString())
        doc?.get()?.addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                if (documentSnapshot.data?.get("name") == null) {
                    findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
                } else {
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Declaring specific variables to be used multiple times.
     */
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}