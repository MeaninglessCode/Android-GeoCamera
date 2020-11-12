package com.github.meaninglesscode.geocameraapp.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.meaninglesscode.geocameraapp.R

/**
 * Class implementing [AppCompatActivity] representing the entry point activity for this
 * application.
 */
class MapsActivity: AppCompatActivity() {
    // Configuration for toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Override of [AppCompatActivity.onCreate] that initializes the [NavController] and sets up
     * the [AppBarConfiguration].
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreate] method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.geo_camera_act)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController: NavController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration.Builder(R.id.mapsFragment).build()

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Override of [AppCompatActivity.onSupportNavigateUp]
     *
     * @returns [Boolean]
     */
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}