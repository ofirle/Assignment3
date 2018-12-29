package com.example.ol.assignment2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.adapter.RecyclerViewAdapter;
import com.example.ol.assignment2.model.Book;
import com.example.ol.assignment2.model.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private String classStringName = "SearchActivity";
    TextView tvNoBooksFound;
    RecyclerView rvBookList;
    private EditText etSearch;

    Context m_Context;
    ArrayList<Book> m_ListBooks = new ArrayList<Book>();
    ArrayList<Book> m_FilteredList = new ArrayList<Book>();
    private FirebaseUser m_FbUser;
    private User m_User;
    Typeface m_MyFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        m_MyFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        initUI();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        initUser();
        rvBookList = (RecyclerView) findViewById(R.id.rv_filtered_books);
        initRecyclerView(m_FilteredList);
    }


    private void filter(String text) {
        tvNoBooksFound.setVisibility(View.INVISIBLE);
        m_FilteredList.clear();
        if (!text.isEmpty()) {
            for (Book book : m_ListBooks) {
                if (book.getBook().getTitle().toLowerCase().contains(text.toLowerCase()) |
                        book.getBook().getTitle().toLowerCase().contains(text.toLowerCase())) {
                    m_FilteredList.add(book);
                }
            }

            if (m_FilteredList.size() == 0) {
                tvNoBooksFound.setVisibility(View.VISIBLE);
                tvNoBooksFound.setText("There is no books for the search: " + "\"" + etSearch.getText().toString() + "\"");
            } else {
                tvNoBooksFound.setText("");
            }
        }
        rvBookList.getAdapter().notifyDataSetChanged();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            intent = new Intent(SearchActivity.this, OrdersActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBooks);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(SearchActivity.this, AllBooksActivity.class);
                            intent.putExtra("user", m_User);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            Toast.makeText(SearchActivity.this, "Sign out success.", Toast.LENGTH_LONG).show();
                            intent = new Intent(SearchActivity.this, SignInActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent = new Intent(SearchActivity.this, BookLibraryActivity.class);
                            startActivity(intent);
                            break;
                    }
                    return true;
                }
            };

    private void initRecyclerView(ArrayList<Book> lst) {
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, m_User, classStringName, m_MyFont, m_ListBooks);
        rvBookList.setAdapter(myAdapter);
        rvBookList.setLayoutManager(new GridLayoutManager(this, 3));
        m_Context = rvBookList.getContext();
    }

    private void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    private void initUser()
    {
        m_FbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (m_FbUser == null) {
            Intent intent = new Intent(SearchActivity.this, SignInActivity.class);
            startActivity(intent);
        } else {
            m_User = (User) getIntent().getSerializableExtra("user");
            m_ListBooks = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");
        }
    }

    private void initUI()
    {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        tvNoBooksFound = (TextView) findViewById(R.id.txtNoBooksFound);
        tvNoBooksFound.setVisibility(View.INVISIBLE);
        tvNoBooksFound.setTypeface(m_MyFont);
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setTypeface(m_MyFont);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
    }
}
