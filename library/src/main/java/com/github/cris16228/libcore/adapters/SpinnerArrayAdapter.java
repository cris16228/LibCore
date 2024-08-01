package com.github.cris16228.libcore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.cris16228.libcore.R;
import com.google.android.material.divider.MaterialDivider;

import java.util.ArrayList;
import java.util.List;

public class SpinnerArrayAdapter extends ArrayAdapter<String> {


    public SpinnerArrayAdapter(@NonNull Context context, ArrayList<String> list) {
        super(context, 0, list);
    }

    public SpinnerArrayAdapter(@NonNull Context context, List<String> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, true);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, false);
    }

    private View initView(int position, View convertView, ViewGroup parent, boolean hideDivider) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.template_spinner_item, parent, false);
        }
        TextView text = convertView.findViewById(R.id.spinner_text);
        MaterialDivider divider = convertView.findViewById(R.id.spinner_divider);
        text.setText(getItem(position));
        if (position == getCount() - 1 || hideDivider) {
            divider.setVisibility(View.GONE);
        }
        return convertView;
    }
}
