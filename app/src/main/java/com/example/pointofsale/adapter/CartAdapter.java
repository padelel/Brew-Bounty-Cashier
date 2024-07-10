package com.example.pointofsale.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItemList;
    private CartAdapterListener listener;

    public CartAdapter(List<CartItem> cartItemList, CartAdapterListener listener) {
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.txtMenuName.setText(cartItem.getMenu());
        holder.txtQuantity.setText(String.valueOf(cartItem.getKuantitas()));
        holder.txtTotalPrice.setText(String.format("Total: Rp.%.2f", cartItem.getTotalPrice()));

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = cartItem.getKuantitas() + 1;
            cartItem.setKuantitas(newQuantity);
            holder.txtQuantity.setText(String.valueOf(newQuantity));
            holder.txtTotalPrice.setText(String.format("Total: Rp.%.2f", cartItem.getTotalPrice()));
            listener.onItemQuantityChanged(cartItem, newQuantity);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = cartItem.getKuantitas() - 1;
            if (newQuantity > 0) {
                cartItem.setKuantitas(newQuantity);
                holder.txtQuantity.setText(String.valueOf(newQuantity));
                holder.txtTotalPrice.setText(String.format("Total: Rp.%.2f", cartItem.getTotalPrice()));
                listener.onItemQuantityChanged(cartItem, newQuantity);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            cartItemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItemList.size());
            listener.onItemDeleted(cartItem);
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMenuName;
        TextView txtQuantity;
        TextView txtTotalPrice;
        ImageView menuImage;
        Button btnIncrease;
        Button btnDecrease;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMenuName = itemView.findViewById(R.id.txt_pesanan);
            txtQuantity = itemView.findViewById(R.id.txt_quantity);
            txtTotalPrice = itemView.findViewById(R.id.txt_jmlharga);
            menuImage = itemView.findViewById(R.id.menuimage);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface CartAdapterListener {
        void onItemQuantityChanged(CartItem cartItem, int newQuantity);
        void onItemDeleted(CartItem cartItem);
    }
}
