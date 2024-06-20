package com.lostpawsconnect.dogmobileapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lostpawsconnect.dogmobileapp.R;
import com.lostpawsconnect.dogmobileapp.interfaces.ReportListener;
import com.lostpawsconnect.dogmobileapp.model.Dogs;
import com.lostpawsconnect.dogmobileapp.services.VRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    Context mContext;
    List<Dogs> dogsList;
    ReportListener listener;


    public ReportAdapter(Context c, List<Dogs> d, ReportListener l) {
        this.mContext = c;
        this.dogsList = d;
        this.listener = l;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_dog, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        Dogs dogs = dogsList.get(position);
        holder.txtPetName.setText(dogs.getDogName());
        if (dogs.getImageurl() != null && !dogs.getImageurl().equals("")) {
            Picasso.get()
                    .load(dogs.getImageurl()) // Load the image from the web URL
                    .into(holder.imgDog, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully (optional)
                        }

                        @Override
                        public void onError(Exception e) {
                            // Handle any errors that occur during image loading (optional)
                            e.printStackTrace(); // Example: Print the error to the console
                        }
                    });
        }
        holder.itemView.setOnClickListener(v -> listener.onClickListener(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return this.dogsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgDog;
        TextView txtPetName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDog = itemView.findViewById(R.id.imgDog);
            txtPetName = itemView.findViewById(R.id.txtPetName);
        }
    }
}
