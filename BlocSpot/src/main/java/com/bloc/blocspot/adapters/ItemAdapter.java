package com.bloc.blocspot.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 2/7/2015.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(ItemAdapter itemAdapter, CheckBox box, PointItem item);
        public void onPopupMenuClicked(ItemAdapter itemAdapter, View view, PointItem item);
    }

    public static interface DataSource {
        public PointItem getPointItem(ItemAdapter itemAdapter, int position);
        public int getItemCount(ItemAdapter itemAdapter);
    }

    private WeakReference<Delegate> delegate;
    private WeakReference<DataSource> dataSource;

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.point_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        if (getDataSource() == null) {
            return;
        }

        PointItem pointItem = getDataSource().getPointItem(this, index);
        itemAdapterViewHolder.update(pointItem);
    }

    @Override
    public int getItemCount() {
        if (getDataSource() == null) {
            return 0;
        }
        return getDataSource().getItemCount(this);
    }

    /*
    *
    * Delegates
    *
     */

    public Delegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            return null;
        }
        return dataSource.get();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = new WeakReference<DataSource>(dataSource);
    }


    /*
    *
    * ItemAdapterViewHolder
    *
     */
    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView location;
        TextView note;
        TextView distance;
        ImageButton popupMenu;
        PointItem item;
        CheckBox visitedBox;
        View noteView;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            location = (TextView) itemView.findViewById(R.id.point_item_location);
            note = (TextView) itemView.findViewById(R.id.point_item_note);
            distance = (TextView) itemView.findViewById(R.id.point_item_distance);
            popupMenu = (ImageButton) itemView.findViewById(R.id.point_item_popup_menu);
            visitedBox = (CheckBox) itemView.findViewById(R.id.point_item_checkbox);
            visitedBox.setClickable(false);
            noteView = itemView;
            
            itemView.setOnClickListener(this);
            popupMenu.setOnClickListener(this);

        }

        void update(PointItem item) {
            this.item = item;
            location.setText(item.getLocation());
            note.setText(item.getNote());
            distance.setText(item.getDistance());

            if(this.item.isVisited()){
                visitedBox.setChecked(true);
            }else{
                visitedBox.setChecked(false);
            }

            String color = BlocSpotApplication.getSharedDataSource().getCategoryColor(item.getCategory());
            if(color.equals("White")){
                visitedBox.setBackgroundResource(R.color.white);
            }else if(color.equals("Red")){
                visitedBox.setBackgroundResource(R.color.red);
            }else if(color.equals("Green")){
                visitedBox.setBackgroundResource(R.color.green);
            }else if(color.equals("Blue")){
                visitedBox.setBackgroundResource(R.color.blue);
            }else if(color.equals("Yellow")){
                visitedBox.setBackgroundResource(R.color.yellow);
            }else if(color.equals("Aqua")){
                visitedBox.setBackgroundResource(R.color.aqua);
            }else if(color.equals("Magenta")){
                visitedBox.setBackgroundResource(R.color.magenta);
            }

        }

        @Override
        public void onClick(View view) {
            if (view == itemView) {
                if (getDelegate() != null) {
                    getDelegate().onItemClicked(ItemAdapter.this, visitedBox, item);
                }
            }else{
                if (getDelegate() != null) {
                    getDelegate().onPopupMenuClicked(ItemAdapter.this, view, item);
                }
            }
        }

    }
}
