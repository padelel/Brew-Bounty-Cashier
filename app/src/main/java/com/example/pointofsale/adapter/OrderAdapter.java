package com.example.pointofsale.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.Order;
import com.example.pointofsale.model.MenuItem;

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
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvOrderId, tvTotalPrice;
        RecyclerView recyclerViewMenuItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvcustomername);
            tvOrderId = itemView.findViewById(R.id.tvorderid);
            tvTotalPrice = itemView.findViewById(R.id.tvtotalprice);
            recyclerViewMenuItems = itemView.findViewById(R.id.recyclerview_menu_items);
            recyclerViewMenuItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        public void bind(Order order) {
            tvCustomerName.setText(order.getCustomerName());
            tvOrderId.setText(order.getOrderId());

            List<MenuItem> menuItems = order.convertToMenuItems();
            MenuItemAdapter menuItemAdapter = new MenuItemAdapter(menuItems);
            recyclerViewMenuItems.setAdapter(menuItemAdapter);

            double totalPrice = order.getTotalPrice();
            NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            numberFormat.setMaximumFractionDigits(0);
            String formattedTotalPrice = numberFormat.format(totalPrice);
            tvTotalPrice.setText(String.format("Rp%s", formattedTotalPrice));
        }
    }
}
