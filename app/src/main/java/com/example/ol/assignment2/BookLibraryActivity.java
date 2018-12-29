package com.example.ol.assignment2;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
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

public class BookLibraryActivity extends AppCompatActivity {

    private String classStringName = "BookLibraryActivity";
    private BottomNavigationView bottomNav;
    private TextView txtNewOnTheShelf, txtPopularity, txtBiography, txtThriller, txtFiction, txtSelfHelp;
    Typeface m_MyFont;

    private FirebaseUser m_FbUser;
    private User m_User;
    private DatabaseReference m_MyUserRef;
    private ArrayList<Book> m_ListBooks = new ArrayList<>();
    private ArrayList<Book> m_ListThrillerBooks = new ArrayList<>();
    private ArrayList<Book> m_ListSelfHelpBooks = new ArrayList<>();
    private ArrayList<Book> m_ListBiographyBooks = new ArrayList<>();
    private ArrayList<Book> m_ListFictionBooks = new ArrayList<>();
    private ArrayList<Book> m_ListNewBooks = new ArrayList<>();
    private ArrayList<Book> m_ListPopularityBooks = new ArrayList<>();
    private SortArrayListFields m_Salf = new SortArrayListFields();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_library);
        initUI();
        initUser();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(BookLibraryActivity.this, OrdersActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBooks);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(BookLibraryActivity.this, AllBooksActivity.class);
                            intent.putExtra("user", m_User);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(BookLibraryActivity.this, SearchActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBooks);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            Toast.makeText(BookLibraryActivity.this, "Sign out success.", Toast.LENGTH_LONG).show();
                            intent = new Intent(BookLibraryActivity.this, SignInActivity.class);
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
        initRecyclerViewLayouts(R.id.bl_new, m_ListNewBooks);
        initRecyclerViewLayouts(R.id.bl_popularity, m_ListPopularityBooks);
        initRecyclerViewLayouts(R.id.bl_fiction, m_ListFictionBooks);
        initRecyclerViewLayouts(R.id.bl_biography, m_ListBiographyBooks);
        initRecyclerViewLayouts(R.id.bl_selfhelp, m_ListSelfHelpBooks);
        initRecyclerViewLayouts(R.id.bl_thriller, m_ListThrillerBooks);
    }

    private void initRecyclerViewLayouts(int rv_index, ArrayList<Book> lst) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = (RecyclerView) findViewById(rv_index);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, m_User, classStringName, m_MyFont, m_ListBooks);
        recyclerView.setAdapter(myAdapter);
    }

    public void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    public void initNavBar() {
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);
    }

    public void initListsFromDataBase(DataSnapshot dataSnapshot) {
        m_ListBooks.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Book newBook = ds.getValue(Book.class);
            m_ListBooks.add(newBook);
            if (newBook.getYear() >= 2017)
                m_ListNewBooks.add(newBook);
            if (newBook.getGenre().toLowerCase().equals("fiction"))
                m_ListFictionBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("biography"))
                m_ListBiographyBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("thriller"))
                m_ListThrillerBooks.add(newBook);
            else if (newBook.getGenre().toLowerCase().contains("self-help"))
                m_ListSelfHelpBooks.add(newBook);
        }
        m_ListBooks = m_Salf.sortPopularity(m_ListBooks);
        for (int i = 0; i < 20; i++) {
            m_ListPopularityBooks.add(m_ListBooks.get(i));
        }
        initRecyclerView();
    }

    private void initUI() {
        bottomNav = findViewById(R.id.bottom_navigation);
        m_MyFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        txtNewOnTheShelf = (TextView) findViewById(R.id.txtnew);
        txtPopularity = (TextView) findViewById(R.id.txtpopularity);
        txtBiography = (TextView) findViewById(R.id.txtBiography);
        txtThriller = (TextView) findViewById(R.id.txtThriller);
        txtFiction = (TextView) findViewById(R.id.txtFiction);
        txtSelfHelp = (TextView) findViewById(R.id.txtSelfhelp);
        txtNewOnTheShelf.setTypeface(m_MyFont);
        txtPopularity.setTypeface(m_MyFont);
        txtBiography.setTypeface(m_MyFont);
        txtThriller.setTypeface(m_MyFont);
        txtFiction.setTypeface(m_MyFont);
        txtSelfHelp.setTypeface(m_MyFont);
    }

    private void initUser()
    {
        m_FbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (m_FbUser == null) {
            Intent intent = new Intent(BookLibraryActivity.this, SignInActivity.class);
            startActivity(intent);
        } else {
            if (m_FbUser.isAnonymous()) {
                m_User = new User("guest", 0, null);
                readingDataFromDatabase();
            } else {
                m_MyUserRef = FirebaseDatabase.getInstance().getReference("Users/" + m_FbUser.getUid());
                m_MyUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        m_User = snapshot.getValue(User.class);
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
}