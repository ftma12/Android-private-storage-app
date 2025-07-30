package com.example.privatefilestorageapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class CustomListObjectsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private CustomListAdapter customListAdapter;
    private ArrayList<ListItemModel> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custom_list_objects);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.custom_listview_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Find the ListView
        ListView custom_list_view = findViewById(R.id.custom_listview);

        // Create empty list using model class
        itemList = new ArrayList<>();

        // Load Settings list automatically
        loadSettingsList();

        // Set up the adapter
        customListAdapter = new CustomListAdapter(this, itemList, true); // 'true' means it's a settings list
        custom_list_view.setAdapter(customListAdapter);
        customListAdapter.notifyDataSetChanged(); // Ensure UI updates
    }

    private void loadSettingsList() {
        System.out.println("Debug: Loading Settings List");

        // Add sample data for settings
        itemList.add(new ListItemModel("Change Password", R.drawable.baseline_lock_24));
        itemList.add(new ListItemModel("Logout", R.drawable.baseline_lock_24));
    }
}
