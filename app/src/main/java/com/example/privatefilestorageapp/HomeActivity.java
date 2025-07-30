package com.example.privatefilestorageapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ensure title remains constant

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("            ");
        }

        // Find Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Set Default Fragment to Home
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("HomeActivity", "Navigation Item Selected: " + item.getItemId()); // Debug Log

        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.home) {
            selectedFragment = new HomeDashboardFragment();
        } else if (item.getItemId() == R.id.files) {
            selectedFragment = new FilesFragment();
        } else if (item.getItemId() == R.id.upload) {
            selectedFragment = new UploadFragment();
        } else if (item.getItemId() == R.id.settings) {
            Log.d("HomeActivity", "Settings Fragment Selected");
            selectedFragment = new SettingsFragment();
        }

        if (selectedFragment != null) {
            replaceFragment(selectedFragment);
            return true;
        }

        return false;
    }

    private void replaceFragment(Fragment fragment) {
        Log.d("HomeActivity", "Replacing Fragment: " + fragment.getClass().getSimpleName()); // Debug Log

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}