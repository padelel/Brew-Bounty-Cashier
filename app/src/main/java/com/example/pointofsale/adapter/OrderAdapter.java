package com.example.pointofsale.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.Order;

import java.util.List;

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

        StringBuilder cartItems = new StringBuilder();
        for (Order.CartItem item : order.getCartItems()) {
            cartItems.append(item.getMenu())
                    .append(" (")
                    .append(item.getKuantitas())
                    .append("), ");
        }

        holder.tvCartItems.setText(cartItems.toString());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvOrderId, tvCartItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvcustomername);
            tvOrderId = itemView.findViewById(R.id.tvorderid);
            tvCartItems = itemView.findViewById(R.id.tvcartitems);
        }
    }
}
