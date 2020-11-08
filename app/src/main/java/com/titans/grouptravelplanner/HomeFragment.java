package com.titans.grouptravelplanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<Post> mPostsList;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView mPostsRecyclerView;
    private List<String> mFriendIdList=new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(getActivity());
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        FirebaseApp.initializeApp(getActivity());
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        getView().findViewById(R.id.create_post).setOnClickListener(v ->
                PostsActivity.startActivity(getActivity()));

        getView().findViewById(R.id.find_friends).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), JourneyPlannerActivity.class);
            startActivity(intent);
        });

        refreshLayout=view.findViewById(R.id.refreshLayout);

        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsList = new ArrayList<>();

        mAdapter = new PostsAdapter(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setAdapter(mAdapter);

        refreshLayout.setOnRefreshListener(() -> {

            mPostsList.clear();
            mAdapter.notifyDataSetChanged();
            getAllPosts();

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPostsList.clear();
        mAdapter.notifyDataSetChanged();
        getAllPosts();
    }

    private void getAllPosts() {

        mPostsList.clear();
        mAdapter.notifyDataSetChanged();

        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                mFirestore.collection("Users")
                                        .document(currentUser.getUid())
                                        .collection("Friends")
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {

                                            if (!querySnapshot.isEmpty()) {

                                                for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                                    if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                        mPostsList.add(post);
                                                        refreshLayout.setRefreshing(false);
                                                        mAdapter.notifyDataSetChanged();

                                                    }
                                                }

                                            } else {

                                                getCurrentUsersPosts();

                                            }

                                        })
                                        .addOnFailureListener(e -> {
                                            refreshLayout.setRefreshing(false);
                                            Log.w("Error", "listen:error", e);
                                        });

                            }
                        }


                    }else{
                        refreshLayout.setRefreshing(false);
                        getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Log.w("Error", "listen:error", e);
                    }
                });

    }

    private void getCurrentUsersPosts() {

        mPostsList.clear();
        mAdapter.notifyDataSetChanged();

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if(queryDocumentSnapshots.isEmpty()){

                        refreshLayout.setRefreshing(false);
                        getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                    }else{

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            Post post = documentChange.getDocument().toObject(Post.class).withId(documentChange.getDocument().getId());
                            mPostsList.add(post);
                            refreshLayout.setRefreshing(false);
                            mAdapter.notifyDataSetChanged();
                        }

                        if (mPostsList.isEmpty()) {
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }

                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Log.w("Error", "listen:error", e);
                    }
                });


    }

}
