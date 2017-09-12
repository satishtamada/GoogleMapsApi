package com.tamada.googlemapsapi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.models.PlaceAutoComplete;

import java.util.List;
import java.util.StringTokenizer;


public class AutoCompleteAdapter extends ArrayAdapter<PlaceAutoComplete> {
    private final Context context;
    private final List<PlaceAutoComplete> Places;

    public AutoCompleteAdapter(Context context, List<PlaceAutoComplete> modelsArrayList) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.Places = modelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.place_name);
            holder.location = (TextView) rowView.findViewById(R.id.place_detail);
            rowView.setTag(holder);

        } else
            holder = (ViewHolder) rowView.getTag();
        /***** Get each Model object from ArrayList ********/
        holder.Place = Places.get(position);
        StringTokenizer st=new StringTokenizer(holder.Place.getPlaceDesc(), ",");
        /************  Set Model values in Holder elements ***********/

        holder.name.setText(st.nextToken());
        String desc_detail="";
        for(int i=1; i<st.countTokens(); i++) {
            if(i==st.countTokens()-1){
                desc_detail = desc_detail + st.nextToken();
            }else {
                desc_detail = desc_detail + st.nextToken() + ",";
            }
        }
        holder.location.setText(desc_detail);
        return rowView;
    }

    class ViewHolder {
        PlaceAutoComplete Place;
        TextView name, location;
    }

    @Override
    public int getCount(){
        return Places.size();
    }
}