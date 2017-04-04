package com.iti.tripplanner.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.iti.tripplanner.R;
import com.iti.tripplanner.activities.MainActivity;
import com.iti.tripplanner.activities.TripActivity;
import com.iti.tripplanner.activities.TripDetailsActivity;
import com.iti.tripplanner.models.Trip;
import com.tooltip.Tooltip;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final ViewBinderHelper mViewBinderHelper = new ViewBinderHelper();
    private Activity mParent;

    private ArrayList<Trip> mFilteredTrips, mTrips;

    public RecyclerAdapter(Activity parent) {
        mFilteredTrips = new ArrayList<>();
        mTrips = new ArrayList<>();
        mViewBinderHelper.setOpenOnlyOne(true);
        mParent = parent;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, final int position) {
        holder.tooltipHandler = new Handler();
        holder.nameTextView.setText(mFilteredTrips.get(position).getName());
        holder.timeTextView.setText(mFilteredTrips.get(position).getTime());
        mViewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));

        String text = "Start: " +
                mFilteredTrips.get(holder.getAdapterPosition()).getStartString() +
                "\n\nDestination: " +
                mFilteredTrips.get(holder.getAdapterPosition()).getDestinationString() +
                "\n\nRound Trip: " +
                (mFilteredTrips.get(holder.getAdapterPosition()).isRoundTrip() ? "Yes" : "No") +
                "\n\nNotes :\n\n" +
                mFilteredTrips.get(holder.getAdapterPosition()).getNotes();

        holder.tooltip = new Tooltip.Builder(holder.row)
                .setText(text)
                .setBackgroundColor(Color.parseColor("#EECCE6FF"))
                .setTextStyle(Typeface.BOLD)
                .setMargin(15f)
                .setArrowWidth(70f)
                .setCornerRadius(30f)
                .build();

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mParent)
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFilteredTrips.get(holder.getAdapterPosition()).cancelAlarm(mParent.getApplicationContext());
                                DatabaseAdapter.getInstance().deleteTrip(mFilteredTrips.get(holder.getAdapterPosition()).get_id());
                                remove(holder.getAdapterPosition());
                                holder.swipeRevealLayout.close(true);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                holder.swipeRevealLayout.close(true);
                            }
                        })
                        .show();
            }
        });
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.swipeRevealLayout.close(true);
                Intent intent = new Intent(mParent, TripActivity.class);
                intent.putExtra("Trip", mFilteredTrips.get(holder.getAdapterPosition()));
                intent.putExtra("Position", holder.getAdapterPosition());
                mParent.startActivityForResult(intent, MainActivity.RQST_EDIT_TRIP);
            }
        });
        holder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.tooltip.isShowing())
                    holder.tooltip.dismiss();
                holder.swipeRevealLayout.close(true);
                Intent intent = new Intent(mParent, TripDetailsActivity.class);
                intent.putExtra("Trip", mFilteredTrips.get(holder.getAdapterPosition()));
                intent.putExtra("Position", holder.getAdapterPosition());
                mParent.startActivityForResult(intent, MainActivity.RQST_VIEW_TRIP);
            }
        });

        holder.row.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    holder.tooltipHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.tooltip.show();
                        }
                    }, 200);
                }
                if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    holder.tooltipHandler.removeCallbacksAndMessages(null);
                    if (holder.tooltip.isShowing())
                        holder.tooltip.dismiss();
                }

                return false;
            }
        });
    }

    public void filter(final String filter) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                mFilteredTrips.clear();
                if (TextUtils.isEmpty(filter))
                    mFilteredTrips.addAll(mTrips);
                else
                    for (Trip trip : mTrips)
                        if (trip.getName().toLowerCase().contains(filter.toLowerCase()))
                            mFilteredTrips.add(trip);

                mParent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    public void remove(int position) {
        mFilteredTrips.remove(position);
        mTrips.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Trip trip, int position) {
        mFilteredTrips.add(position, trip);
        mTrips.add(position, trip);
        notifyItemInserted(position);
    }

    public void update(Trip trip, int position) {
        mFilteredTrips.set(position, trip);
        mTrips.set(position, trip);
        notifyItemChanged(position);
    }

    public void clear() {
        mFilteredTrips.clear();
        mTrips.clear();
    }

    public ArrayList<Trip> getAllElements() {
        return mFilteredTrips;
    }

    @Override
    public int getItemCount() {
        return mFilteredTrips.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Tooltip tooltip;
        TextView nameTextView;
        TextView timeTextView;
        TextView btnDelete;
        TextView btnEdit;
        SwipeRevealLayout swipeRevealLayout;
        LinearLayout row;
        Handler tooltipHandler;

        ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.TripName);
            timeTextView = (TextView) v.findViewById(R.id.TripTime);
            btnDelete = (TextView) v.findViewById(R.id.DeleteButton);
            btnEdit = (TextView) v.findViewById(R.id.EditButton);
            row = (LinearLayout) v.findViewById(R.id.ActualRow);
            swipeRevealLayout = (SwipeRevealLayout) v.findViewById(R.id.swipeRevealLayout);
        }
    }
}