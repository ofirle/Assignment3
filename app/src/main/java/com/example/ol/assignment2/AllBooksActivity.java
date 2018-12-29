package com.example.ol.assignment2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.adapter.RecyclerViewAdapter;
import com.example.ol.assignment2.model.Book;
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

public class AllBooksActivity extends AppCompatActivity {
    private String classStringName = "AllBooksActivity";
    private ArrayList<SortItem> mItemList;
    private SortAdapter mAdapter;
    private Spinner sortSpinner;
    private ImageView ivFilter;
    private ImageView ivClose;
    private SeekBar sbPrice;
    private ImageView upArrow, downArrow;
    private TextView txtMinReviews, txtMaxPriceSelectedFilter;
    private TextView txtDownloads, txtPrice;
    private Button btnSubmitFilter;

    private int m_NumOfMinDownloads = 0;
    private double m_MaxPriceFilter;
    private Typeface m_MyFont;
    private ArrayList<Book> m_ListBooks = new ArrayList<Book>();
    private ArrayList<Book> m_ListSortedBook = new ArrayList<Book>();
    private SortArrayListFields m_Salf = new SortArrayListFields();
    private FirebaseUser m_FbUser;
    private User m_User;
    private DatabaseReference m_MyUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_books);
        initUI();
        initSpinnerList();
        mAdapter = new SortAdapter(this, mItemList);
        sortSpinner.setAdapter(mAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkPosition(position);
                initRecyclerView(m_ListSortedBook);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    checkUser();
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterBooks();
            }
        });
    }

    private void initSpinnerList() {
        mItemList = new ArrayList<>();
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
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, m_User, classStringName, m_MyFont, m_ListBooks);
        rvBookList.setAdapter(myAdapter);
        rvBookList.setLayoutAnimation(controller);
        rvBookList.getAdapter().notifyDataSetChanged();
        rvBookList.scheduleLayoutAnimation();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(AllBooksActivity.this, OrdersActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBooks);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            break;
                        case R.id.nav_search:
                            intent = new Intent(AllBooksActivity.this, SearchActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBooks);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(AllBooksActivity.this, SignInActivity.class);
                            Toast.makeText(AllBooksActivity.this, "Sign out success.", Toast.LENGTH_LONG).show();
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent = new Intent(AllBooksActivity.this, BookLibraryActivity.class);
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
                m_ListBooks.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Book newBook = ds.getValue(Book.class);
                    m_ListBooks.add(newBook);
                }
                initRecyclerView(m_ListBooks);
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
        final Dialog dialog = new Dialog(AllBooksActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_books);


        txtDownloads = (TextView) dialog.findViewById(R.id.txtDownloads);
        txtPrice = (TextView) dialog.findViewById(R.id.txtPrice);
        ivClose = (ImageView) dialog.findViewById(R.id.ivCloseFilter);
        sbPrice = (SeekBar) dialog.findViewById(R.id.sbPrice);
        upArrow = (ImageView) dialog.findViewById(R.id.ivArrowUp);
        downArrow = (ImageView) dialog.findViewById(R.id.ivArrowDown);
        txtMinReviews = (TextView) dialog.findViewById(R.id.txtMinReviews);
        txtMaxPriceSelectedFilter = (TextView) dialog.findViewById(R.id.txtMaxPriceSelectedFilter);
        btnSubmitFilter = (Button) dialog.findViewById(R.id.btnSubmitFilter);

        txtDownloads.setTypeface(m_MyFont);
        txtPrice.setTypeface(m_MyFont);
        txtMinReviews.setTypeface(m_MyFont);
        txtMaxPriceSelectedFilter.setTypeface(m_MyFont);
        btnSubmitFilter.setTypeface(m_MyFont);

        upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_NumOfMinDownloads < 50000) {
                    m_NumOfMinDownloads = m_NumOfMinDownloads + 1000;
                    txtMinReviews.setText(Integer.toString(m_NumOfMinDownloads));
                }
            }
        });
        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_NumOfMinDownloads >= 1000) {
                    m_NumOfMinDownloads = m_NumOfMinDownloads - 1000;
                    txtMinReviews.setText(Integer.toString(m_NumOfMinDownloads));
                }
            }
        });

        sbPrice.setProgress(100);
        m_MaxPriceFilter = 10.0;
        txtMaxPriceSelectedFilter.setText(Double.toString(m_MaxPriceFilter) + '$');

        sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_MaxPriceFilter = progress / 10.0;
                txtMaxPriceSelectedFilter.setText(Double.toString(m_MaxPriceFilter) + '$');
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txtMaxPriceSelectedFilter.setText(Double.toString(m_MaxPriceFilter) + '$');
            }
        });

        btnSubmitFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_ListSortedBook = new ArrayList<>();
                for (Book book : m_ListBooks) {//add numOfReviews
                    if (book.getPrice() <= m_MaxPriceFilter && book.getDownloads() >= m_NumOfMinDownloads)
                        m_ListSortedBook.add(book);
                }
                dialog.dismiss();
                initRecyclerView(m_ListSortedBook);
            }
        });


        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_MaxPriceFilter = 10.0;
                m_NumOfMinDownloads = 0;
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void initUI()
    {
        m_MyFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        sortSpinner = (Spinner) findViewById(R.id.spinner_sort);
        ivFilter = (ImageView) findViewById(R.id.ivFilter);
    }

    private void checkPosition(int i_Position)
    {
        switch (i_Position) {
            case 1:
                m_ListSortedBook = m_Salf.sortPriceLowToHigh(m_ListBooks);
                break;
            case 2:
                m_ListSortedBook = m_Salf.sortRating(m_ListBooks);
                break;
            case 3:
                m_ListSortedBook = m_Salf.sortBookNameLibrary(m_ListBooks);
                break;
        }
    }

    private void checkUser()
    {
        m_FbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (m_FbUser == null) {
            Intent intent = new Intent(AllBooksActivity.this, SignInActivity.class);
            startActivity(intent);
        } else {
            m_MyUserRef = FirebaseDatabase.getInstance().getReference("Users/" + m_FbUser.getUid());
            m_MyUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    m_User = (User) getIntent().getSerializableExtra("user");
                    readingDataFromDatabase();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}
