package com.example.ol.assignment2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.adapter.RecyclerViewReviewsAdapter;
import com.example.ol.assignment2.model.Book;
import com.example.ol.assignment2.model.Review;
import com.example.ol.assignment2.model.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Book_Activity extends AppCompatActivity {

    private TextView tvTitle, tvAuthor, tvGenre, tvPages, tvDownload,tvYear;
    private ImageView ivBookCover;
    private Button btnBuy, btnReview, btnDownload;
    private ImageView ivClose;
    private TextView tvBookName, tvTotalRatingTxt;
    private EditText etTextReview;
    private Button btnSubmitReview;
    private RatingBar ratingBarBook, ratingReviewBar;
    private ArrayList<Review> lstReview;
    private FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    private User user;
    private Book theBook;
    ArrayList<Book> lstBook; // Because we need to transfer to next activity.
    private double totalRatingReviews = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        String activityFrom = null;
        Intent intent = getIntent();
        theBook = (Book) intent.getSerializableExtra("choseBook");
        user = (User) intent.getSerializableExtra("user");
        lstBook = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");
        activityFrom = intent.getStringExtra("activityFrom");

        BottomNavigationView bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu=bottomNav.getMenu();
        MenuItem menuItem=menu.getItem(checkActivityFrom(activityFrom));
        menuItem.setChecked(true);





        tvTitle = findViewById(R.id.txtTitle);
        tvAuthor = findViewById(R.id.txtAuthor);
        tvGenre = findViewById(R.id.txtGenre);
        tvPages = findViewById(R.id.txtPages);
        tvDownload = findViewById(R.id.txtDownload);
        tvYear = findViewById(R.id.txtYear);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnBuy = findViewById(R.id.btnBuy);
        btnReview = findViewById(R.id.btnReview);
        btnDownload = findViewById(R.id.btnDownload);
        ratingBarBook = findViewById(R.id.ratingBarBook);
        tvTotalRatingTxt = findViewById(R.id.totalRatingTxt);

        final int idBook = theBook.getId();
        final String Title = theBook.getTitle();
        String Author = theBook.getAuthor();
        String Genre = theBook.getGenre();
        int Pages = theBook.getPages();
        int Year = theBook.getYear();
        int Downloads = theBook.getDownloads();
        String Image = theBook.getImg();
        double Rating = theBook.getRating();
        double Price = theBook.getPrice();
        final String pdf = theBook.getPdf();

        ratingBarBook.setRating(Float.parseFloat(Double.toString(theBook.getRating())));
        tvTitle.setText(Title);
        tvAuthor.setText(Author);
        tvGenre.setText(Genre);
        tvPages.setText(Integer.toString(Pages) + " Pages");
        tvYear.setText(Integer.toString(Year));
        tvDownload.setText(Integer.toString(Downloads));
        Uri myUri = Uri.parse(Image);
        Picasso.with(Book_Activity.this).load(myUri).into(ivBookCover);
        ivBookCover.setScaleType(ImageView.ScaleType.FIT_XY);


        boolean Bought = checkIfBought();
        buttonsSetChecker(Bought, Price);
        getReviews();

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf));
                startActivity(browserIntent);
            }
        });

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getEmail().equals("guest")) {
                    signOutFirebase();
                    user = null;
                    Intent intent = new Intent(Book_Activity.this, SignIn.class);
                    startActivity(intent);
                } else {
                    if (user.getMyBooks() == null) {
                        List<Integer> myBooksID = new ArrayList<>();
                        myBooksID.add(theBook.getId());
                        user.setMyBooks(myBooksID);
                    } else {
                        user.getMyBooks().add(theBook.getId());
                    }
                    user.setTotalPurchase(user.getTotalPurchase() + 1);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                    btnReview.setVisibility(View.VISIBLE);
                    buttonsSetChecker(true, theBook.getPrice());
                }
            }
        });

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeReview(Title);
            }

        });
    }

    private void initRecyclerView() {
        RecyclerView rvReviewList = (RecyclerView) findViewById(R.id.rvReviewList);
        rvReviewList.setLayoutManager(new LinearLayoutManager(this));
        Context context = rvReviewList.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down);
        RecyclerViewReviewsAdapter myAdapter = new RecyclerViewReviewsAdapter(this, lstReview);
        rvReviewList.setAdapter(myAdapter);

        rvReviewList.setLayoutAnimation(controller);
        rvReviewList.getAdapter().notifyDataSetChanged();
        rvReviewList.scheduleLayoutAnimation();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(Book_Activity.this, Orders_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(Book_Activity.this, AllBooks_Activity.class);
                            intent.putExtra("user", user);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(Book_Activity.this, Search_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(Book_Activity.this, SignIn.class);
                            break;
                        case R.id.nav_home:
                            intent= new Intent(Book_Activity.this,BookLibrary.class);
                            break;
                    }

                    startActivity(intent);
                    return true;
                }
            };

    private boolean checkIfBought() {
        boolean isBought = false;
        if (user.getMyBooks() != null) {
            List<Integer> theBooksIDList = user.getMyBooks();
            for (Integer bookNum : theBooksIDList) {
                if (bookNum == theBook.getId()) {
                    isBought = true;
                    break;
                }
            }
        }
        return isBought;
    }

    private void buttonsSetChecker(boolean Bought, double Price) {
        if (Bought) {
            //change icon to download
            btnDownload.setText("Download");
            btnDownload.setVisibility(View.VISIBLE);
            btnBuy.setVisibility(View.GONE);
            } else {
            btnBuy.setText("Buy it for " + Double.toString(Price) + "$");
            btnBuy.setVisibility(View.VISIBLE);
            btnReview.setVisibility(View.GONE);
        }
    }

    public void getReviews() {

        lstReview = new ArrayList<>();
        //lstReview.add(new Review("Elad Ben-Moshe", 1, "12-05-2017", "Geate Book!, Excellent wrtiting", 4.5));

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(theBook.getId()));
        DatabaseReference mReviewsBookDatabase = mDatabase.child("Reviews");
        mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lstReview.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Review currentReview = ds.getValue(Review.class);
                    if(currentReview.getName().equals(user.getEmail())) {
                        btnReview.setVisibility(View.GONE);
                    }
                    lstReview.add(currentReview);
                    totalRatingReviews += currentReview.getScoreReview();
                }


                updateReviewsTotal(lstReview.size(), theBook.getRating());
                initRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateReviewsTotal(int reviewsCounter, double currentRating)
    {
        ratingBarBook.setRating(Float.parseFloat(Double.toString(currentRating)));
        tvTotalRatingTxt.setText("(" +Integer.toString(reviewsCounter)+")");
    }

    private void writeReview(String Title) {
        final Dialog dialog = new Dialog(Book_Activity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_review);

        ivClose = (ImageView) dialog.findViewById(R.id.ivCloseReview);
        tvBookName = (TextView) dialog.findViewById(R.id.tvBookNameReview);
        etTextReview = (EditText) dialog.findViewById(R.id.etTextReview);
        btnSubmitReview = (Button) dialog.findViewById(R.id.btnSubmitReview);
        ratingReviewBar = (RatingBar) dialog.findViewById(R.id.ratingBarReview);
        tvBookName.setText(Title);

        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Review newReview = new Review(user.getEmail(), fbUser.getUid(), getDateWithoutTimeUsingCalendar(), etTextReview.getText().toString(), ratingReviewBar.getRating());
                lstReview.add(newReview);
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(theBook.getId()));
                final DatabaseReference mReviewsBookDatabase = mDatabase.child("Reviews");
                mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReviewsBookDatabase.setValue(lstReview);
                        btnReview.setVisibility(View.GONE);
                        updateTheRatingOnDatabase(ratingReviewBar.getRating());
                        Toast.makeText(Book_Activity.this, "Review wrote successfully!", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public static String getDateWithoutTimeUsingCalendar() {
        String modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        return modifiedDate;
    }

    private void updateTheRatingOnDatabase(float newReviewRating)
    {
        totalRatingReviews += newReviewRating;
        DecimalFormat formatAverage = new DecimalFormat("#.#");
        final double average = Double.valueOf(formatAverage.format(totalRatingReviews / lstReview.size()));
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(theBook.getId()));
        final DatabaseReference mReviewsBookDatabase = mDatabase.child("rating");
        mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mReviewsBookDatabase.setValue(average);
                updateReviewsTotal(lstReview.size(), average);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    private int checkActivityFrom(String i_ActivityFromString)
    {
        int numOfIcon = 0;
     if(i_ActivityFromString.equals("BookLibrary"))
         numOfIcon = 4;
     else
         if(i_ActivityFromString.equals("Search_Activity"))
             numOfIcon = 3;
             else
                 if(i_ActivityFromString.equals("AllBooks_Activity"))
                     numOfIcon =2;
             else
                 if(i_ActivityFromString.equals("Orders_Activity"))
                     numOfIcon =1;


        return numOfIcon;
    }

}



