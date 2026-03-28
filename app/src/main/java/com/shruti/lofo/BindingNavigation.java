package com.shruti.lofo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import android.widget.Toast; // ⭐ ADDED for Toast

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.shruti.lofo.auth.Login;

public class BindingNavigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_nav);

        mAuth = FirebaseAuth.getInstance();

        // 1. Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Find Navigation Views and Drawer Layout
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_drawer);
        BottomNavigationView bottomNavView = findViewById(R.id.nav_view);

        // 3. Setup Navigation Controller Safely
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment instanceof NavHostFragment) {
            navController = ((NavHostFragment) navHostFragment).getNavController();
        } else {
            // Should not happen if activity_bind_nav.xml is correct
            return;
        }

        // 4. Setup AppBarConfiguration (Updated IDs to match the drawer's shared destinations)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, // Assumed primary start destination
                R.id.lost_drawer,      // Matches the drawer and graph ID
                R.id.found_drawer,     // Matches the drawer and graph ID
                R.id.navigation_help   // Kept for bottom nav consistency
        )
                .setOpenableLayout(drawer)
                .build();

        // 5. Link Navigation Views to Controller
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavView, navController);

        // 5a. Set CUSTOM Listener for Drawer View (Handles Logout and other Nav destinations)
        navigationView.setNavigationItemSelectedListener(this);

        // 6. Update Drawer Header (Must be done after setup)
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            DrawerManipulator.updateDrawerHeader(this, headerView);
        }
    }

    // 7. IMPLEMENT CUSTOM NAVIGATION ITEM SELECTED LISTENER
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout_drawer) {
            // 1. Sign out of Firebase
            if (mAuth != null) {
                mAuth.signOut();
            }

            // ⭐ FIX: Add "Logout Successful" Toast here ⭐
            Toast.makeText(this, "Logged Out Successfully!", Toast.LENGTH_SHORT).show();

            // 2. Navigate back to the Login/Main Activity (Changed to Login.class for clarity)
            Intent intent = new Intent(this, Login.class);

            // ⭐ CRITICAL FIX: Use flags to clear activity stack and prevent subsequent login issues ⭐
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return true;
        }

        // Handle all other items using the NavController
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

        // Close the drawer if navigation was successful
        if (handled) {
            drawer.closeDrawers();
        }

        return handled;
    }

    // 8. Handle the Back/Up arrow in the Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}