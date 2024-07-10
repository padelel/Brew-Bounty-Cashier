package com.example.pointofsale.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvOrderId.setText(order.getOrderId());

        Picasso.get()
                .load(order.getImageURL())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.menuImage);

        StringBuilder menuItems = new StringBuilder();
        StringBuilder prices = new StringBuilder();
        double totalPrice = 0.0;
        for (Order.CartItem item : order.getCartItems()) {
            menuItems.append(item.getMenu());
            prices.append("Rp")
                    .append(NumberFormat.getNumberInstance(new Locale("id", "ID")).format(item.getHarga()))
                    .append(" x ")
                    .append(item.getKuantitas());
            totalPrice += item.getHarga() * item.getKuantitas();
        }

        holder.tvMenuName.setText(menuItems.toString());
        holder.tvPrice.setText(prices.toString());

        // Buat format number angka uang indonesia
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        numberFormat.setMaximumFractionDigits(0);

        // Masukkan angka yang sudah diformat
        String formattedTotalPrice = numberFormat.format(totalPrice);
        holder.tvTotalPrice.setText(String.format("Rp" + formattedTotalPrice));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvOrderId, tvMenuName, tvPrice, tvTotalPrice;
        ImageView menuImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvcustomername);
            tvOrderId = itemView.findViewById(R.id.tvorderid);
            tvMenuName = itemView.findViewById(R.id.tvmenuname);
            tvPrice = itemView.findViewById(R.id.tvprice);
            tvTotalPrice = itemView.findViewById(R.id.tvtotalprice);
            menuImage = itemView.findViewById(R.id.menuimage);
        }
    }
}