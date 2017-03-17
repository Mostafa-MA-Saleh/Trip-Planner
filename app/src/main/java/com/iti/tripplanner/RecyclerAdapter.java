package com.iti.tripplanner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.tooltip.Tooltip;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final ViewBinderHelper mViewBinderHelper = new ViewBinderHelper();
    private Activity mParent;

    private ArrayList<Trip> mTrips;

    RecyclerAdapter(Activity parent) {
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
        holder.nameTextView.setText(mTrips.get(position).getName());
        holder.timeTextView.setText(mTrips.get(position).getTime());
        mViewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));

        String text = "Start: " +
                mTrips.get(holder.getAdapterPosition()).getStartString() +
                "\n\nDestination: " +
                mTrips.get(holder.getAdapterPosition()).getDestinationString() +
                "\n\nRound Trip: " +
                (mTrips.get(holder.getAdapterPosition()).isRoundTrip() ? "Yes" : "No") +
                "\n\nNotes :\n\n" +
                mTrips.get(holder.getAdapterPosition()).getNotes();

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
                                mTrips.get(holder.getAdapterPosition()).cancelAlarm(mParent.getApplicationContext());
                                new DBAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid()).deleteTrip(mTrips.get(holder.getAdapterPosition()).get_id());
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
                intent.putExtra("Trip", mTrips.get(holder.getAdapterPosition()));
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
                intent.putExtra("Trip", mTrips.get(holder.getAdapterPosition()));
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

    void remove(int position) {
        mTrips.remove(position);
        notifyItemRemoved(position);
    }

    void add(Trip trip, int position) {
        mTrips.add(position, trip);
        notifyItemInserted(position);
    }

    void update(Trip trip, int position) {
        mTrips.set(position, trip);
        notifyItemChanged(position);
    }

    void clear() {
        mTrips.clear();
    }

    ArrayList<Trip> getAllElements() {
        return mTrips;
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
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