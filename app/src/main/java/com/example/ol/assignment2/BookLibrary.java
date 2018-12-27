package com.example.ol.assignment2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

public class BookLibrary extends AppCompatActivity {

    private String classStringName = "BookLibrary";
    private FirebaseUser fbUser;
    private User user;
    private DatabaseReference myUserRef;
    private ArrayList<Book> lstBook = new ArrayList<>();
    private ArrayList<Book> lstThrillerBooks = new ArrayList<>();
    private ArrayList<Book> lstSelfHelpBooks = new ArrayList<>();
    private ArrayList<Book> lstBiographyBooks = new ArrayList<>();
    private ArrayList<Book> lstFictionBooks = new ArrayList<>();
    private ArrayList<Book> lstNewBooks = new ArrayList<>();
    private ArrayList<Book> lstPopularityBooks = new ArrayList<>();
    private SortArrayListFields salf = new SortArrayListFields();
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_library);

        bottomNav = findViewById(R.id.bottom_navigation);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Intent intent = new Intent(BookLibrary.this, SignIn.class);
            startActivity(intent);
        }
            else {
            if (fbUser.isAnonymous()) {
                user = new User("guest", 0, null);
                readingDataFromDatabase();
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
            initNavBar();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(BookLibrary.this, Orders_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(BookLibrary.this, AllBooks_Activity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(BookLibrary.this, Search_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(BookLibrary.this, SignIn.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
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
                initListsFromDataBase(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void initRecyclerView() {
        initRecyclerViewLayouts(R.id.bl_new,lstNewBooks);
        initRecyclerViewLayouts(R.id.bl_popularity,lstPopularityBooks);
        initRecyclerViewLayouts(R.id.bl_fiction,lstFictionBooks);
        initRecyclerViewLayouts(R.id.bl_biography,lstBiographyBooks);
        initRecyclerViewLayouts(R.id.bl_selfhelp,lstSelfHelpBooks);
        initRecyclerViewLayouts(R.id.bl_thriller,lstThrillerBooks);
    }

    private void initRecyclerViewLayouts(int rv_index,ArrayList<Book> lst) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = (RecyclerView) findViewById(rv_index);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, user, classStringName);
        recyclerView.setAdapter(myAdapter);
    }


    public void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }


    public void initNavBar()
    {
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);
    }
    public void initListsFromDataBase(DataSnapshot dataSnapshot)
    {
        lstBook.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Book newBook = ds.getValue(Book.class);
            lstBook.add(newBook);
            if (newBook.getYear() >= 2017)
                lstNewBooks.add(newBook);
            if (newBook.getGenre().toLowerCase().equals("fiction"))
                lstFictionBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("biography"))
                lstBiographyBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("thriller"))
                lstThrillerBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("self-help"))
                lstSelfHelpBooks.add(newBook);
        }
        lstBook=salf.sortPopularity(lstBook);
        for(int i=0;i<20;i++)
        {
            lstPopularityBooks.add(lstBook.get(i));
        }
        initRecyclerView();
    }
}