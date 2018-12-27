package com.example.ol.assignment2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.adapter.RecyclerViewAdapter;
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

import java.util.ArrayList;

public class AllBooks_Activity extends AppCompatActivity{

    private String classStringName = "AllBooks_Activity";
    private ArrayList<SortItem>mItemList;
    private SortAdapter mAdapter;
    ArrayList<Book> lstBook=new ArrayList<Book>();
    ArrayList<Book> lstSortedBook=new ArrayList<Book>();
    Spinner sortSpinner;
    ImageView ivFilter;
    private ImageView ivClose;
    private SeekBar sbPrice;
    ImageView upArrow,downArrow;
    TextView txtMinReviews,txtMaxPriceSelectedFilter;
    Button btnSubmitFilter;
    int numOfMinDownloads=0;
    double maxPriceFilter;


    private SortArrayListFields salf = new SortArrayListFields();

    private FirebaseUser fbUser;
    private User user;
    private DatabaseReference myUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_books);

        BottomNavigationView bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu=bottomNav.getMenu();
        MenuItem menuItem=menu.getItem(2);
        menuItem.setChecked(true);

        sortSpinner = (Spinner) findViewById(R.id.spinner_sort);
        ivFilter= (ImageView) findViewById(R.id.ivFilter);

        initSpinnerList();

        mAdapter = new SortAdapter(this,mItemList);
        sortSpinner.setAdapter(mAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 1: lstSortedBook= salf.sortPriceLowToHigh(lstBook);
                        break;
                    case 2: lstSortedBook=salf.sortRating(lstBook);
                        break;
                    case 3: lstSortedBook=salf.sortBookNameLibrary(lstBook);
                        break;
                }
                initRecyclerView(lstSortedBook);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Intent intent = new Intent(AllBooks_Activity.this, SignIn.class);
            startActivity(intent);
        } else {
            myUserRef = FirebaseDatabase.getInstance().getReference("Users/" + fbUser.getUid());
            myUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    readingDataFromDatabase();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterBooks();
            }
        });
    }

    private void initSpinnerList() {
        mItemList=new ArrayList<>();
        mItemList.add(new SortItem(R.drawable.ic_sort_black_24dp));
        mItemList.add(new SortItem(R.drawable.ic_attach_money_black_24dp));
        mItemList.add(new SortItem(R.drawable.ic_star_black_24dp));
        mItemList.add(new SortItem(R.drawable.ic_sort_by_alpha_black_24dp));
    }


    private void initRecyclerView(ArrayList<Book> lst) {

        RecyclerView rvBookList = (RecyclerView) findViewById(R.id.rv_book_list);
        rvBookList.setLayoutManager(new GridLayoutManager(this, 3));
        Context context = rvBookList.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_slide_from_left);
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, user, classStringName);
        rvBookList.setAdapter(myAdapter);
        rvBookList.setLayoutAnimation(controller);
        rvBookList.getAdapter().notifyDataSetChanged();
        rvBookList.scheduleLayoutAnimation();

    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(AllBooks_Activity.this, Orders_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            break;
                        case R.id.nav_search:
                            intent = new Intent(AllBooks_Activity.this, Search_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(AllBooks_Activity.this, SignIn.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent= new Intent(AllBooks_Activity.this,BookLibrary.class);
                            startActivity(intent);
                            break;
                    }
                    return true;
                }
            };
    private void readingDataFromDatabase() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Books");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lstBook.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Book newBook = ds.getValue(Book.class);
                    lstBook.add(newBook);
                }
                initRecyclerView(lstBook);
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

    private void FilterBooks() {
        final Dialog dialog = new Dialog(AllBooks_Activity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_books);


        ivClose = (ImageView) dialog.findViewById(R.id.ivCloseFilter);
        sbPrice = (SeekBar) dialog.findViewById(R.id.sbPrice);
        upArrow = (ImageView) dialog.findViewById(R.id.ivArrowUp);
        downArrow = (ImageView) dialog.findViewById(R.id.ivArrowDown);
        txtMinReviews = (TextView) dialog.findViewById(R.id.txtMinReviews);
        txtMaxPriceSelectedFilter= (TextView) dialog.findViewById(R.id.txtMaxPriceSelectedFilter);
        btnSubmitFilter = (Button) dialog.findViewById(R.id.btnSubmitFilter);



        upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numOfMinDownloads = numOfMinDownloads + 1000;
                txtMinReviews.setText(Integer.toString(numOfMinDownloads));
            }
        });
        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numOfMinDownloads>=1000) {
                    numOfMinDownloads = numOfMinDownloads - 1000;
                    txtMinReviews.setText(Integer.toString(numOfMinDownloads));
                }
            }
        });

        sbPrice.setProgress(100);
        sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxPriceFilter=progress/10.0;
                txtMaxPriceSelectedFilter.setText(Double.toString(maxPriceFilter)+'$');
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txtMaxPriceSelectedFilter.setText(Double.toString(maxPriceFilter)+'$');
            }
        });

        btnSubmitFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstSortedBook.clear();
                for (Book book : lstBook)
                {//add numOfReviews
                    if(book.getPrice()<=maxPriceFilter && book.getDownloads() >= numOfMinDownloads)
                        lstSortedBook.add(book);
                }
                dialog.dismiss();
                initRecyclerView(lstSortedBook);
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

}
