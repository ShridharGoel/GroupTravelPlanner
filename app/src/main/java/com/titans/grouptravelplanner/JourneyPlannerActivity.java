package com.titans.grouptravelplanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

public class JourneyPlannerActivity extends AppCompatActivity {

    private String TAG = "JourneyPlannerActivity";
    private Singleton singleton;
    private ProgressDialog progressDialog;
    private FirestoreDbUtility firestoreDbUtility;
    private GeneralUtility generalUtility;
    private PlaceAutocompleteFragment sourceAutocompleteFragment;
    private PlaceAutocompleteFragment destinationAutocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_planner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        singleton = Singleton.getInstance(getApplicationContext());
        firestoreDbUtility = new FirestoreDbUtility();
        generalUtility = new GeneralUtility();

        sourceAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.source_autocomplete_fragment);
        destinationAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.destination_autocomplete_fragment);

        // setting filter
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                //.setTypeFilter(TYPE_FILTER_ADDRESS)
                .build();
        sourceAutocompleteFragment.setFilter(autocompleteFilter);
        destinationAutocompleteFragment.setFilter(autocompleteFilter);

        // setting hint or selected places
        setSelectedSourceAndDestinationPlace(sourceAutocompleteFragment,
                destinationAutocompleteFragment);

        // adding clear button listener
        sourceAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sourceAutocompleteFragment.setText("");
                        singleton.setSourcePlace(null);
                    }
                });
        destinationAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        destinationAutocompleteFragment.setText("");
                        singleton.setDestinationPlace(null);
                    }
                });

        // changing icon
        ImageView sourceSearchIcon = (ImageView)((LinearLayout)sourceAutocompleteFragment
                .getView()).getChildAt(0);
        sourceSearchIcon.setImageResource(R.drawable.ic_time_to_leave);

        ImageView destinationSearchIcon = (ImageView)((LinearLayout)destinationAutocompleteFragment
                .getView()).getChildAt(0);
        destinationSearchIcon.setImageResource(R.drawable.ic_destination);


        sourceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String placeAddress = place.getAddress().toString();
                Log.i(TAG, "Place: " + placeAddress);
                singleton.setSourcePlace(place);
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
                showMessage("Error" + " Status: " + status);
            }
        });
        destinationAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String placeAddress = place.getAddress().toString();
                Log.i(TAG, "Place: " + placeAddress);
                singleton.setDestinationPlace(place);
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
                showMessage("Error" + " Status: " + status);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showMessage(String message) {
        View parentLayout = findViewById(R.id.activityContent);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public void onRangeRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.range10RadioButton: {
                if (checked) {
                    singleton.setFilterRange(10);
                }
                break;
            }
            case R.id.range20RadioButton: {
                if (checked) {
                    singleton.setFilterRange(20);
                }
                break;
            }
            case R.id.range50RadioButton: {
                if (checked) {
                    singleton.setFilterRange(50);
                }
                break;
            }
        }
    }

    public void onSearchTravellersButtonClicked(View view) {
        Place sourcePlace = singleton.getSourcePlace();
        Place destinationPlace = singleton.getDestinationPlace();

        if (sourcePlace == null || destinationPlace == null) {
            showMessage("Please enter source as well as destination");
            return;
        }

        // clear current zoom level and target
        singleton.setGoogleMapCurrentCameraPosition(null);

        // go to MainActivity
        Intent intent = new Intent(JourneyPlannerActivity.this, MainActivity.class);

        // clear history stack so that back button does no lead to JourneyPlannerActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setSelectedSourceAndDestinationPlace(
            PlaceAutocompleteFragment sourceAutocompleteFragment,
            PlaceAutocompleteFragment destinationAutocompleteFragment) {

        Place sourcePlace = singleton.getSourcePlace();
        Place destinationPlace = singleton.getDestinationPlace();

        if (sourcePlace != null) {
            sourceAutocompleteFragment.setText(sourcePlace.getAddress().toString());
        } else {
            sourceAutocompleteFragment.setHint(
                    getResources().getString(R.string.enter_source_hint_text)
            );
        }

        if (destinationPlace != null) {
            destinationAutocompleteFragment.setText(destinationPlace.getAddress().toString());
        } else {
            destinationAutocompleteFragment.setHint(
                    getResources().getString(R.string.enter_destination_hint_text)
            );
        }
    }
}
