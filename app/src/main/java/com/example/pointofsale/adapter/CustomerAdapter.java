package com.example.pointofsale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.R;
import com.example.pointofsale.model.Customer;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<Customer> customerlist;
    private Dialog dialog;

    public interface Dialog {
        void onClick(int pos);
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public CustomerAdapter(Context context, List<Customer> customerlist) {
        this.context = context;
        this.customerlist = customerlist;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_customer, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.name.setText(customerlist.get(position).getName());
        holder.email.setText((customerlist.get(position).getEmail()));
        holder.address.setText((customerlist.get(position).getAddress()));
        holder.phone.setText((customerlist.get(position).getPhone()));
    }

    @Override
    public int getItemCount() {
        return customerlist.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, address, phone;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvname);
            email = itemView.findViewById(R.id.tvemail);
            address = itemView.findViewById(R.id.tvaddress);
            phone = itemView.findViewById(R.id.tvphone);
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
