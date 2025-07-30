package com.example.privatefilestorageapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {

    private Context context;
    private List<ListItemModel> items;
    private LayoutInflater inflater;
    private boolean isSettingsList;

    public CustomListAdapter(Context context, List<ListItemModel> items, boolean isSettingsList) {
        this.context = context;
        this.items = items;
        this.isSettingsList = isSettingsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
//ai
        // Bind UI Components
        MaterialCardView cardView = convertView.findViewById(R.id.root_card_view);
        ImageView icon = convertView.findViewById(R.id.item_icon);
        TextView title = convertView.findViewById(R.id.item_text);

        // Get item data
        ListItemModel item = items.get(position);
        icon.setImageResource(item.getIconResId());
        title.setText(item.getTitle());

        // Set alternating background colors
        if (position % 2 == 0) {
            cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.md_theme_surfaceContainerLow)
            );
        } else {
            cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.md_theme_surfaceContainer)
            );
        }

        return convertView;
    }
}