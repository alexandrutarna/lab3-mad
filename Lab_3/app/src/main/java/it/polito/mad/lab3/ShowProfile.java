package it.polito.mad.lab3;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.ArrayAdapter;


public class ShowProfile extends AppCompatActivity  implements View.OnClickListener{

    //private DatabaseReference db;
    Bitmap imageBitmap;
    String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);


        final ImageButton editButton = findViewById(R.id.editButton);
        editButton.setBackgroundColor(Color.TRANSPARENT);
        editButton.setOnClickListener(this);

        final ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setBackgroundColor(Color.TRANSPARENT);
        searchButton.setOnClickListener(this);

        final ImageButton newBookButton = findViewById(R.id.newBookButton);
        newBookButton.setBackgroundColor(Color.TRANSPARENT);
        newBookButton.setOnClickListener(this);

    }
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = this.getSharedPreferences("shared_id", Context.MODE_PRIVATE);
        final String uID = sharedPref.getString("uID", null);

//        makeText(this, uID, LENGTH_LONG).show();

        //String photoPath; //= sharedPref.getString("photo", null);

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();//child("users").child(uID);
        final ArrayList<Book> list = new ArrayList<Book>();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("users").child(uID).child("name").getValue(String.class);
                final String mail = dataSnapshot.child("users").child(uID).child("mail").getValue(String.class);
                final String bio = dataSnapshot.child("users").child(uID).child("bio").getValue(String.class);
                imageUrl = dataSnapshot.child("users").child(uID).child("imageUrl").getValue(String.class);

                //System.out.println(name + mail + bio + imageUrl);

                TextView show_name = findViewById(R.id.name);
                TextView show_email = findViewById(R.id.mail);
                TextView show_bio = findViewById(R.id.bio);

                show_name.setText(name);
                show_email.setText(mail);
                show_bio.setText(bio);

                if (imageUrl != null) {
                    try {
                        imageBitmap = decodeFromFirebaseBase64(imageUrl);
                        final ImageView img = findViewById(R.id.img);
                        img.setImageBitmap(imageBitmap);
                        //System.out.println("la sto prendendo dal database");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (DataSnapshot issue : dataSnapshot.child("users").child(uID).child("copies").getChildren()) {
                    System.out.println("sono dentro\n");
                    String key = issue.getValue(String.class);
                    System.out.println("key"+key+"\n");
                    String isbn = dataSnapshot.child("copies").child(key).child("isbn").getValue(String.class);
                    System.out.println("isbn"+isbn+"\n");
                    String title = dataSnapshot.child("books").child(isbn).child("title").getValue(String.class);
                    Book book = new Book(title); System.out.println("titolo"+title+"\n");
                    list.add(book);
                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        /*
        if (imageUrl == null) {
            if (photoPath != null) {
                try {
                    Uri saved_uri = Uri.parse(photoPath);
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), saved_uri);
                    final ImageView img = findViewById(R.id.img);
                    img.setImageBitmap(imageBitmap);
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
        */
        BookAdapter adapter = new BookAdapter(this, list);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
/*
        ArrayList<Book> list = new ArrayList<Book>();
        Book  book1 = new Book ("la via della luce");
        Book  book2 = new Book ("la via della puttana");
        list.add(book1);
        list.add(book2);
        BookAdapter adapter = new BookAdapter(this, list);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);*/

    }


    //@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editButton: editProfile(); break;
            case R.id.searchButton:
                System.out.println("Search Button");
                EditText searchBar = findViewById(R.id.searchBar);
                String toFind = searchBar.getText().toString();
                System.out.println(toFind);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child("books").orderByChild("title").startAt(toFind).endAt(toFind+"\uf8ff");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            System.out.println("snapshot exists");
                            // dataSnapshot is the "issue" node with all children with id 0
                            for (DataSnapshot issue : dataSnapshot.getChildren() /*.child("books").getChildren() */) {
                                String title = issue.child("title").getValue(String.class);
                                System.out.println("item found: "+ title+"\n");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.newBookButton: newBook(); break;
        }
    }

    public void editProfile(){
        Intent intent = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(intent);
    }

    public void newBook(){
        Intent intent = new Intent(getApplicationContext(), NewBook.class);
        startActivity(intent);
    }

    public void signiIn(){
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
    }

    public static Bitmap decodeFromFirebaseBase64(String imageUrl) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(imageUrl, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

}


