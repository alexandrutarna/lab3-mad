package it.polito.mad.lab3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static android.content.Intent.createChooser;
import static android.provider.MediaStore.EXTRA_OUTPUT;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;


public class NewBook extends AppCompatActivity implements View.OnClickListener {

    static final int GALLERY_REQ = 0;
    static final int CAMERA_REQ = 1;
    static final int CROP_REQ = 2;

    private static final int RC_BARCODE_CAPTURE = 3;

    private EditText title;
    private EditText author;
    private EditText publisher;
    private EditText editionYear;
    private EditText bookCondition;
    private EditText isbn;
    private EditText bookNotes;

    ImageButton saveButton;
    ImageButton takePhoto;
    ImageButton sendIsbn;

    private String isbn_string = "";
    private String title_str = "";
    private String publisher_str = "";
    private String editionYear_str = "";
    private String author_str = "";



    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    Uri photo_uri;

    private String TAG = NewBook.class.getSimpleName();
    private final static String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    //String isbn = "";
    //9788815128218
    //9788834001424
    //String title = "";
    //String author = "";
    //String publisher = "";
    //String editionYear = "";

    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        saveButton = findViewById(R.id.saveButton);
        takePhoto = findViewById(R.id.takePhotoBook);
        sendIsbn = findViewById(R.id.sendIsbn);

        title = findViewById(R.id.title);
        author = findViewById(R.id.author);
        publisher = findViewById(R.id.publisher);
        editionYear = findViewById(R.id.editionYear);
        bookCondition = findViewById(R.id.bookCondition);
        isbn = findViewById(R.id.isbn);
        bookNotes = findViewById(R.id.bookNotes);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBook();
            }
        });
        saveButton.setBackgroundColor(Color.TRANSPARENT);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_image();
            }
        });
        takePhoto.setBackgroundColor(Color.TRANSPARENT);


        sendIsbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askInfo();
            }
        });
        sendIsbn.setBackgroundColor(Color.TRANSPARENT);

        findViewById(R.id.read_barcode).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.read_barcode).setOnClickListener(this);


        mAuth = FirebaseAuth.getInstance();


        // TODO: Attach a new AuthListener to detect sign in and out
       /*
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();}};
        */
    }//end of onCreate

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            //intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            //intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, true);

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    void saveBook() {

        SharedPreferences sharedPref = this.getSharedPreferences("shared_id",Context.MODE_PRIVATE); //to save and load small data
        String uID = sharedPref.getString("uID", null);

        String title_to_save = title.getText().toString();
        String author_to_save = author.getText().toString();
        String publisher_to_save = publisher.getText().toString();
        String editionYear_to_save = editionYear.getText().toString();
        String bookCondition_to_save = bookCondition.getText().toString();
        String isbn_to_save = isbn.getText().toString();
        String bookNotes_to_save = bookNotes.getText().toString();

        Toast.makeText(this, title_to_save, Toast.LENGTH_LONG).show();

        DatabaseReference booksDBRef = FirebaseDatabase.getInstance().getReference().child("books").child(isbn_to_save);

        String newCopyRef = FirebaseDatabase.getInstance().getReference().child("copies").push().getKey();
        DatabaseReference copyDBRef = FirebaseDatabase.getInstance().getReference().child("copies").child(newCopyRef);

        booksDBRef.child("editionYear").setValue(editionYear_to_save);
        booksDBRef.child("publisher").setValue(publisher_to_save);
        booksDBRef.child("author").setValue(author_to_save);
        booksDBRef.child("title").setValue(title_to_save);
        booksDBRef.child("isbn").setValue(isbn_to_save);

        copyDBRef.child("bookCondition").setValue(bookCondition_to_save);
        copyDBRef.child("notes").setValue(bookNotes_to_save);
        copyDBRef.child("userID").setValue(uID);
        copyDBRef.child("isbn").setValue(isbn_to_save);
        copyDBRef.child("copyID").setValue(newCopyRef);


        encodeBitmapAndSaveToFirebase(imageBitmap,newCopyRef);

        finish();
    }


    private void select_image() {
        final CharSequence[] items = {getResources().getString(R.string.gallery),
                getResources().getString(R.string.photo),
                getResources().getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(NewBook.this);
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

        System.out.println("passo dal CROP");
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

        System.out.print("onActivivtyResult\n\n\n");


        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK)
        {

            Bundle gallery_bundle = data.getExtras();
            //Bitmap
            imageBitmap = (Bitmap) gallery_bundle.get("data");
            photo_uri = getImageUri(getApplicationContext(), imageBitmap);
            //save_photo();

            final ImageView photoBook = findViewById(R.id.photoBook);
            photoBook.setImageBitmap(imageBitmap);

        }

        else if (requestCode == CAMERA_REQ && resultCode == RESULT_OK)
        {
            performCrop();
        }

        else if (requestCode == CROP_REQ && resultCode == RESULT_OK)
        {
            Bundle cropped_image = data.getExtras();
            //Bitmap
            imageBitmap = (Bitmap) (cropped_image != null ? cropped_image.get("data") : null);
            photo_uri = getImageUri(getApplicationContext(), imageBitmap);
            //save_photo();

            final ImageView photoBook = findViewById(R.id.photoBook);
            photoBook.setImageBitmap(imageBitmap);
        }

        else if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    isbn.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    //statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }
    }


    private void askInfo () {
            EditText isbnET = findViewById(R.id.isbn);
            System.out.println("askinfo : "+isbnET.getText().toString());
            isbn_string = isbnET.getText().toString();
            new GetBook().execute();
        }


    private class GetBook extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg0) {

            String url = baseUrl + isbn_string;
            System.out.println(url);

            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            //Log.e(TAG, "Response from url: " + jsonStr);


            return jsonStr;
        }//end of doInBackground
        protected void onPostExecute(String jsonStr){
            super.onPostExecute(jsonStr);

            //final EditText titleET = findViewById(R.id.title);
            //final EditText authorET = findViewById(R.id.author);
            //final EditText publisherET = findViewById(R.id.publisher);
            //final EditText editionYearET = findViewById(R.id.editionYear);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                        try { title_str = volumeInfo.getString("title"); } catch (Exception e) {}
                        try { publisher_str = volumeInfo.getString("publisher"); } catch (Exception e) {}
                        try { editionYear_str = volumeInfo.getString("publishedDate"); } catch (Exception e) {}


                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        for (int j = 0; j < authors.length(); j++) {
                            try {
                                author_str += authors.getString(j);
                            } catch (Exception e) {
                            }
                            if (j < authors.length()-1) {
                                author_str += ", ";
                                //System.out.println(author);
                            }
                        }
                        //authorET.setText(author_str);

                        //System.out.println(title_str + " " + author_str);


                        //titleET.setText(title_str);
                        //publisherET.setText(publisher_str);
                        //editionYearET.setText(editionYear_str);

                        author.setText(author_str);
                        title.setText(title_str);
                        publisher.setText(publisher_str);
                        editionYear.setText(editionYear_str);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }}

    }//end of GetBook


    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap, String newCopyRef) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            DatabaseReference copyDBRef = FirebaseDatabase.getInstance().getReference().child("copies").child(newCopyRef);
            copyDBRef.child("imageUrl").setValue(imageEncoded);
        } catch (NullPointerException e) {
            e.fillInStackTrace();
        }
    }


}