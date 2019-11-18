package com.example.musicqueue.ui.queue;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musicqueue.Constants;
import com.example.musicqueue.MainActivity;
import com.example.musicqueue.R;

import com.example.musicqueue.models.Queue;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.annotation.Target;

import com.example.musicqueue.ui.songs.SongsActivity;

public class QueueFragment extends Fragment {

    private static final String TAG = "QueueFragment";

    private QueueViewModel queueViewModel;

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference queueCollection;
    private FirestoreRecyclerAdapter<Queue, QueueHolder> adapter;

    private LinearLayoutManager linearLayoutManager;


    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        queueViewModel = ViewModelProviders.of(this).get(QueueViewModel.class);
        View root = inflater.inflate(R.layout.fragment_queue, container, false);

        setColors();

        queueCollection = firestore.collection(Constants.FIRESTORE_QUEUE_COLLECTION);

        recyclerView = root.findViewById(R.id.queue_recycler);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        setUpAdapter();

        Button songFragBtn = root.findViewById(R.id.song_fragment_button);
        songFragBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SongsActivity.class));
            }
        });

        return root;
    }

    private void setUpAdapter() {
        Query baseQuery = queueCollection;

        FirestoreRecyclerOptions<Queue> options =
                new FirestoreRecyclerOptions.Builder<Queue>()
                        .setQuery(baseQuery, new SnapshotParser<Queue>() {
                            @NonNull
                            @Override
                            public Queue parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                                Log.v(TAG, snapshot.toString());
                                return new Queue(
                                        snapshot.get("name").toString(),
                                        snapshot.get("location").toString(),
                                        snapshot.get("docid").toString(),
                                        (Timestamp) snapshot.get("created"),
                                        (Integer) snapshot.get("songCount"));
                            }
                        }).build();

        adapter = new FirestoreRecyclerAdapter<Queue, QueueHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull QueueHolder holder, int position, @NonNull Queue model) {
                holder.setDocId(model.getDocId());
                holder.setName(model.getName());
                holder.setLocation(model.getLocation());
                holder.setSongSize(model.getSongCount());
                holder.setFavorite(false);
            }


            @NonNull
            @Override
            public QueueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.queue_card_layout, parent, false);

                return new QueueHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);

        }

    private void setColors() {
        String PRIMARY_COLOR = "#192125";
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(Color.parseColor(PRIMARY_COLOR)));
        ((MainActivity)getActivity()).updateStatusBarColor(PRIMARY_COLOR);
        ((MainActivity)getActivity()).updateStatusBarIconColor(false);
        Toolbar toolbar = getActivity().findViewById(R.id.action_bar);
        if (toolbar!= null){
            String COLOR_FONT_LIGHT = "#F5F5F5";
            toolbar.setTitleTextColor(Color.parseColor(COLOR_FONT_LIGHT));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        super.onStop();
    }
}