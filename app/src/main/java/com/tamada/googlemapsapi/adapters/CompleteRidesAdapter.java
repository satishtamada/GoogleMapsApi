package com.tamada.googlemapsapi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.models.ListItemModel;

import java.util.ArrayList;

/**
 * Created by satish on 18/2/16.
 */
public class CompleteRidesAdapter extends RecyclerView.Adapter<CompleteRidesAdapter.MyViewHolder> {
    private final Context context;
    private ArrayList<ListItemModel> listItemModelArrayList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView rideRoute;
        public final TextView rideTime;


        public MyViewHolder(View view) {
            super(view);
            rideRoute = (TextView) view.findViewById(R.id.idItemTitle);
            rideTime = (TextView) view.findViewById(R.id.idItemDesc);

        }
    }


    public CompleteRidesAdapter(ArrayList<ListItemModel> ridesList, Context context) {
        this.listItemModelArrayList = ridesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ListItemModel listItemModel = listItemModelArrayList.get(position);
        holder.rideRoute.setText(listItemModel.getItemName());
        holder.rideTime.setText(listItemModel.getItemDesc());
    }

    @Override
    public int getItemCount() {
        return listItemModelArrayList.size();
    }
}
