package com.example.wongwien.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wongwien.MapsActivity;
import com.example.wongwien.OnSwipeTouchListener;
import com.example.wongwien.R;
import com.example.wongwien.ReviewDetailActivity;
import com.example.wongwien.model.ModelMylocation;
import com.example.wongwien.model.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.Myholder> {
    public static final int PATTERN_REVIEW_1 = 0;
    public static final int PATTERN_REVIEW_2 = 1;
    public static final int PATTERN_REVIEW_3 = 2;
    private static final String TAG = "AdapterReview";
    Context context;
    ArrayList<ModelReview> reviews;
    ArrayList<Uri>imageUriArray;
    ModelMylocation mylocation;

    FirebaseUser user;
    String myUid;

    int star, starBeforeChange;
    boolean isUseReview = false;
    double point;
    double avgscore=0;

    int max_w=600;
    int max_h=400;


    public AdapterReview(Context context, ArrayList<ModelReview> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        checkUserStatus();
        switch (viewType) {
            case PATTERN_REVIEW_1:
                View view = LayoutInflater.from(context).inflate(R.layout.row_review_pattern1, parent, false);
                return new Myholder(view);
            case PATTERN_REVIEW_2:
                view = LayoutInflater.from(context).inflate(R.layout.row_review_pattern2, parent, false);
                return new Myholder(view);
            case PATTERN_REVIEW_3:
                view = LayoutInflater.from(context).inflate(R.layout.row_review_pattern3, parent, false);
                return new Myholder(view);
            default:
                view = LayoutInflater.from(context).inflate(R.layout.row_review_pattern1, parent, false);
                return new Myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        loadLocation(reviews.get(position),holder);


        loadAllStarToReviews(reviews.get(position).getrId(), myUid, holder);

        String timeStamp = reviews.get(position).getR_timeStamp();
        String title = reviews.get(position).getR_title();
        Double scorepoint = reviews.get(position).getR_point();

        //conver time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = (String) DateFormat.format("dd/MM/yyyy hh:mm:aa", cal);

        holder.rTitile.setText(title);
        holder.txtPoint.setText(String.valueOf(scorepoint));

        int num = Integer.parseInt(reviews.get(position).getR_num());

        /*
        * Handle Share funciton
        *
        * */
        // TODO: 2/2/2022 share event

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (getItemViewType(position)){
                    case PATTERN_REVIEW_1:
                        shareTextOnly(reviews.get(position).getR_title(),reviews.get(position).getR_desc0());
                        break;
                    case PATTERN_REVIEW_2:
                        BitmapDrawable bitmapDrawable = ((BitmapDrawable) holder.r_image0.getDrawable());
                        Bitmap bitmap = bitmapDrawable .getBitmap();
                        ShareImageAndText(reviews.get(position).getR_title(), reviews.get(position).getR_desc0(),bitmap);
                        break;
                    case PATTERN_REVIEW_3:
                        Log.d(TAG, "onClick: r_num:"+Integer.parseInt(reviews.get(position).getR_num()));
                        switch (Integer.parseInt(reviews.get(position).getR_num())){
                            case 1:
                                Bitmap[] bit=new Bitmap[1];
                                bitmapDrawable = ((BitmapDrawable) holder.r_image0.getDrawable());
                                bit[0] = bitmapDrawable .getBitmap();
                                ShareMultiImageAndText(reviews.get(position).getR_title(), reviews.get(position).getR_desc0(),  bit);
                                break;

                            case 2:
                                 bit=new Bitmap[2];
                                bit[0] =((BitmapDrawable) holder.r_image0.getDrawable()).getBitmap();
                                bit[1] =((BitmapDrawable) holder.r_image1.getDrawable()).getBitmap();

                                String descrpt=reviews.get(position).getR_desc0()+","+reviews.get(position).getR_desc1();
                                ShareMultiImageAndText(reviews.get(position).getR_title(), descrpt,  bit);
                                break;
                            case 3:
                                bit=new Bitmap[3];
                                bit[0] =((BitmapDrawable) holder.r_image0.getDrawable()).getBitmap();
                                bit[1] =((BitmapDrawable) holder.r_image1.getDrawable()).getBitmap();
                                bit[2] =((BitmapDrawable) holder.r_image2.getDrawable()).getBitmap();

                                 descrpt=reviews.get(position).getR_desc0()+","+reviews.get(position).getR_desc1()+","+reviews.get(position).getR_desc2();
                                ShareMultiImageAndText(reviews.get(position).getR_title(), descrpt,  bit);
                                break;
                            case 4:
                                bit=new Bitmap[4];
                                bit[0] =((BitmapDrawable) holder.r_image0.getDrawable()).getBitmap();
                                bit[1] =((BitmapDrawable) holder.r_image1.getDrawable()).getBitmap();
                                bit[2] =((BitmapDrawable) holder.r_image2.getDrawable()).getBitmap();
                                bit[3] =((BitmapDrawable) holder.r_image3.getDrawable()).getBitmap();

                                descrpt=reviews.get(position).getR_desc0()+","+reviews.get(position).getR_desc1()+","+reviews.get(position).getR_desc2()+","+reviews.get(position).getR_desc3();
                                ShareMultiImageAndText(reviews.get(position).getR_title(), descrpt,  bit);
                                break;
                            default:
                                 bit=new Bitmap[1];
                                bitmapDrawable = ((BitmapDrawable) holder.r_image0.getDrawable());
                                bit[0] = bitmapDrawable .getBitmap();
                                ShareMultiImageAndText(reviews.get(position).getR_title(), reviews.get(position).getR_desc0(),  bit);
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        switch (num) {
            case 0:
                holder.rDesc0.setText(reviews.get(position).getR_desc0());
                break;
            case 1:
                try {
                    Picasso.get().load(reviews.get(position).getR_image0()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image0);
                    holder.rDesc0.setText(reviews.get(position).getR_desc0());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                if(getItemViewType(position)==PATTERN_REVIEW_3){
                    try {
                        Picasso.get().load(reviews.get(position).getR_image0()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image0);
                        holder.rDesc0.setText(reviews.get(position).getR_desc0());

                        Picasso.get().load(reviews.get(position).getR_image1()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image1);
                        holder.rDesc1.setText(reviews.get(position).getR_desc1());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case 3:
                if(getItemViewType(position)==PATTERN_REVIEW_3) {
                    holder.cover02.setVisibility(View.VISIBLE);
                    try {
                        Picasso.get().load(reviews.get(position).getR_image0()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image0);
                        holder.rDesc0.setText(reviews.get(position).getR_desc0());

                        Picasso.get().load(reviews.get(position).getR_image1()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image1);
                        holder.rDesc1.setText(reviews.get(position).getR_desc1());

                        Picasso.get().load(reviews.get(position).getR_image2()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image2);
                        holder.rDesc2.setText(reviews.get(position).getR_desc2());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 4:
                if(getItemViewType(position)==PATTERN_REVIEW_3) {
                    holder.cover02.setVisibility(View.VISIBLE);
                    holder.cover03.setVisibility(View.VISIBLE);
                    try {
                        Picasso.get().load(reviews.get(position).getR_image0()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image0);
                        holder.rDesc0.setText(reviews.get(position).getR_desc0());

                        Picasso.get().load(reviews.get(position).getR_image1()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image1);
                        holder.rDesc1.setText(reviews.get(position).getR_desc1());

                        Picasso.get().load(reviews.get(position).getR_image2()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image2);
                        holder.rDesc2.setText(reviews.get(position).getR_desc2());

                        Picasso.get().load(reviews.get(position).getR_image3()).resize(max_w, max_h) .onlyScaleDown().into(holder.r_image3);
                        holder.rDesc3.setText(reviews.get(position).getR_desc3());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                holder.rDesc0.setText(reviews.get(position).getR_desc0());
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToReviewDetail(position);
            }
        });

        if(getItemViewType(position)==PATTERN_REVIEW_3){

            holder.cover00.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReviewDetail(position);
                }
            });
            holder.cover01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReviewDetail(position);
                }
            });

            holder.cover02.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReviewDetail(position);
                }
            });

            holder.cover03.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReviewDetail(position);
                }
            });
        }

        holder.score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_review_score, null, false);

                checkPaticipation(reviews.get(position).getrId(), myUid, view);
                builder.setView(view);

                AlertDialog show = builder.show();

                ImageView star1 = view.findViewById(R.id.star1);
                ImageView star2 = view.findViewById(R.id.star2);
                ImageView star3 = view.findViewById(R.id.star3);
                ImageView star4 = view.findViewById(R.id.star4);
                ImageView star5 = view.findViewById(R.id.star5);


                star1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        star = 1;
                        checkStatusStar(view);
                    }
                });
                star2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        star = 2;
                        checkStatusStar(view);
                    }
                });
                star3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        star = 3;
                        checkStatusStar(view);
                    }
                });
                star4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        star = 4;
                        checkStatusStar(view);
                    }
                });
                star5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        star = 5;
                        checkStatusStar(view);
                    }
                });

                Button btnSend = view.findViewById(R.id.btnSend);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RParticipations").child(reviews.get(position).getrId());
                        ref.child(myUid).setValue(String.valueOf(star));

                        calculateScore(reviews.get(position), star, holder);

                        holder.starScore.setImageResource(R.drawable.ic_star_primary);

                        show.dismiss();
                    }
                });

            }
        });
    }
        private void loadLocation(ModelReview review, Myholder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reviews").child(review.getrId()).child("Mylocation");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try{
                    mylocation = snapshot.getValue(ModelMylocation.class);
                    if(mylocation!=null){
                        holder.showAddress.setVisibility(View.VISIBLE);

                        String title=mylocation.getMap_title();
                        if(title.equals("My Location")){
                            title=review.getuName()+" Location";
                        }
                        holder.txtMapTitle .setText(title);
                        String add=mylocation.getAddress();
                        String address[] = add.split(",");
                        holder.txtShowAddress.setText(address[address.length-2]+","+address[address.length-1]);

                    }else{
                        holder.txtShowAddress.setText("hello");
                        holder.showAddress.setVisibility(View.GONE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ShareImageAndText(String pTitle, String pDesc, Bitmap bitmap) {
        //concatenate title and description to share
        String sharebody = pTitle + "\n" + pDesc;

        //first we will save this image in catche,get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, sharebody);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.setType("image/png");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();//create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.wongwien.fileprovider", file);
            Log.d(TAG, "saveImageToShare: uri::"+uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }
    private void ShareMultiImageAndText(String pTitle, String pDesc, Bitmap[] bitmap) {
        //concatenate title and description to share
        String sharebody = pTitle + "\n" + pDesc;

        //first we will save this image in catche,get the saved image uri
       saveMultiImageToShare(bitmap);
       if(imageUriArray!=null){
           Log.d(TAG, "ShareMultiImageAndText: ImageUriArray:"+imageUriArray.toString());
           //share intent
           Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
           intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUriArray);
           intent.putExtra(Intent.EXTRA_TEXT, sharebody);
           intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
           intent.setType("image/png");
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
           context.startActivity(Intent.createChooser(intent, "Share Via"));
       }

    }

    private void saveMultiImageToShare(Bitmap[] bitmap) {
        imageUriArray=new ArrayList<>();
        File imageFolder = new File(context.getCacheDir(), "images");

        for(int i=0;i<bitmap.length;i++){
            Date now = new Date();
            Uri uri = null;
            try {
                imageFolder.mkdirs();//create if not exists
                File file = new File(imageFolder, "shared_image_"+now+".png");

                FileOutputStream stream = new FileOutputStream(file);
                bitmap[i].compress(Bitmap.CompressFormat.PNG, 90, stream);
                stream.flush();
                stream.close();
                uri = FileProvider.getUriForFile(context, "com.example.wongwien.fileprovider", file);
                imageUriArray.add(uri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void shareTextOnly(String pTitle, String pDesc) {
        //concatenate title and description to share
        String sharebody = pTitle + "\n" + pDesc;

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT, sharebody);//text to share
        context.startActivity(Intent.createChooser(intent, "Share Via"));//message to show in share
    }
    private void goToReviewDetail(int position){
        Intent intent = new Intent(context, ReviewDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("list", reviews.get(position));

        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    private void calculateAvgScore(ModelReview review,Myholder holder){
        ArrayList<Double>score=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RParticipations");
        Query q=ref.child(review.getrId());
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshort:snapshot.getChildren()){
                    String s=snapshort.getValue(String.class);
                    score.add(Double.parseDouble(s));
                }

                double sum=0;
                for(int i=0;i<score.size();i++){
                    sum+=score.get(i);
                }
                avgscore= sum/score.size();
                Log.d(TAG, "onDataChange: value::"+score+" avg:"+avgscore);
                if(avgscore!=0){
                    point=avgscore;
                }
                double point2 = round(point,2);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reviews");
                Query q = ref.orderByChild("rId").equalTo(review.getrId());
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot d : snapshot.getChildren()) {
                            ModelReview model = d.getValue(ModelReview.class);
                            if (model.getrId().equals(review.getrId())) {
                                d.getRef().child("r_point").setValue(point2);
                                changeShowTextScore(holder, point2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void calculateScore(ModelReview review, int star, Myholder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reviews");
        Query q = ref.orderByChild("rId").equalTo(review.getrId());
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    ModelReview model = d.getValue(ModelReview.class);
                    point =model.getR_point();

                    calculateAvgScore(review,holder);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public  double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void changeShowTextScore(Myholder holder, double point) {
        holder.txtPoint.setText(String.valueOf(point));
    }

    private void checkStatusStar(View view) {
        ImageView star1 = view.findViewById(R.id.star1);
        ImageView star2 = view.findViewById(R.id.star2);
        ImageView star3 = view.findViewById(R.id.star3);
        ImageView star4 = view.findViewById(R.id.star4);
        ImageView star5 = view.findViewById(R.id.star5);

        switch (star) {
            case 1:
                star1.setImageResource(R.drawable.ic_star_white);
                star2.setImageResource(R.drawable.ic_star_empty);
                star3.setImageResource(R.drawable.ic_star_empty);
                star4.setImageResource(R.drawable.ic_star_empty);
                star5.setImageResource(R.drawable.ic_star_empty);
                break;
            case 2:
                star1.setImageResource(R.drawable.ic_star_white);
                star2.setImageResource(R.drawable.ic_star_white);
                star3.setImageResource(R.drawable.ic_star_empty);
                star4.setImageResource(R.drawable.ic_star_empty);
                star5.setImageResource(R.drawable.ic_star_empty);
                break;
            case 3:
                star1.setImageResource(R.drawable.ic_star_white);
                star2.setImageResource(R.drawable.ic_star_white);
                star3.setImageResource(R.drawable.ic_star_white);
                star4.setImageResource(R.drawable.ic_star_empty);
                star5.setImageResource(R.drawable.ic_star_empty);
                break;
            case 4:
                star1.setImageResource(R.drawable.ic_star_white);
                star2.setImageResource(R.drawable.ic_star_white);
                star3.setImageResource(R.drawable.ic_star_white);
                star4.setImageResource(R.drawable.ic_star_white);
                star5.setImageResource(R.drawable.ic_star_empty);
                break;
            case 5:
                star1.setImageResource(R.drawable.ic_star_white);
                star2.setImageResource(R.drawable.ic_star_white);
                star3.setImageResource(R.drawable.ic_star_white);
                star4.setImageResource(R.drawable.ic_star_white);
                star5.setImageResource(R.drawable.ic_star_white);
                break;
            default:
                star1.setImageResource(R.drawable.ic_star_empty);
                star2.setImageResource(R.drawable.ic_star_empty);
                star3.setImageResource(R.drawable.ic_star_empty);
                star4.setImageResource(R.drawable.ic_star_empty);
                star5.setImageResource(R.drawable.ic_star_empty);
                break;
        }
    }

    private void loadAllStarToReviews(String rId, String myUid, Myholder holder) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RParticipations").child(rId).child(myUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String num = snapshot.getValue(String.class);
                    if (num != null) {
                        star = Integer.parseInt(num);
                        holder.starScore.setImageResource(R.drawable.ic_star_primary);
                    } else {
                        star = 0;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: notfound");
                }
            });
        } catch (Exception e) {
            star = 0;
        }
    }

    private void checkPaticipation(String rId, String myUid, View view) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RParticipations").child(rId).child(myUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String num = snapshot.getValue(String.class);
                    if (num != null) {
                        isUseReview = true;
                        starBeforeChange = Integer.parseInt(num);
                        star = Integer.parseInt(num);
                        checkStatusStar(view);
                        Log.d(TAG, "onDataChange::" + rId + ":: star:::" + star);
                    } else {
                        star = 0;
                        isUseReview = false;
                        Log.d(TAG, "onDataChange::" + rId + ":: star:::" + star);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: notfound");
                }
            });
        } catch (Exception e) {
            star = 0;
            isUseReview = false;
            starBeforeChange = 0;
            checkStatusStar(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (reviews.get(position).getR_type()) {
            case "pattern1":
                return PATTERN_REVIEW_1;
            case "pattern2":
                return PATTERN_REVIEW_2;
            case "pattern3":
                return PATTERN_REVIEW_3;
            default:
                return PATTERN_REVIEW_1;
        }
    }

    private void checkUserStatus() {
        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
        }
    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        private TextView rTitile, rDesc0, rDesc1, rDesc2, rDesc3, txtPoint,txtShowAddress,txtMapTitle;
        private ImageView r_image0, r_image1, r_image2, r_image3, starScore;
        private LinearLayout score,showAddress,btnShare;
        private RelativeLayout cover00, cover01, cover02, cover03;
        private HorizontalScrollView hsv;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            rTitile = itemView.findViewById(R.id.r_title);
            txtPoint = itemView.findViewById(R.id.txtPoint);
            score = itemView.findViewById(R.id.score);
            starScore = itemView.findViewById(R.id.starScore);
            rDesc0 = itemView.findViewById(R.id.r_desc0);
            txtShowAddress=itemView.findViewById(R.id.txtShowAddress);
            showAddress=itemView.findViewById(R.id.showAddress);
            txtMapTitle=itemView.findViewById(R.id.txtMapTitle);
            btnShare=itemView.findViewById(R.id.btnShare);

            try{
                rDesc0 = itemView.findViewById(R.id.r_desc0);
                r_image0 = itemView.findViewById(R.id.r_image0);
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                hsv = itemView.findViewById(R.id.hsv);

                cover00 = itemView.findViewById(R.id.cover00);
                cover01 = itemView.findViewById(R.id.cover01);
                cover02 = itemView.findViewById(R.id.cover02);
                cover03 = itemView.findViewById(R.id.cover03);

                rDesc0 = itemView.findViewById(R.id.r_desc0);
                rDesc1 = itemView.findViewById(R.id.r_desc1);
                rDesc2 = itemView.findViewById(R.id.r_desc2);
                rDesc3 = itemView.findViewById(R.id.r_desc3);
                r_image0 = itemView.findViewById(R.id.r_image0);
                r_image1 = itemView.findViewById(R.id.r_image1);
                r_image2 = itemView.findViewById(R.id.r_image2);
                r_image3 = itemView.findViewById(R.id.r_image3);
                hsv = itemView.findViewById(R.id.hsv);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
