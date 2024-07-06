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
import com.example.pointofsale.model.Food;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private static final String TAG = "FoodAdapter";

    private Context mContext;
    private List<Food> mFoodList;
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

    public FoodAdapter(Context context, List<Food> foodList) {
        mContext = context;
        mFoodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food currentFood = mFoodList.get(position);
        holder.name.setText(currentFood.getName());
        holder.category.setText(currentFood.getCategory());
        holder.description.setText(currentFood.getDescription());
        holder.price.setText(String.valueOf(currentFood.getPrice()));
        holder.stock.setText(String.valueOf(currentFood.getStock())); // Set stock value

        // Log untuk memeriksa URL gambar sebelum dimuat
        Log.d(TAG, "Image URL: " + currentFood.getImageURL());

        // Load image using Picasso
        Picasso.get()
                .load(currentFood.getImageURL())
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

        // Example dialog implementation
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
        return mFoodList.size();
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView name, category, description, price, stock;
        ImageView imageView;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            category = itemView.findViewById(R.id.tvCategory);
            description = itemView.findViewById(R.id.tvDescription);
            price = itemView.findViewById(R.id.tvPrice);
            stock = itemView.findViewById(R.id.tvStock); // Initialize the stock TextView
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
