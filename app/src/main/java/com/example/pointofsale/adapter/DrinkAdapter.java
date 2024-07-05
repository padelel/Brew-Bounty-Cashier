package com.example.pointofsale.adapter;

import android.content.Context;
import android.util.Log;
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

    private static final String TAG = "DrinkAdapter";

    private Context mContext;
    private List<Drink> mDrinkList;
    private OnItemClickListener mListener;
    private Dialog mDialog;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface Dialog {
        void onClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        mContext = context;
        mDrinkList = drinkList;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_drink, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink currentDrink = mDrinkList.get(position);
        holder.name.setText(currentDrink.getName());
        holder.category.setText(currentDrink.getCategory());
        holder.description.setText(currentDrink.getDescription());
        holder.price.setText(String.valueOf(currentDrink.getPrice()));

        // Log untuk memeriksa URL gambar sebelum dimuat
        Log.d(TAG, "Image URL: " + currentDrink.getImageURL());

        // Load image using Picasso
        Picasso.get()
                .load(currentDrink.getImageURL())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && mListener != null) {
                    mListener.onItemClick(adapterPosition);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && mDialog != null) {
                    mDialog.onClick(adapterPosition);
                    return true; // consume the long click
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDrinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {

        TextView name, category, description, price;
        ImageView imageView;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            category = itemView.findViewById(R.id.tvCategory);
            description = itemView.findViewById(R.id.tvDescription);
            price = itemView.findViewById(R.id.tvPrice);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
