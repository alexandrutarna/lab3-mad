package it.polito.mad.lab3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.internal.PlaceEntity;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;


public class SearchPlace extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);

    }

    public void findPlace(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                Log.e("Tag", "Place: " + place.getAddress() + "\n");

                Log.e("place", "tostring: " + place.toString()+ "\n");

                Log.e("place", "id: " + place.getId()+ "\n");
                Log.e("place", "name: " + place.getName()+ "\n");
                Log.e("place", "address: " + place.getAddress()+ "\n");
                Log.e("place", "atributions: " + place.getAttributions()+ "\n");
                Log.e("place", "latlng: " + place.getLatLng() + "\n");

                String coordinates = place.getLatLng().toString();
                System.out.println(coordinates +"\n");

                int startPos = coordinates.indexOf("(");
                int commaPos = coordinates.indexOf(",");

                String lat = coordinates.substring(startPos+1,commaPos);
                String lng = coordinates.substring(commaPos+1,coordinates.length() -1 );
                System.out.println("lat: " + lat + "\n");
                System.out.println("lng: " + lng + "\n");

                String address = place.getAddress().toString();
                System.out.println("address: " + address);

                String[] values = address.split(",");
                String country = values[values.length -1];
                country = country.replaceAll("\\s+","");

                String city = values[0];
                System.out.println("country :" + country);
                System.out.println("city :" + city);







                ((TextView) findViewById(R.id.searched_address)).setText(place.getName()+",\n"+
                        place.getAddress() +"\n" + place.getPhoneNumber());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
