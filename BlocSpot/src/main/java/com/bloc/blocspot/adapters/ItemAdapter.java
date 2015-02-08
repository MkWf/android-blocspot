package com.bloc.blocspot.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.api.DataSource;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 2/7/2015.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(ItemAdapter itemAdapter, CheckBox box);
        public void onPopupMenuClicked(ItemAdapter itemAdapter, View view);
    }

    private WeakReference<Delegate> delegate;

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.point_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = BlocSpotApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getItems().get(index));
    }

    @Override
    public int getItemCount() {
        return BlocSpotApplication.getSharedDataSource().getItems().size();
    }

    public Delegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView location;
        TextView note;
        TextView distance;
        ImageButton popupMenu;
        PointItem item;
        CheckBox visitedBox;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            location = (TextView) itemView.findViewById(R.id.point_item_location);
            note = (TextView) itemView.findViewById(R.id.point_item_note);
            distance = (TextView) itemView.findViewById(R.id.point_item_distance);
            popupMenu = (ImageButton) itemView.findViewById(R.id.point_item_popup_menu);
            visitedBox = (CheckBox) itemView.findViewById(R.id.point_item_checkbox);
            itemView.setOnClickListener(this);
            popupMenu.setOnClickListener(this);

        }

        void update(PointItem item) {
            this.item = item;
            location.setText(item.getLocation());
            note.setText(item.getNote());
            distance.setText(item.getDistance());
        }

        @Override
        public void onClick(View view) {
            if (view == itemView) {
                if (getDelegate() != null) {
                    getDelegate().onItemClicked(ItemAdapter.this, visitedBox);
                }
            }else{
                if (getDelegate() != null) {
                    getDelegate().onPopupMenuClicked(ItemAdapter.this, view);
                }
            }
        }

    }
}
