package com.example.msalapp_sso_poc

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.example.msalapp_sso_poc.databinding.ActivityMainBinding
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var msalApp: ISingleAccountPublicClientApplication? = null
    private var msalAccount: IAccount? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PublicClientApplication.createSingleAccountPublicClientApplication(
            this,
            R.raw.msal_auth_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    msalApp = application
                    loadAccount()
                }

                override fun onError(exception: MsalException?) {
                    Log.i("PublicClientApplicationInitError", "${exception.toString()}")
                    binding.tvLogs.text = exception.toString()
                }
            }
        )
    }

    private fun initializeUI() {
        binding.btnLogin.setOnClickListener {
            val signInParameters = SignInParameters.builder()
                .withActivity(this)
                .withScopes(arrayOf("user.read").toList())
                .withCallback(getAuthCallback())

            msalApp?.signIn(signInParameters.build())
        }

        binding.btnLogout.setOnClickListener {
            msalApp?.signOut(
                object : ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() {
                        updateUI(null)
                        signOut()
                    }

                    override fun onError(exception: MsalException) {
                        Log.i("Logout Error", "${exception.toString()}")
                        binding.tvLogs.text = exception.toString()
                    }
                }
            )
        }

        val defaultChromeIntent = CustomTabsIntent.Builder().build()
        binding.btnMyapps.setOnClickListener {
            defaultChromeIntent.launchUrl(this, Uri.parse("https://myapps.microsoft.com"))
        }

        binding.btnMyappsWebview.setOnClickListener {
            Intent(this, WebviewActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnSamlWebview.setOnClickListener {
            Intent(this, WebviewSAMLActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializeUI()
        loadAccount()
    }

    private fun loadAccount() {
        if (msalApp == null) {
            return
        }

        msalApp?.getCurrentAccountAsync(
            object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                override fun onAccountLoaded(activeAccount: IAccount?) {
                    msalAccount = activeAccount
                    updateUI(msalAccount)
                }

                override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                    if (currentAccount == null) {
                        msalAccount = null
                        signOut()
                    }
                }

                override fun onError(exception: MsalException) {
                    Log.i("Logout Error", "${exception.toString()}")
                    binding.tvLogs.text = exception.toString()
                }
            }
        )
    }

    private fun getAuthCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                msalAccount = authenticationResult?.account
                updateUI(msalAccount)

                if (authenticationResult != null) {
                    callGraphAPIs(authenticationResult)
                }
            }

            override fun onError(exception: MsalException?) {
                Log.i("AuthCallback Error", "${exception.toString()}")
                binding.tvLogs.text = exception.toString()
            }

            override fun onCancel() {
                Log.i("AuthCallback Error", "${"User Cancelled!"}")
                binding.tvLogs.text = "User Cancelled!"
            }
        }
    }

    private fun updateUI(account: IAccount?) {
        if (account != null) {
            Log.i("ID-Token-Claims", "${account.claims}")
            val username = account.claims?.get("name") as? String
            Log.i("username", "${username}")
            Toast.makeText(this, "You have successfully logged in!", Toast.LENGTH_SHORT).show()
            binding.btnLogin.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnMyapps.visibility = View.VISIBLE
            binding.btnMyappsWebview.visibility = View.VISIBLE
            binding.btnSamlWebview.visibility = View.VISIBLE
            binding.tvGreetings.visibility = View.VISIBLE
            binding.tvGreetings.text = "Welcome: ${username}"
            binding.tvLogs.text = ""
        }
    }

    private fun callGraphAPIs(authenticationResult: IAuthenticationResult) {
        Log.i("Access-Token", authenticationResult.accessToken)
        var token: String = authenticationResult.accessToken
        getUserProfilePic(token, Response.Listener<Bitmap?> { response ->
            Log.d("Profile-Pic-Response", "Response: ${response}")
            if (response != null) {
                val circularBitmap = RoundedBitmapDrawableFactory.create(resources, response)
                circularBitmap.isCircular = true
                binding.profileImage.setImageDrawable(circularBitmap)
            }
        }, Response.ErrorListener { error ->
            Log.d("Profile-Pic-Response-Error", "Error: ${error}")
            binding.tvLogs.text = error.toString()
        })
    }

    private fun getUserProfilePic(
        token: String,
        responseListener: Response.Listener<Bitmap?>,
        errorListener: Response.ErrorListener
    ) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://graph.microsoft.com/v1.0/me/photo/\$value"
        val request = object : ImageRequest(
            url,
            responseListener, // Listener for a successful response
            0, 0,
            ImageView.ScaleType.CENTER_CROP,
            Bitmap.Config.ARGB_8888,
            errorListener // Listener for an error response
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        queue.add(request)
    }

    private fun signOut() {
        Toast.makeText(this, "You have successfully logged out!", Toast.LENGTH_SHORT).show()
        binding.btnLogin.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.btnMyapps.visibility = View.GONE
        binding.btnMyappsWebview.visibility = View.GONE
        binding.btnSamlWebview.visibility = View.GONE
        binding.tvGreetings.visibility = View.GONE
        binding.profileImage.setImageResource(R.drawable.entra_id_logo)
        binding.tvLogs.text = ""
    }
}