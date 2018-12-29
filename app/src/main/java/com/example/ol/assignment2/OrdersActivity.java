package com.example.ol.assignment2;

import android.content.Context;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.model.Order;
import com.example.ol.assignment2.adapter.RecyclerViewOrdersAdapter;
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

public class OrdersActivity extends AppCompatActivity {

    private String classStringName = "OrdersActivity";
    private TextView titleText;

    ArrayList<Book> m_ListBook;
    ArrayList<Order> m_ListOrders = new ArrayList<Order>();
    private User m_User;
    private FirebaseUser m_FbUser;
    Typeface m_MyFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        initVariablesAndUI();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_orders:
                            break;
                        case R.id.nav_all:
                            intent = new Intent(OrdersActivity.this, AllBooksActivity.class);
                            intent.putExtra("user", m_User);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(OrdersActivity.this, SearchActivity.class);
                            intent.putExtra("user", m_User);
                            intent.putExtra("booksList", m_ListBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            Toast.makeText(OrdersActivity.this, "Sign out success.", Toast.LENGTH_LONG).show();
                            intent = new Intent(OrdersActivity.this, SignInActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent = new Intent(OrdersActivity.this, BookLibraryActivity.class);
                            startActivity(intent);
                            break;
                    }
                    return true;
                }
            };

    private void initRecyclerView() {
        RecyclerView rvOrdersList = (RecyclerView) findViewById(R.id.orders_list);
        rvOrdersList.setLayoutManager(new LinearLayoutManager(this));
        Context context = rvOrdersList.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down);
        RecyclerViewOrdersAdapter myAdapter = new RecyclerViewOrdersAdapter(this, m_ListOrders, m_User, classStringName, m_MyFont, m_ListBook);
        rvOrdersList.setAdapter(myAdapter);

        rvOrdersList.setLayoutAnimation(controller);
        rvOrdersList.getAdapter().notifyDataSetChanged();
        rvOrdersList.scheduleLayoutAnimation();
    }

    public void signOutFirebase() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    private void buildOrdersList() {
        m_FbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (m_FbUser == null) {
            Toast.makeText(OrdersActivity.this, "You must log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(OrdersActivity.this, SignInActivity.class);
            startActivity(intent);
        } else {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(m_FbUser.getUid());
            DatabaseReference mReviewsBookDatabase = mDatabase.child("myBooks");
            mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    m_ListOrders.clear();
                    Book bookOfOrder;
                    Order currentOrder;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        int currentID = ds.getValue(Integer.class);
                        bookOfOrder = findBookInList(currentID);
                        currentOrder = new Order(bookOfOrder, "26/12/2018", false);
                        m_ListOrders.add(currentOrder);
                    }
                    initRecyclerView();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private Book findBookInList(int bookId) {
        Book searchBook = null;

        for (int i = 0; i < m_ListBook.size(); i++) {
            if (m_ListBook.get(i).getId() == bookId) {
                searchBook = m_ListBook.get(i);
                break;
            }
        }
        return searchBook;
    }

    private void initVariablesAndUI()
    {
        Intent intent = getIntent();
        m_User = (User) intent.getSerializableExtra("user");
        m_MyFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        m_ListBook = (ArrayList<Book>) intent.getSerializableExtra("booksList");

        if (m_User.getEmail().equals("guest")) {
            signOutFirebase();
            m_User = null;
            Intent intent2 = new Intent(OrdersActivity.this, SignInActivity.class);
            intent2.putExtra("activityFrom", classStringName);
            intent2.putExtra("booksList", m_ListBook);
            Toast.makeText(OrdersActivity.this, "You must sign in for enter to your orders.", Toast.LENGTH_LONG);
            startActivity(intent2);
        } else {
            titleText = findViewById(R.id.titleText);
            titleText.setText("Total Orders: " + m_User.getTotalPurchase());
            titleText.setTypeface(m_MyFont);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            Menu menu = bottomNav.getMenu();
            MenuItem menuItem = menu.getItem(1);
            menuItem.setChecked(true);
            buildOrdersList();
        }

    }
}

