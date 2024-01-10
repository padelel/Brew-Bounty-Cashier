package com.example.pointofsale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.CartItem;
import com.example.pointofsale.model.Order;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderlist;
    private List<CartItem> cartitems;
    private Dialog dialog;

    public interface Dialog {
        void onClick(int pos);
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public OrderAdapter(Context context, List<Order> orderlist) {
        this.context = context;
        this.orderlist = orderlist;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.customerName.setText(orderlist.get(position).getCustomerName());
        holder.orderId.setText(orderlist.get(position).getOrderId());

        // Get the list of cart items for the current order
        List<CartItem> cartItems = orderlist.get(position).getCartItems();

        // Convert the cart items to a string representation
        String cartItemsText = convertCartItemsToString(cartItems);

        // Set the converted string to the TextView
        holder.cartItems.setText(cartItemsText);
    }

    // Add this method to your OrderAdapter class
    private String convertCartItemsToString(List<CartItem> cartItems) {
        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through each CartItem and append its details to the string
        for (CartItem cartItem : cartItems) {
            String menu = cartItem.getMenu();
            int quantity = cartItem.getKuantitas();
            double price = cartItem.getHarga();
            double totalPrice = cartItem.getTotalPrice();

            // Customize the format based on your needs
            String itemDetails = String.format("%s - %dx %.2f = %.2f", menu, quantity, price, totalPrice);

            stringBuilder.append(itemDetails).append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return orderlist.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, orderId, cartItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.tvcustomername);
            orderId = itemView.findViewById(R.id.tvorderid);
            cartItems = itemView.findViewById(R.id.tvcartitems);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null) {
                        dialog.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }
}
