package mad.assignment.bookaddicts;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    
    ArrayList<BookInfo> bookInfoArrayList;
    ProgressBar progressBar;
    EditText edit;
    ImageButton search;
    TextView showEmpty;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.spinner);
        edit = findViewById(R.id.edit);
        search = findViewById(R.id.searchBtn);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForBook();
            }
        });

        edit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchForBook();
                    return true;
                }
                return false;
            }
        });
    }

    void searchForBook() {
        progressBar.setVisibility(View.VISIBLE);
        showEmpty = findViewById(R.id.show_empty);
        recyclerView = findViewById(R.id.bookRecyclerView);

        if (edit.getText().toString().isEmpty()) {
            edit.setError("Please enter book name to search!");
            progressBar.setVisibility(View.GONE);
        }
        else if(isOnline()) {
            showEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            displayBooks(edit.getText().toString());
        }
        else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "No Internet!", Toast.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
            showEmpty.setVisibility(View.VISIBLE);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    private void displayBooks(String query) {
        bookInfoArrayList = new ArrayList<>();
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        String publisher = volumeObj.optString("publisher");
                        String publishedDate = volumeObj.optString("publishedDate");
                        String description = volumeObj.optString("description");
                        int pageCount = volumeObj.optInt("pageCount");
                        String previewLink = volumeObj.optString("previewLink");
                        String infoLink = volumeObj.optString("infoLink");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        String buyLink = saleInfoObj.optString("buyLink");


//                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
//                        String thumbnail = imageLinks.optString("small");
                        String thumbnail;
                        try {
                            JSONObject imageLinks = volumeObj.getJSONObject("imageLinks");
                            thumbnail= imageLinks.getString("smallThumbnail");
                            thumbnail = thumbnail.substring(0, 4) + 's' + thumbnail.substring(4);
                        } catch (JSONException e) {
                             e.printStackTrace();
                            thumbnail = "false";
                        }

//                        String thumbnail="";
//                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
//                        if (imageLinks != null && imageLinks.has("thumbnail")) {
//                            thumbnail = imageLinks.getString("thumbnail");
//                        }


                        BookInfo bookInfo = new BookInfo(title, subtitle, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink);
                        bookInfoArrayList.add(bookInfo);

                        BookAdapter bookAdapter = new BookAdapter(bookInfoArrayList, MainActivity.this);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
                        recyclerView = findViewById(R.id.bookRecyclerView);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(bookAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "No Book Found!!!", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(req);
    }
}
