package it.polito.mad.lab3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.createChooser;
import static android.provider.MediaStore.EXTRA_OUTPUT;

public class EditProfile extends AppCompatActivity {

    static final int GALLERY_REQ = 0;
    static final int CAMERA_REQ = 1;
    static final int CROP_REQ = 2;

    Bitmap imageBitmap;
    String imageUrl;
    Uri photo_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        final ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setBackgroundColor(Color.TRANSPARENT);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                saveData();
            }

        });

        final ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setBackgroundColor(Color.TRANSPARENT);

        final ImageButton newBookButton = findViewById(R.id.newBookButton);
        newBookButton.setBackgroundColor(Color.TRANSPARENT);


        final Button location_btn = findViewById(R.id.goto_search_place);
        location_btn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goToSearchPlace();
            }

        });

        String photo_saved = null;

        /*
        EditText name = findViewById(R.id.name);
        EditText mail = findViewById(R.id.mail);
        EditText bio = findViewById(R.id.bio);



        if (savedInstanceState != null) {
            name_saved = savedInstanceState.getString("name");
            mail_saved = savedInstanceState.getString("mail");
            bio_saved = savedInstanceState.getString("bio");
        } else {
            SharedPreferences sharedPref = this.getSharedPreferences("shared_id", Context.MODE_PRIVATE);
            name_saved = sharedPref.getString("name", null);
            mail_saved = sharedPref.getString("mail", null);
            bio_saved = sharedPref.getString("bio", null);
            photo_saved = sharedPref.getString("photo", null);
        }

        name.setText(name_saved);
        mail.setText(mail_saved);
        bio.setText(bio_saved);

        */

        SharedPreferences sharedPref = this.getSharedPreferences("shared_id", Context.MODE_PRIVATE);

        photo_saved = sharedPref.getString("photo", null);

        String uID = sharedPref.getString("uID", null);


        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(uID);


        if (savedInstanceState != null) {

            String name_saved = null;
            String mail_saved = null;
            String bio_saved = null;
            Bitmap bitmap_saved=null;

            name_saved = savedInstanceState.getString("name");
            mail_saved = savedInstanceState.getString("mail");
            bio_saved = savedInstanceState.getString("bio");
            bitmap_saved = savedInstanceState.getParcelable("BitmapImage");


            EditText name = findViewById(R.id.name);
            EditText mail = findViewById(R.id.mail);
            EditText bio = findViewById(R.id.bio);
            ImageView img = findViewById(R.id.img);

            img.setImageBitmap(bitmap_saved);
            name.setText(name_saved);
            mail.setText(mail_saved);
            bio.setText(bio_saved);

        } else {
            try {
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue(String.class);//getValue(String.class);
                        final String mail = dataSnapshot.child("mail").getValue(String.class);
                        final String bio = dataSnapshot.child("bio").getValue(String.class);
                        imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);


                        //System.out.println(name + mail + bio + imageUrl);

                        EditText show_name = findViewById(R.id.name);
                        EditText show_email = findViewById(R.id.mail);
                        EditText show_bio = findViewById(R.id.bio);

                        show_name.setText(name);
                        show_email.setText(mail);
                        show_bio.setText(bio);

                        if (imageUrl != null) {
                            try {
                                imageBitmap = decodeFromFirebaseBase64(imageUrl);
                                final ImageView img = findViewById(R.id.img);
                                img.setImageBitmap(imageBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
/*
                if (imageUrl == null) {
                    if (photo_saved != null) {
                        try {
                            Uri saved_uri = Uri.parse(photo_saved);
                            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), saved_uri);
                            final ImageView img = findViewById(R.id.img);
                            img.setImageBitmap(imageBitmap);
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    }
                }
                */

            } catch (NullPointerException e) {
                e.fillInStackTrace();
            }
        }


        final ImageButton photoButton = findViewById(R.id.photoSel);
        photoButton.setBackgroundColor(Color.TRANSPARENT);
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                selectImage();
            }

        });

    }


    public void saveData() {
        SharedPreferences sharedPref = this.getSharedPreferences("shared_id",Context.MODE_PRIVATE); //to save and load small data
        //SharedPreferences.Editor editor = sharedPref.edit();  //to modify shared preferences

        String uID = sharedPref.getString("uID", null);

        EditText edit_name = findViewById(R.id.name);   //edit text object instances
        EditText edit_mail = findViewById(R.id.mail);
        EditText edit_bio = findViewById(R.id.bio);
/*
        editor.putString("name", edit_name.getText().toString());
        editor.putString("mail", edit_mail.getText().toString());
        editor.putString("bio", edit_bio.getText().toString());

        Toast.makeText(getApplicationContext(), R.string.saveMessage, Toast.LENGTH_SHORT).show();

        editor.apply();
*/
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(uID);

        dbRef.child("name").setValue(edit_name.getText().toString());
        dbRef.child("mail").setValue(edit_mail.getText().toString());
        dbRef.child("bio").setValue(edit_bio.getText().toString());

        encodeBitmapAndSaveToFirebase(imageBitmap,dbRef);

        goToShowProfile();
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        EditText edit_name = findViewById(R.id.name);
        EditText edit_mail = findViewById(R.id.mail);
        EditText edit_bio = findViewById(R.id.bio);

        outState.putString("name", edit_name.getText().toString());
        outState.putString("mail", edit_mail.getText().toString());
        outState.putString("bio", edit_bio.getText().toString());

        outState.putParcelable("BitmapImage", imageBitmap);

    }


    private void save_photo() {
        /*
        SharedPreferences sharedPref = this.getSharedPreferences("shared_id",Context.MODE_PRIVATE); //to save and load small data
        SharedPreferences.Editor editor = sharedPref.edit();  //to modify shared preferences

        editor.putString("photo", photo_uri.toString());
        editor.apply();
        */
    }


    private void selectImage() {
        final CharSequence[] items = {getResources().getString(R.string.gallery),
                getResources().getString(R.string.photo),
                getResources().getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        //builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                //camera
                if (item == GALLERY_REQ) gallery();
                //gallery
                if (item == CAMERA_REQ) camera();

                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void gallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Uri gallery_uri = Uri.fromFile(getMediaFile()); // Gallery uri path
        //galleryIntent.putExtra(EXTRA_OUTPUT, gallery_uri);
        galleryIntent.putExtra("crop", "true");
        galleryIntent.putExtra("aspectX", 1);
        galleryIntent.putExtra("aspectY", 1);
        galleryIntent.putExtra("outputX", 240);
        galleryIntent.putExtra("outputY", 240);
        //galleryIntent.putExtra("return-data", true);
        galleryIntent.setType("image/*");
        startActivityForResult(createChooser(galleryIntent, "Select File"),GALLERY_REQ);
    }


    private void camera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photo_uri = Uri.fromFile(getMediaFile()); // Camera uri path
        takePictureIntent.putExtra(EXTRA_OUTPUT, photo_uri);
        takePictureIntent.putExtra("return-data", true);
        startActivityForResult(takePictureIntent, CAMERA_REQ);
    }


    private void performCrop () {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setDataAndType(photo_uri, "image/*");
        cropIntent.putExtra("crop", true);
        cropIntent.putExtra("outputX", 240);
        cropIntent.putExtra("outputY", 240);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("return-data", true);

        startActivityForResult(cropIntent, CROP_REQ);

    }


    private static File getMediaFile() {


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG1"+ ".png");
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //System.out.print("onActivivtyResult\n\n\n");


        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK)
        {
            Bundle gallery_bundle = data.getExtras();
            imageBitmap = (Bitmap) gallery_bundle.get("data");
            photo_uri = getImageUri(getApplicationContext(), imageBitmap);
            save_photo();

            final ImageView img = findViewById(R.id.img);
            img.setImageBitmap(imageBitmap);

        }

        else if (requestCode == CAMERA_REQ && resultCode == RESULT_OK)
        {
            performCrop();
        }

        else if (requestCode == CROP_REQ && resultCode == RESULT_OK)
        {
            Bundle cropped_image = data.getExtras();
            imageBitmap = (Bitmap) cropped_image.get("data");
            photo_uri = getImageUri(getApplicationContext(), imageBitmap);
            save_photo();

            final ImageView img = findViewById(R.id.img);
            img.setImageBitmap(imageBitmap);
        }

    }


    private void goToShowProfile () {

        Intent intent = new Intent(getApplicationContext(), it.polito.mad.lab3.ShowProfile.class);
        startActivity(intent);
    }


    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap, DatabaseReference dbRef) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            dbRef.child("imageUrl").setValue(imageEncoded);

        } catch (NullPointerException e) {
            e.fillInStackTrace();
        }
    }


    public static Bitmap decodeFromFirebaseBase64(String imageUrl) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(imageUrl, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }




    private void goToSearchPlace() {

        Intent intent = new Intent(getApplicationContext(), it.polito.mad.lab3.SearchPlace.class);
        startActivity(intent);
    }
}
