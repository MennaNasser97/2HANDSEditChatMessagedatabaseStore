package com.example.a2hands.NotificationsPackage;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.NotificationsPackage.NotificationFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;
import com.example.a2hands.homePackage.RatingPackage.Rating;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyNotificationRecyclerViewAdapter extends RecyclerView.Adapter<MyNotificationRecyclerViewAdapter.ViewHolder> {

    private final List<Notification> notifisList;
    private final OnListFragmentInteractionListener mListener;

    public MyNotificationRecyclerViewAdapter(List<Notification> items, OnListFragmentInteractionListener listener) {
        notifisList = items;
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public LinearLayout notifiHelpReqContainer;
        public TextView notificationContent;
        public TextView  notificationDate;
        public CircleImageView notificationPic;
        public Button acceptReq;
        public Button refuseReq;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            notifiHelpReqContainer = view.findViewById(R.id.notifiHelpReqContainer);
            notificationContent = view.findViewById(R.id.notificationContent);
            notificationDate = view.findViewById(R.id.notificationDate);
            notificationPic = view.findViewById(R.id.notificationPic);
            acceptReq = view.findViewById(R.id.acceptReq);
            refuseReq = view.findViewById(R.id.refuseReq);

        }

    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int pos) {
        //check type of notifi
        if(notifisList.get(pos).type.equals("HELP_REQUEST")) {
            holder.notifiHelpReqContainer.setVisibility(View.VISIBLE);
            holder.acceptReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotifiAndhelpReq(pos,holder);
                    /////add user to ratings
                    Rating rating = new Rating();
                    //rating subscriber is who will be rated by publisher
                    rating.subscriber_id = notifisList.get(pos).publisher_id;
                    rating.publisher_id = notifisList.get(pos).subscriber_id;
                    rating.post_id = notifisList.get(pos).post_id;
                    rating.subscriber_pic = notifisList.get(pos).publisher_pic;
                    rating.subscriber_name = notifisList.get(pos).publisher_name;
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ratings");
                    String rating_id = ref.push().getKey();
                    rating.rating_id = rating_id;
                    rating.review_text="";
                    rating.date = new Date();
                    ref.child(rating_id).setValue(rating);
                    Toast.makeText(holder.mView.getContext(),"Help request accepted", Toast.LENGTH_SHORT).show();
                    //update counter for ratings
                    FirebaseDatabase.getInstance().getReference("counter").child(rating.post_id )
                            .child("ratings_count").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            int curr_ = mutableData.getValue(Integer.class);
                            mutableData.setValue(curr_ +1);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                    //end update counter for ratings

                    //delete help request
                    FirebaseFirestore.getInstance().collection("help_requests")
                            .document(notifisList.get(pos).help_request_id).delete();

                }
            });
            holder.refuseReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotifiAndhelpReq(pos,holder);
                    Toast.makeText(holder.mView.getContext(),"Help Request Refused",Toast.LENGTH_SHORT).show();
                    //delete help request
                    FirebaseFirestore.getInstance().collection("help_requests")
                            .document(notifisList.get(pos).help_request_id).delete();
                }
            });
        }

        else{

        }
        holder.notificationContent.setText(notifisList.get(pos).content);
        PrettyTime p = new PrettyTime();
        holder.notificationDate.setText(p.format(notifisList.get(pos).date));
//load pic of notifi publisher and put into the image view
        FirebaseStorage.getInstance().getReference()
                .child("Profile_Pics/" +notifisList.get(pos).publisher_id+ "/"
                        + notifisList.get(pos).publisher_pic)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString()).into(holder.notificationPic);
            }
        });


    }
    void deleteNotifiAndhelpReq(final int pos,final ViewHolder holder){
        //delete help request record from firestore db
        FirebaseFirestore.getInstance().collection("help_requests")
                .document(notifisList.get(pos).help_request_id)
                .delete();
        //delete this notification
        FirebaseDatabase.getInstance().getReference("notifications")
                .child(notifisList.get(pos).notification_id).removeValue();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return notifisList.size();
    }
}
