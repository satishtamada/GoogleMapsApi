package com.tamada.googlemapsapi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.adapters.CompleteRidesAdapter;
import com.tamada.googlemapsapi.app.AppConfig;
import com.tamada.googlemapsapi.app.AppController;
import com.tamada.googlemapsapi.models.ListItemModel;

import java.util.ArrayList;

/**
 * Created by inventbird on 31/8/17.
 */
public class ListActivity extends AppCompatActivity {
    public ArrayList<ListItemModel> listItemModelArrayList;
    private RecyclerView recyclerView;
    private CompleteRidesAdapter completeRidesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setIcon(R.drawable.ic_tool_icon);
        }
        listItemModelArrayList = new ArrayList<>();
        completeRidesAdapter = new CompleteRidesAdapter(listItemModelArrayList,AppController.getInstance().getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(AppController.getInstance().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        getItems();
        recyclerView.setAdapter(completeRidesAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(position==0){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapsActivity.class));
                }else if(position==4){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapDirectionsActivity.class));
                }else if(position==1){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapControlsActivity.class));
                }else if(position==2){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapPinsActivity.class));
                } else if(position==3){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapsPlaceAutoCompleteActivity.class));
                } else if(position==6){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),MapEventsActivity.class));
                } else if(position==7){
                    startActivity(new Intent(AppController.getInstance().getApplicationContext(),DrawShapesActivity.class));
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void getItems() {
        for (int i = 0; i < AppConfig.itemNames.length; i++) {
            ListItemModel invoiceModel = new ListItemModel( AppConfig.itemNames[i], AppConfig.itemDescriptions[i]);
            listItemModelArrayList.add(invoiceModel);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private final GestureDetector gestureDetector;
        private final ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
