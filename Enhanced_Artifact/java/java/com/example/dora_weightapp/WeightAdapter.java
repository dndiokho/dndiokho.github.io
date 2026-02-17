package com.example.dora_weightapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WeightAdapter extends ArrayAdapter<WeightItem> {

    public WeightAdapter(Context context, ArrayList<WeightItem> weights) {
        super(context, 0, weights);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WeightItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.weight_list_item, parent, false);
        }

        TextView tvWeight = convertView.findViewById(R.id.tvWeight);
        tvWeight.setText(item.toString());

        return convertView;
    }
}
