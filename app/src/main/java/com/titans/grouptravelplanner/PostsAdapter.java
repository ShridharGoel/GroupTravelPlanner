package com.titans.grouptravelplanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitTextView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private boolean isOwner;
    private Activity activity;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private boolean forComment;

    public PostsAdapter(List<Post> postList, Context context, Activity activity, BottomSheetDialog mmBottomSheetDialog, View statsheetView, boolean forComment) {
        this.postList = postList;
        this.activity=activity;
        this.context = context;
        this.forComment=forComment;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        View view= LayoutInflater.from(context).inflate(R.layout.item_post_feed,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        try {
            setupViews(holder);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(postList.get(position).getUserId().equals(mCurrentUser.getUid())){
            isOwner=true;
            holder.delete.setVisibility(View.VISIBLE);

            holder.delete.setOnClickListener(v -> new MaterialDialog.Builder(context)
                    .title("Delete post")
                    .content("Are you sure do you want to delete this post?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .onPositive((dialog, which) -> {

                        final ProgressDialog pdialog=new ProgressDialog(context);
                        pdialog.setMessage("Please wait...");
                        pdialog.setIndeterminate(true);
                        pdialog.setCancelable(false);
                        pdialog.setCanceledOnTouchOutside(false);
                        pdialog.show();

                        dialog.dismiss();
                        FirebaseFirestore.getInstance().collection("Posts")
                                .document(postList.get(holder.getAdapterPosition()).postId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {


                                    postList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyDataSetChanged();

                                    pdialog.dismiss();


                                })
                                .addOnFailureListener(e -> {
                                    pdialog.dismiss();
                                    Log.e("error",e.getLocalizedMessage());
                                });

                    })
                    .onNegative((dialog, which) -> dialog.dismiss()).show());

        }else{
            isOwner=false;
            holder.delete.setVisibility(View.GONE);
        }


        try {
            mFirestore.collection("Users")
                    .document(postList.get(position).getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        try {
                            if (!documentSnapshot.getString("username").equals(postList.get(holder.getAdapterPosition()).getUsername()) &&
                                    !documentSnapshot.getString("image").equals(postList.get(holder.getAdapterPosition()).getUserimage())) {

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("username", documentSnapshot.getString("username"));
                                postMap.put("userimage", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                holder.user_name.setText(documentSnapshot.getString("username"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.user_image);


                            } else if (!documentSnapshot.getString("username").equals(postList.get(holder.getAdapterPosition()).getUsername())) {


                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("username", documentSnapshot.getString("username"));

                                mFirestore.collection("Posts")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                holder.user_name.setText(documentSnapshot.getString("username"));

                            } else if (!documentSnapshot.getString("image").equals(postList.get(holder.getAdapterPosition()).getUserimage())) {

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("userimage", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.user_image);

                            }


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Error", e.getMessage()));
        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }

        //holder.user_name.setOnClickListener(v -> FriendProfile.startActivity(context,postList.get(holder.getAdapterPosition()).getUserId()));

        holder.comment_btn.setOnFavoriteAnimationEndListener((buttonView, favorite) -> {


            String desc = "<b>" + postList.get(holder.getAdapterPosition()).getUsername() + "</b> : " + postList.get(holder.getAdapterPosition()).getDescription();
            //CommentsActivity.startActivity(context, postList,desc, holder.getAdapterPosition(),isOwner);


        });

        if(forComment){
            holder.comment_btn.performClick();
        }


    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private Uri getBitmapUri(Bitmap bitmap, String name) {

        try {

            OutputStream outputStream=null;
            File dir=new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/TravelPlanner/");

            if(!dir.exists()){
                dir.mkdirs();
            }

            File file=new File(dir,"Post"+System.currentTimeMillis()+".png");
            if(file.exists()){
                file.delete();
            }else{
                file.createNewFile();
            }

            outputStream = new FileOutputStream(file);
            BufferedOutputStream outputStream1=new BufferedOutputStream(outputStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream1);
            outputStream1.flush();
            outputStream1.close();
            return Uri.parse(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getBitmap(FrameLayout view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    private void setupViews(final ViewHolder holder) {

        int pos = holder.getAdapterPosition();

        getLikeandFav(holder);

        if(!TextUtils.isEmpty(postList.get(pos).getUsername())) {
            holder.user_name.setText(postList.get(pos).getUsername());
        }

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                .load(postList.get(pos).getUserimage())
                .into(holder.user_image);

        String timeAgo = TimeAgo.using(Long.parseLong(postList.get(pos).getTimestamp()));

        holder.timestamp.setText(timeAgo);

        if(isOnline()) {
            enableDoubleTap(holder);
        }

        if (postList.get(pos).getImage_count()==0) {

            holder.pager_layout.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.GONE);
            setmImageHolderBg(postList.get(pos).getColor(), holder.mImageholder);
            holder.post_text.setVisibility(View.VISIBLE);
            holder.post_text.setText(postList.get(pos).getDescription());

            holder.share_btn.setOnFavoriteAnimationEndListener((buttonView, favorite) -> {

                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.mImageholder), postList.get(holder.getAdapterPosition()).getName()));
                try {
                    context.startActivity(Intent.createChooser(intent, "Share using..."));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            });


        } else if(postList.get(pos).getImage_count()==1) {

            holder.indicator_holder.setVisibility(View.GONE);

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + postList.get(pos).getUsername() + "</b> : " + postList.get(pos).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));

        }else if(postList.get(pos).getImage_count()>0) {

            holder.indicator2.setDotsClickable(true);
            holder.indicator2.setViewPager(holder.pager);

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.indicator_holder.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + postList.get(pos).getUsername() + "</b> : " + postList.get(pos).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));
        }
    }

    private void enableDoubleTap(final ViewHolder holder) {

        //Double Tap for Photo is set on PostPhotosAdapter

        final GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                animatePhotoLike(holder);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }
        }
        );

        holder.post_text.setOnTouchListener((v, event) -> detector.onTouchEvent(event));

    }

    private void getLikeandFav(final ViewHolder holder) {

        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Liked_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            boolean liked = documentSnapshot.getBoolean("liked");

                            if (liked) {
                                holder.like_btn.setFavorite(true,false);
                            } else {
                                holder.like_btn.setFavorite(false,false);
                            }
                        } else {
                            Log.e("Like", "No document found");

                        }

                        if(isOnline()) {
                            holder.like_btn.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                @Override
                                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                    if (favorite) {
                                        Map<String, Object> likeMap = new HashMap<>();
                                        likeMap.put("liked", true);

                                        try {

                                            mFirestore.collection("Posts")
                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                    .collection("Liked_Users")
                                                    .document(mCurrentUser.getUid())
                                                    .set(likeMap)
                                                    .addOnSuccessListener(aVoid -> {

                                                    })
                                                    .addOnFailureListener(e -> Log.e("Error like", e.getMessage()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Map<String, Object> likeMap = new HashMap<>();
                                        likeMap.put("liked", false);

                                        try {

                                            mFirestore.collection("Posts")
                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                    .collection("Liked_Users")
                                                    .document(mCurrentUser.getUid())
                                                    //.set(likeMap)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //holder.like_count.setText(String.valueOf(Integer.parseInt(holder.like_count.getText().toString())-1));
                                                            //Toast.makeText(context, "Unliked post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Error unlike", e.getMessage());
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Like", e.getMessage());
                    }
                });

        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Saved_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            boolean fav = documentSnapshot.getBoolean("Saved");

                            if (fav) {
                                holder.sav_button.setFavorite(true,false);
                            } else {
                                holder.sav_button.setFavorite(false,false);
                            }
                        } else {
                            Log.e("Fav", "No document found");

                        }

                        if(isOnline()) {
                            holder.sav_button.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                @Override
                                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                    if (favorite) {

                                        Map<String, Object> favMap = new HashMap<>();
                                        favMap.put("Saved", true);

                                        try {

                                            mFirestore.collection("Posts")
                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                    .collection("Saved_Users")
                                                    .document(mCurrentUser.getUid())
                                                    .set(favMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            Map<String, Object> postMap = new HashMap<>();

                                                            postMap.put("userId", postList.get(holder.getAdapterPosition()).getUserId());
                                                            postMap.put("name", postList.get(holder.getAdapterPosition()).getName());
                                                            postMap.put("username", postList.get(holder.getAdapterPosition()).getUsername());
                                                            postMap.put("timestamp", postList.get(holder.getAdapterPosition()).getTimestamp());
                                                            postMap.put("image_count", postList.get(holder.getAdapterPosition()).getImage_count());
                                                            postMap.put("description", postList.get(holder.getAdapterPosition()).getDescription());
                                                            postMap.put("color", postList.get(holder.getAdapterPosition()).getColor());

                                                            mFirestore.collection("Users")
                                                                    .document(mCurrentUser.getUid())
                                                                    .collection("Saved_Posts")
                                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                                    .set(postMap)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("Error add fav", e.getMessage());
                                                                }
                                                            });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Error fav", e.getMessage());
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {

                                        Map<String, Object> favMap = new HashMap<>();
                                        favMap.put("Saved", false);

                                        try {

                                            mFirestore.collection("Posts")
                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                    .collection("Saved_Users")
                                                    .document(mCurrentUser.getUid())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFirestore.collection("Users")
                                                                    .document(mCurrentUser.getUid())
                                                                    .collection("Saved_Posts")
                                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                                    .delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.e("Error remove fav", e.getMessage());
                                                                        }
                                                                    });

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Error fav", e.getMessage());
                                                        }
                                                    });

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Fav", e.getMessage());
                    }
                });

    }

    private void animatePhotoLike(final ViewHolder holder) {
        holder.vBgLike.setVisibility(View.VISIBLE);
        holder.ivLike.setVisibility(View.VISIBLE);

        holder.vBgLike.setScaleY(0.1f);
        holder.vBgLike.setScaleX(0.1f);
        holder.vBgLike.setAlpha(1f);
        holder.ivLike.setScaleY(0.1f);
        holder.ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetLikeAnimationState(holder);
            }
            @Override
            public void onAnimationStart(Animator animation) {
                holder.like_btn.setFavorite(true,true);
            }
        });
        animatorSet.start();

    }

    private void resetLikeAnimationState(ViewHolder holder) {
        holder.vBgLike.setVisibility(View.INVISIBLE);
        holder.ivLike.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView user_image;
        private TextView user_name, timestamp, post_desc;
        private MaterialFavoriteButton sav_button, like_btn, share_btn, comment_btn;
        private FrameLayout mImageholder;
        private FrameLayout pager_layout;
        private RelativeLayout indicator_holder;
        private AutofitTextView post_text;
        private ImageView delete;
        private ViewPager pager;
        private View vBgLike;
        private ImageView ivLike;
        private DotsIndicator indicator2;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            user_image = mView.findViewById(R.id.post_user_image);
            like_btn = mView.findViewById(R.id.like_button);
            vBgLike = mView.findViewById(R.id.vBgLike);
            ivLike = mView.findViewById(R.id.ivLike);
            user_name = mView.findViewById(R.id.post_username);
            timestamp = mView.findViewById(R.id.post_timestamp);
            post_desc = mView.findViewById(R.id.post_desc);
            post_text = mView.findViewById(R.id.post_text);
            pager=mView.findViewById(R.id.pager);
            pager_layout=mView.findViewById(R.id.pager_layout);
            comment_btn = mView.findViewById(R.id.comment_button);
            share_btn = mView.findViewById(R.id.share_button);
            delete = mView.findViewById(R.id.delete_button);
            sav_button = mView.findViewById(R.id.save_button);
            mImageholder = mView.findViewById(R.id.image_holder);
            indicator2 = mView.findViewById(R.id.indicator);
            indicator_holder = mView.findViewById(R.id.indicator_holder);

        }
    }

    private void setmImageHolderBg(String color, FrameLayout mImageholder) {
        switch (Integer.parseInt(color)) {
            case 1:
                mImageholder.setBackgroundResource(R.drawable.gradient_1);
                break;
            case 2:
                mImageholder.setBackgroundResource(R.drawable.gradient_2);
                break;
            case 3:
                mImageholder.setBackgroundResource(R.drawable.gradient_3);
                break;
            case 4:
                mImageholder.setBackgroundResource(R.drawable.gradient_4);
                break;
            case 5:
                mImageholder.setBackgroundResource(R.drawable.gradient_5);
                break;
            case 6:
                mImageholder.setBackgroundResource(R.drawable.gradient_6);
                break;
            case 7:
                mImageholder.setBackgroundResource(R.drawable.gradient_7);
                break;
            case 8:
                mImageholder.setBackgroundResource(R.drawable.gradient_8);
                break;
            case 9:
                mImageholder.setBackgroundResource(R.drawable.gradient_9);
                break;
            case 10:
                mImageholder.setBackgroundResource(R.drawable.gradient_10);
                break;
            case 11:
                mImageholder.setBackgroundResource(R.drawable.gradient_11);
                break;
            case 12:
                mImageholder.setBackgroundResource(R.drawable.gradient_12);
                break;
            case 13:
                mImageholder.setBackgroundResource(R.drawable.gradient_13);
                break;
            case 14:
                mImageholder.setBackgroundResource(R.drawable.gradient_14);
                break;
            case 15:
                mImageholder.setBackgroundResource(R.drawable.gradient_15);
                break;
            case 16:
                mImageholder.setBackgroundResource(R.drawable.gradient_16);
                break;
            case 17:
                mImageholder.setBackgroundResource(R.drawable.gradient_17);
                break;
            case 18:
                mImageholder.setBackgroundResource(R.drawable.gradient_18);
                break;
            case 19:
                mImageholder.setBackgroundResource(R.drawable.gradient_19);
                break;
        }

    }
}
