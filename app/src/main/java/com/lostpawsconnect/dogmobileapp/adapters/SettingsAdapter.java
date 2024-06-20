package com.lostpawsconnect.dogmobileapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lostpawsconnect.dogmobileapp.R;
import com.lostpawsconnect.dogmobileapp.interfaces.SettingsListener;

import java.util.List;

public class SettingsAdapter extends BaseAdapter {

    Context mContext;
    List<String> settingsList;

    public SettingsAdapter(Context c, List<String> l) {
        this.mContext = c;
        this.settingsList = l;
    }

    @Override
    public int getCount() {
        return settingsList.size();
    }

    @Override
    public Object getItem(int position) {
        return settingsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_settings, parent, false);
        ImageView imgLogo = mView.findViewById(R.id.imgLogo);
        TextView txtData = mView.findViewById(R.id.txtData);
        String str = settingsList.get(position);
        switch (str) {
            case "Logout":
                imgLogo.setImageResource(R.drawable.ic_logout);
                break;
        }
        txtData.setText(str);
        return mView;
    }
}
