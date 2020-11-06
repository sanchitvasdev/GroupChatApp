package com.sanchit.groupchatapp.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.sanchit.groupchatapp.ChatRoomActivity
import com.sanchit.groupchatapp.R
import com.sanchit.groupchatapp.models.User
import kotlinx.android.synthetic.main.fragment_signup.*
import java.time.LocalDate

/**
 * This fragment subclass represents sign up page
 * where user can enter his/her details such as
 * photo,name,state and city.
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */

class SignUpFragment : Fragment() {

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var downloadUrl: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        val rootView = inflater.inflate(R.layout.fragment_signup, container, false)
        val userImgView: ShapeableImageView =
            rootView.findViewById(R.id.userImgView) as ShapeableImageView
        val nextBtn: Button = rootView.findViewById(R.id.nextBtn) as Button

        userImgView.setOnClickListener {
            checkPermissionForImage()
        }

        /**
         * Checks whether user has entered all the details.
         */
        nextBtn.setOnClickListener {
            val name = nameEt.text.toString()
            val state = StateEt.text.toString()
            val city = CityEt.text.toString()
            if (!::downloadUrl.isInitialized) {
                Toast.makeText(activity, "Image cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (name.isEmpty()) {
                Toast.makeText(activity, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (state.isEmpty()) {
                Toast.makeText(activity, "State cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (city.isEmpty()) {
                Toast.makeText(activity, "City cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                /**
                 * If all details are filled create a new user instance.
                 */
                val user = User(
                    name,
                    state,
                    city,
                    downloadUrl,
                    downloadUrl/*Needs to thumbnail url*/,
                    auth.uid!!,
                    LocalDate.now().toString()
                )
                /**
                 * Add the user into database and starts ChatRoomActivity.
                 */
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                    startActivity(intent)
                }.addOnFailureListener {
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                    startActivity(intent)
                    nextBtn.isEnabled = true
                }
            }
        }

        return rootView
    }

    /**
     * Checks whether user has allowed permissions for taking images from his/her storage.
     */
    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED)
                && (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                /**
                 * Request permission again if users denies the permission.
                 */
                requestPermissions(
                    permission,
                    1001
                )
                requestPermissions(
                    permissionWrite,
                    1002
                )
            } else {
                /**
                 * Calls pickImageFromGallery() function if user allows permissions.
                 */
                pickImageFromGallery()
            }
        }
    }

    /**
     * Picks the image chosen by user from gallery.
     */
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    /**
     * Gives the result of the Intent.ACTION_PICK.
     *
     * @param requestCode Taking requestCode from intent.
     * @param resultCode Code for getting the result.
     * @param data Getting user data.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                /**
                 * Sets the image in the imageView and
                 * calls startUpload function.
                 */
                userImgView.setImageURI(it)
                startUpload(it)
            }
        }
    }

    /**
     * Uploads image into firebase storage.
     *
     * @param it Passed uri object.
     */
    private fun startUpload(it: Uri) {
        nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                /**
                 * Throws Exception if task is not successful.
                 */
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                nextBtn.isEnabled = true
            } else {
                nextBtn.isEnabled = true
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error Occurred", Toast.LENGTH_SHORT).show()
        }
    }
}