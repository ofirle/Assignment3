package com.example.ol.assignment2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookActivity extends AppCompatActivity {
    private String classStringName = "BookActivity";
    private TextView tvTitle, tvAuthor, tvGenre, tvPages, tvDownload, tvYear, tvBetheFirst;
    private ImageView ivBookCover;
    private Button btnBuy, btnReview, btnDownload;
    private ImageView ivClose;
    private TextView tvBookName, tvTotalRatingTxt;
    private EditText etTextReview;
    private Button btnSubmitReview;
    private RatingBar ratingBarBook, ratingReviewBar;

    private Typeface m_MyFont;
    private ArrayList<Review> m_ListReview;
    private FirebaseUser m_FbUser = FirebaseAuth.getInstance().getCurrentUser();
    private User m_User;
    private Book m_TheBook;
    private String m_ActivityFrom = null;
    private ArrayList<Book> m_ListBook; // Because we need to transfer to next activity.
    private double m_TotalRatingReviews = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        //getPutExtras();
        Intent intent = getIntent();
        m_TheBook = (Book) intent.getSerializableExtra("choseBook");
        m_User = (User) intent.getSerializableExtra("m_User");
        m_ListBook = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");
        m_MyFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        m_ActivityFrom = intent.getStringExtra("activityFrom");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(checkActivityFrom(m_ActivityFrom));
        menuItem.setChecked(true);
        initUI();
        final int idBook = m_TheBook.getId();
        final String Title = m_TheBook.getTitle();
        String Author = m_TheBook.getAuthor();
        String Genre = m_TheBook.getGenre();
        int Pages = m_TheBook.getPages();
        int Year = m_TheBook.getYear();
        int Downloads = m_TheBook.getDownloads();
        String Image = m_TheBook.getImg();
        double Rating = m_TheBook.getRating();
        double Price = m_TheBook.getPrice();
        final String pdf = m_TheBook.getPdf();
        ratingBarBook.setRating(Float.parseFloat(Double.toString(m_TheBook.getRating())));
        tvTitle.setText(Title);
        tvAuthor.setText(Author);
        tvGenre.setText(Genre);
        tvPages.setText(Integer.toString(Pages) + " Pages");
        tvYear.setText(Integer.toString(Year));
        tvDownload.setText(Integer.toString(Downloads));
        Uri myUri = Uri.parse(Image);
        Picasso.with(BookActivity.this).load(myUri).into(ivBookCover);
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
                if (m_User.getEmail().equals("guest")) {
                   signOutGuest();
                } else {
                   userBuyBook();
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
        RecyclerViewReviewsAdapter myAdapter = new RecyclerViewReviewsAdapter(this, m_ListReview, m_MyFont);
        rvReviewList.setAdapter(myAdapter);

        rvReviewList.setLayoutAnimation(controller);
        rvReviewList.getAdapter().notifyDataSetChanged();
        rvReviewList.scheduleLayoutAnimation();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(BookActivity.this, OrdersActivity.class);
                            intent.putExtra("m_User", m_User);
                            intent.putExtra("booksList", m_ListBook);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(BookActivity.this, AllBooksActivity.class);
                            intent.putExtra("m_User", m_User);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(BookActivity.this, SearchActivity.class);
                            intent.putExtra("m_User", m_User);
                            intent.putExtra("booksList", m_ListBook);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(BookActivity.this, SignInActivity.class);
                            break;
                        case R.id.nav_home:
                            intent = new Intent(BookActivity.this, BookLibraryActivity.class);
                            break;
                    }

                    startActivity(intent);
                    return true;
                }
            };


    private boolean checkIfBought() {
        boolean isBought = false;
        if (m_User.getMyBooks() != null) {
            List<Integer> theBooksIDList = m_User.getMyBooks();
            for (Integer bookNum : theBooksIDList) {
                if (bookNum == m_TheBook.getId()) {
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

        m_ListReview = new ArrayList<>();
        //m_ListReview.add(new Review("Elad Ben-Moshe", 1, "12-05-2017", "Geate Book!, Excellent wrtiting", 4.5));

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(m_TheBook.getId()));
        DatabaseReference mReviewsBookDatabase = mDatabase.child("Reviews");
        mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m_ListReview.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Review currentReview = ds.getValue(Review.class);
                    if (currentReview.getName().equals(m_User.getEmail())) {
                        btnReview.setVisibility(View.GONE);
                    }
                    m_ListReview.add(currentReview);
                    m_TotalRatingReviews += currentReview.getScoreReview();
                }

                if (m_ListReview.size() == 0 && btnReview.getVisibility() == View.VISIBLE) {
                    tvBetheFirst.setVisibility(View.VISIBLE);
                }
                updateReviewsTotal(m_ListReview.size(), m_TheBook.getRating());
                initRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateReviewsTotal(int reviewsCounter, double currentRating) {
        ratingBarBook.setRating(Float.parseFloat(Double.toString(currentRating)));
        tvTotalRatingTxt.setText("(" + Integer.toString(reviewsCounter) + ")");
    }

    private void writeReview(String Title) {
        final Dialog dialog = new Dialog(BookActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_review);

        ivClose = (ImageView) dialog.findViewById(R.id.ivCloseReview);
        tvBookName = (TextView) dialog.findViewById(R.id.tvBookNameReview);
        etTextReview = (EditText) dialog.findViewById(R.id.etTextReview);
        btnSubmitReview = (Button) dialog.findViewById(R.id.btnSubmitReview);
        ratingReviewBar = (RatingBar) dialog.findViewById(R.id.ratingBarReview);
        tvBookName.setText(Title);
        tvBookName.setTypeface(m_MyFont);
        etTextReview.setTypeface(m_MyFont);
        btnSubmitReview.setTypeface(m_MyFont);

        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Review newReview = new Review(m_User.getEmail(), m_FbUser.getUid(), getDateWithoutTimeUsingCalendar(), etTextReview.getText().toString(), ratingReviewBar.getRating());
                m_ListReview.add(newReview);
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(m_TheBook.getId()));
                final DatabaseReference mReviewsBookDatabase = mDatabase.child("Reviews");
                mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReviewsBookDatabase.setValue(m_ListReview);
                        btnReview.setVisibility(View.GONE);
                        updateTheRatingOnDatabase(ratingReviewBar.getRating());
                        Toast toast = Toast.makeText(BookActivity.this, "Thank You for Your Review!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                        tvBetheFirst.setVisibility(View.INVISIBLE);
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
        String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        return modifiedDate;
    }

    private void updateTheRatingOnDatabase(float newReviewRating) {
        m_TotalRatingReviews += newReviewRating;
        DecimalFormat formatAverage = new DecimalFormat("#.#");
        final double average = Double.valueOf(formatAverage.format(m_TotalRatingReviews / m_ListReview.size()));
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(m_TheBook.getId()));
        final DatabaseReference mReviewsBookDatabase = mDatabase.child("rating");
        mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mReviewsBookDatabase.setValue(average);
                updateReviewsTotal(m_ListReview.size(), average);
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

    private int checkActivityFrom(String i_ActivityFromString) {
        int numOfIcon = 0;
        if (i_ActivityFromString.equals("SearchActivity"))
            numOfIcon = 3;
        else if (i_ActivityFromString.equals("AllBooksActivity"))
            numOfIcon = 2;
        else if (i_ActivityFromString.equals("OrdersActivity"))
            numOfIcon = 1;
        else
            numOfIcon = 4;


        return numOfIcon;
    }

    private void initUI() {
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
        tvBetheFirst = findViewById(R.id.BeFirstToReview);
        tvTitle.setTypeface(m_MyFont);
        tvAuthor.setTypeface(m_MyFont);
        tvGenre.setTypeface(m_MyFont);
        tvPages.setTypeface(m_MyFont);
        tvDownload.setTypeface(m_MyFont);
        tvYear.setTypeface(m_MyFont);
        btnBuy.setTypeface(m_MyFont);
        btnReview.setTypeface(m_MyFont);
        btnDownload.setTypeface(m_MyFont);
        tvTotalRatingTxt.setTypeface(m_MyFont);
        tvBetheFirst.setTypeface(m_MyFont);
        tvBetheFirst.setVisibility(View.INVISIBLE);
    }

    private void getPutExtras() {


    }

    private void initBookDisplay()
    {

    }

    private void signOutGuest()
    {
        signOutFirebase();
        m_User = null;
        Intent intent = new Intent(BookActivity.this, SignInActivity.class);
        intent.putExtra("activityFrom", classStringName);
        intent.putExtra("choseBook", m_TheBook);
        intent.putExtra("booksList", m_ListBook);
        Toast.makeText(BookActivity.this, "You must sign in before purchase.", Toast.LENGTH_LONG);
        startActivity(intent);
    }

    private void userBuyBook()
    {
        if (m_User.getEmail().equals("guest")) {
            signOutFirebase();
            m_User = null;
            Intent intent = new Intent(BookActivity.this, SignInActivity.class);
            intent.putExtra("activityFrom", classStringName);
            intent.putExtra("choseBook", m_TheBook);
            intent.putExtra("booksList", m_ListBook);
            Toast.makeText(BookActivity.this, "You must sign in before purchase.", Toast.LENGTH_LONG);
            startActivity(intent);
        } else {
            if (m_User.getMyBooks() == null) {
                List<Integer> myBooksID = new ArrayList<>();
                myBooksID.add(m_TheBook.getId());
                m_User.setMyBooks(myBooksID);
            } else {
                m_User.getMyBooks().add(m_TheBook.getId());
            }
            m_User.setTotalPurchase( m_User.getTotalPurchase() + 1);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(m_User);
            DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(Integer.toString(m_TheBook.getId()));
            m_TheBook.setDownloads(m_TheBook.getDownloads()+1);
            bookRef.child("downloads").setValue(m_TheBook.getDownloads());
            tvDownload.setText(Integer.toString(m_TheBook.getDownloads()));
            Toast toast=Toast.makeText(BookActivity.this, "Successfully Purchased", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
            toast.show();
            btnReview.setVisibility(View.VISIBLE);
            if(m_ListReview.size()==0)
            {
                tvBetheFirst.setVisibility(View.VISIBLE);
            }
            buttonsSetChecker(true, m_TheBook.getPrice());
        }
    }
}



