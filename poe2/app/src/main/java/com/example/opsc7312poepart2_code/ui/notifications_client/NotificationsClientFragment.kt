package com.example.poe2.ui.notifications_client

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.opsc7312poepart2_code.ui.ApiClient
import com.example.opsc7312poepart2_code.ui.ApiResponse
import com.example.opsc7312poepart2_code.ui.ApiService
import com.example.opsc7312poepart2_code.ui.Notification
import com.example.opsc7312poepart2_code.ui.login_client.LoginClientFragment.Companion.loggedInClientUserId
import com.example.poe2.R
import com.example.poe2.databinding.FragmentNotificationsClientBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsClientFragment : Fragment() {

    private var _binding: FragmentNotificationsClientBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnViewNotifications: Button
    private lateinit var notificationsListView: ListView
    private lateinit var notificationsAdapter: ArrayAdapter<String>
    private val notificationsList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsClientBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize the ListView
        notificationsAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, notificationsList)
        binding.notificationsListView.adapter =
            notificationsAdapter // Access ListView through binding

        // Initialize views
        btnViewNotifications = view.findViewById(R.id.btnViewNotifications)
        notificationsListView = view.findViewById(R.id.notificationsListView)

        // Set up the button click listener
        btnViewNotifications.setOnClickListener {
            Log.d("NotificationsClient", "View notifications button clicked")
            loadNotifications()
        }

        // Initialize the ImageButtons
        val ibtnHome: ImageButton = binding.ibtnHome // Access ImageButton through binding

        // Set OnClickListener for the Home button
        ibtnHome.setOnClickListener {
            Log.d("NotificationsClient", "Home button clicked")
            findNavController().navigate(R.id.action_nav_notifications_client_to_nav_menu_client)
        }

        return view
    }

    private fun loadNotifications() {
        val functions = FirebaseFunctions.getInstance()
        Log.d("NotificationsClient", "Starting to load notifications")

        // Call the Firebase Cloud Function to get notifications
        functions.getHttpsCallable("getPatientNotifications")
            .call()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("NotificationsClient", "Successfully called Firebase Cloud Function")

                    val result = task.result?.data as Map<*, *>?
                    Log.d("NotificationsClient", "Function result: $result")

                    val notificationsData = result?.get("notifications") as? List<Map<String, String>> ?: emptyList()

                    // Clear the list and add the notifications to the list
                    notificationsList.clear()
                    notificationsData.forEach { notification ->
                        val message = notification["message"] ?: "No message"
                        notificationsList.add(message)
                        Log.d("NotificationsClient", "Added notification: $message")
                    }

                    // Notify the adapter of the data change
                    notificationsAdapter.notifyDataSetChanged()

                    // Check if the notifications list is empty
                    if (notificationsList.isEmpty()) {
                        Log.d("NotificationsClient", "No notifications available")
                        Toast.makeText(context, "No notifications available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Log the error if the task fails
                    val error = task.exception?.message ?: "Unknown error"
                    Log.e("NotificationsClient", "Failed to load notifications: $error")
                    Toast.makeText(context, "Failed to load notifications: $error", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Additional failure listener to capture exceptions
                Log.e("NotificationsClient", "Error calling function: ${exception.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to avoid memory leaks
        Log.d("NotificationsClient", "View destroyed and binding cleared")
    }
}


