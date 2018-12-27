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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.adapter.RecyclerViewAdapter;
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
import java.util.Iterator;

public class Orders_Activity extends AppCompatActivity {

    private String classStringName = "Orders_Activity";
    private TextView titleText;
    ArrayList<Book> lstBook;
    ArrayList<Order> lstOrders = new ArrayList<Order>();
    private User user;
    private FirebaseUser fbUser;
    private DatabaseReference myUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if(user.getEmail().equals("guest"))
        {
            signOutFirebase();
            user = null;
            Intent intent2 = new Intent(Orders_Activity.this, SignIn.class);
            startActivity(intent2);
        }
        else {
            lstBook = (ArrayList<Book>) intent.getSerializableExtra("booksList");
            titleText = findViewById(R.id.titleText);
            titleText.setText("Orders Total: " + user.getTotalPurchase());
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            Menu menu = bottomNav.getMenu();
            MenuItem menuItem = menu.getItem(1);
            menuItem.setChecked(true);
            buildOrdersList();
        }

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
                            intent = new Intent(Orders_Activity.this, AllBooks_Activity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            break;
                        case R.id.nav_search:
                            intent = new Intent(Orders_Activity.this, Search_Activity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("booksList", lstBook);
                            startActivity(intent);
                            break;
                        case R.id.nav_signOut:
                            signOutFirebase();
                            intent = new Intent(Orders_Activity.this, SignIn.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_home:
                            intent = new Intent(Orders_Activity.this, BookLibrary.class);
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
        RecyclerViewOrdersAdapter myAdapter = new RecyclerViewOrdersAdapter(this, lstOrders, user, classStringName);
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
        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Intent intent = new Intent(Orders_Activity.this, SignIn.class);
            startActivity(intent);
        } else {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(fbUser.getUid());
            DatabaseReference mReviewsBookDatabase = mDatabase.child("myBooks");
            mReviewsBookDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    lstOrders.clear();
                    Book bookOfOrder;
                    Order currentOrder;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        int currentID = ds.getValue(Integer.class);
                        bookOfOrder = findBookInList(currentID);
                        currentOrder = new Order(bookOfOrder, "26/12/2018", false);
                        lstOrders.add(currentOrder);
                        }

                    initRecyclerView();
                    }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });
        }
    }

    private Book findBookInList(int bookId)
    {
        Book searchBook = null;

        for(int i=0; i<lstBook.size();i++)
        {
            if(lstBook.get(i).getId() == bookId)
            {
                searchBook = lstBook.get(i);
                break;
            }
        }
      return searchBook;
    }
}

