package com.webcrs.rescueteam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class HelpNeededRecyclerViewAdapter extends RecyclerView.Adapter<HelpNeededRecyclerViewAdapter.MailListViewHolder> {

    private static final String TAG = HelpNeededRecyclerViewAdapter.class.getSimpleName();

    private final MainActivity.ClickListener listener;
    private final List<HelpNeededListModel> itemsList;
    private Context context;

    public HelpNeededRecyclerViewAdapter(List<HelpNeededListModel> helpNeededListModels,
                                         MainActivity.ClickListener clickListener, Context context) {
        this.listener = clickListener;
        this.itemsList = helpNeededListModels;
        this.context = context;
    }

    public class MailListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView contactNumbers, sendDateTime, alternativeNumbers, sosNeeded, description;
        Button direction, call;
        private WeakReference<MainActivity.ClickListener> listenerRef;

        public MailListViewHolder(View itemView, MainActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            contactNumbers = itemView.findViewById(R.id.contactNumbers);
            sendDateTime = itemView.findViewById(R.id.dateTime);
            alternativeNumbers = itemView.findViewById(R.id.alternativeNumbers);
            sosNeeded = itemView.findViewById(R.id.sosNeeded);
            description = itemView.findViewById(R.id.description);

            direction = itemView.findViewById(R.id.showDirection);
            call = itemView.findViewById(R.id.makeCall);

            direction.setOnClickListener(this);
            call.setOnClickListener(this);

        }

        // onClick Listener for view
        @Override
        public void onClick(View v) {

            if (v.getId() == direction.getId()) {
                Toast.makeText(v.getContext(), "direction PRESSED = " +
                        String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();


            } else if (v.getId() == call.getId()) {
                Toast.makeText(v.getContext(), "call PRESSED = " +
                        String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();


                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:9074659707"));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                context.startActivity(callIntent);



            }

            listenerRef.get().onPositionClicked(getAdapterPosition());
        }

    }


    @NonNull
    @Override
    public MailListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_help_needed, parent, false);

        return new MailListViewHolder(itemView,listener);
    }

    @Override
    public void onBindViewHolder(MailListViewHolder holder, int position) {

        holder.sosNeeded.setText("help");
        holder.sendDateTime.setText("03 Aug 3:58 21:45");
        holder.contactNumbers.setText("9746195410,9895013155");
        holder.description.setText("we are stuck in top floor");
        holder.alternativeNumbers.setText("9746195410, 9875641230");

    }

    @Override
    public int getItemCount() {
        return 10;
    }




}
