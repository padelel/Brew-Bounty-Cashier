package com.example.pointofsale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.Drink;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {

    private Context mContext;
    private List<Drink> mDrinkList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        mContext = context;
        mDrinkList = drinkList;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_drink, parent, false);
        return new DrinkViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink currentDrink = mDrinkList.get(position);
        holder.tvName.setText(currentDrink.getName());
        holder.tvPrice.setText(String.valueOf(currentDrink.getPrice()));
        holder.tvCategory.setText(currentDrink.getCategory());
        holder.tvDescription.setText(currentDrink.getDescription());

        // Load image using Picasso
        Picasso.get()
                .load(currentDrink.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // Placeholder image if loading fails
                .error(R.drawable.error_image) // Error image if Picasso fails to load
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mDrinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public TextView tvPrice;
        public TextView tvCategory;
        public TextView tvDescription;
        public ImageView imageView;

        public DrinkViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
