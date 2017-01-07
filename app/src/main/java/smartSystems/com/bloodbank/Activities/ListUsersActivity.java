package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import smartSystems.com.bloodBank.Model.User;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartSystems.com.bloodBank.R;

public class ListUsersActivity extends AppCompatActivity {

    private static final String TAG = "user";
    private DatabaseReference mDatabase;
    static Map<String, String> users = new HashMap<>();
    public static final String USER = "USER";
    public static List<String> userNames = new ArrayList<>();
    public static List<String> ids = new ArrayList<>();
    private EditText etSearch;
    ArrayAdapter<String> adapter;
    private static String donor, currentId, currentAddress, address;
    private static LatLng currentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        //mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final ListView lv = (ListView) findViewById(R.id.listView);
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // make first character uppercase to match bloodtype in database

        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.child("users").child(current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                    donor = values.get("donor");
                    currentAddress = values.get("address");
                    currentLatLng = getLatLng(currentAddress);
                }

                if (donor.equals("Yes")) {
                    mDatabase.child("users").orderByChild("donor").equalTo("No").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            users.clear();
                            userNames.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                address = user.getAddress();
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                Log.d("distance", String.valueOf(distance / 1000));
                                if (distance / 1000 > 15.0) { //divise by 1000 to get distance in Kilometers
                                    users.put(snapshot.getKey(), user.getUsername());
                                }
                            }
                            if (users.size() == 0)
                                Toast.makeText(ListUsersActivity.this, "There are no users in need of blood near your area, " +
                                        "please refer to the search filters", Toast.LENGTH_SHORT).show();

                            userNames = new ArrayList<String>(users.values());
                            adapter = new ArrayAdapter<>(
                                    ListUsersActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    userNames);

                            ids = new ArrayList<String>(users.keySet());
                            lv.setAdapter(adapter);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(ListUsersActivity.this, DetailActivity.class);
                                    String id = ids.get(i);
                                    intent.putExtra(USER, id);
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if (donor.equals("No")) {
                    mDatabase.child("users").orderByChild("donor").equalTo("Yes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            users.clear();
                            userNames.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                address = user.getAddress();
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                if (distance / 1000 > 15.0) { //divise by 1000 to get distance in Kilometers
                                    users.put(snapshot.getKey(), user.getUsername());
                                }
                            }
                            if (users.size() == 0)
                                Toast.makeText(ListUsersActivity.this, "There are no blood donors near your area, " +
                                        "please refer to the search filters", Toast.LENGTH_SHORT).show();

                            userNames = new ArrayList<String>(users.values());
                            adapter = new ArrayAdapter<>(
                                    ListUsersActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    userNames);

                            ids = new ArrayList<String>(users.keySet());
                            lv.setAdapter(adapter);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(ListUsersActivity.this, DetailActivity.class);
                                    String id = ids.get(i);
                                    intent.putExtra(USER, id);
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());

            }
        });
    }

    public LatLng getLatLng(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address location = addressList.get(0);
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
