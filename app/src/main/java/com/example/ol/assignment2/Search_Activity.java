package com.example.ol.assignment2;

import android.content.Context;
import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;

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

public class Search_Activity extends AppCompatActivity {

    private String classStringName = "Search_Activity";
    TextView tvNoBooksFound;
    RecyclerView rvBookList;
    Context context;
    ArrayList<Book> lstBook=new ArrayList<Book>();
    ArrayList<Book> filteredList=new ArrayList<Book>();
    private EditText etSearch;
    private FirebaseUser fbUser;
    private User user;
    private DatabaseReference myUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        tvNoBooksFound=(TextView)findViewById(R.id.txtNoBooksFound);
        tvNoBooksFound.setVisibility(View.INVISIBLE);
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());

            }
        });
        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Intent intent = new Intent(Search_Activity.this, SignIn.class);
            startActivity(intent);
        } else {
            user = (User) getIntent().getSerializableExtra("user");
            lstBook = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");
        }
        rvBookList=(RecyclerView) findViewById(R.id.rv_filtered_books);
        initRecyclerView(filteredList);
    }


private void filter(String text) {
    filteredList.clear();
    if (!text.isEmpty()) {
        for (Book book : lstBook) {
            if (book.getBook().getTitle().toLowerCase().contains(text.toLowerCase()) |
                    book.getBook().getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(book);
            }
        }
        if(filteredList.size()==0) {
            tvNoBooksFound.setVisibility(View.VISIBLE);
            tvNoBooksFound.setText("There is no books for the search: "+"\""+etSearch.getText().toString()+"\"");
        }
        else {
            tvNoBooksFound.setVisibility(View.INVISIBLE);
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
                            intent = new Intent(Search_Activity.this, Orders_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_all:
                            intent = new Intent(Search_Activity.this, AllBooks_Activity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(Search_Activity.this, SignIn.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent= new Intent(Search_Activity.this,BookLibrary.class);
                            startActivity(intent);
                            break;
                    }
                    return true;
                }
            };
    private void initRecyclerView(ArrayList<Book> lst) {
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, lst, user, classStringName);
        rvBookList.setAdapter(myAdapter);
        rvBookList.setLayoutManager(new GridLayoutManager(this, 3));
        context = rvBookList.getContext();
    }

    public void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }
}
