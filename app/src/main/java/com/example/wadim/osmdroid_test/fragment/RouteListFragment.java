package com.example.wadim.osmdroid_test.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.wadim.osmdroid_test.Movie;
import com.example.wadim.osmdroid_test.Route;
import com.example.wadim.osmdroid_test.app.MyApplication;
import com.example.wadim.osmdroid_test.helper.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.wadim.osmdroid_test.R;

public class RouteListFragment extends Fragment {

    private static final String TAG = RouteListFragment.class.getSimpleName();

    // url to fetch shopping items
   // private static final String URL = "http://ec2-18-184-119-144.eu-central-1.compute.amazonaws.com/api/routes/50/19";//"https://api.androidhive.info/json/movies_2017.json";
    private String URL;

    private static String URL0 = "http://ec2-18-197-4-23.eu-central-1.compute.amazonaws.com/api/routes/";
    private static String URL2 = "http://ec2-18-197-4-23.eu-central-1.compute.amazonaws.com/api/routes/";

    private RecyclerView recyclerView;
    private List<Route> itemsList;
    private StoreAdapter mAdapter;
    private SharedPreferences prefs;


    public RouteListFragment() {
        // Required empty public constructor
    }

    public static RouteListFragment newInstance(String param1, String param2) {
        RouteListFragment fragment = new RouteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        itemsList = new ArrayList<>();
        mAdapter = new StoreAdapter(getActivity(), itemsList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        URL = ((MyApplication) getActivity().getApplication()).getUrl();
        fetchStoreItems();

        prefs =  this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

        return view;
    }

    /**
     * fetching shopping item by making http call
     */
    private void fetchStoreItems() {
        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getActivity(), "Couldn't fetch the store items! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<Route> items = new Gson().fromJson(response.toString(), new TypeToken<List<Route>>() {
                        }.getType());

                        itemsList.clear();
                        itemsList.addAll(items);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MyApplication instance = MyApplication.getInstance();
        instance.addToRequestQueue(request);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    /**
     * RecyclerView adapter class to render items
     * This class can go into another separate class, but for simplicity
     */
    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {
        private Context context;
        private List<Route> routeList;



        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView name, grade;
            public ImageView thumbnail, overflow, type;

            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.title);
                grade = view.findViewById(R.id.price);
                thumbnail = view.findViewById(R.id.thumbnail);
                overflow = view.findViewById(R.id.overflow);
                //type = view.findViewById(R.id.type);
            }
        }




        public StoreAdapter(Context context, List<Route> routeList) {
            this.context = context;
            this.routeList = routeList;

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.store_item_row, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final Route route = routeList.get(position);
            holder.name.setText(route.getName());
            holder.grade.setText(route.getGrade());

            Glide.with(context)
                    .load(route.getImage())
                    .into(holder.thumbnail);

            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //DetailsFragment fragment = new DetailsFragment();
                    Fragment fragment = DetailsFragment.newInstance(route.getId(), 0);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                    actionBar.setTitle("Route Details");
                }
            });

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(holder.overflow, route.getId());
                }
            });
        }

        private void showPopupMenu(View view, int id) {
            // inflate menu

            PopupMenu popup = new PopupMenu(context, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.men_item, popup.getMenu());
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener(id));
            popup.show();
        }

        class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

            private int selectedId;
            private DatabaseHelper db;



            public MyMenuItemClickListener() {
            }

            public MyMenuItemClickListener(int id) {
                this.selectedId = id;
                db = new DatabaseHelper(getContext());
            }

            private void fetchStoreItems() {
                StringBuilder stringBuilder = new StringBuilder(URL0);
                stringBuilder.append(selectedId);
                URL2 = stringBuilder.toString();

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, URL2, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                //mTextView.setText("Response: " + response.toString());
                                //Toast.makeText(getActivity(), "LOADED", Toast.LENGTH_LONG).show();
                                try {
                                    Route route = new Route();
                                    route.setId((int)response.get("id"));
                                    route.setName((String)response.get("name"));
                                    route.setType((int)response.get("type"));
                                    route.setGrade((String)response.get("grade"));
                                    route.setImage((String)response.get("img"));
                                    route.setLat((Double)response.get("lat"));
                                    route.setLon((Double)response.get("lon"));
                                    long id = db.insertNote(route);
                                    if(id==0)
                                    {
                                        Toast.makeText(getActivity(), "Route already added", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getActivity(), "Added to favourite", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error

                            }
                        });

                MyApplication instance = MyApplication.getInstance();
                instance.addToRequestQueue(jsonObjectRequest);


            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_favourite:
                        //Toast.makeText(context, "Add to favourite", Toast.LENGTH_SHORT).show();
                        fetchStoreItems();
                        return true;
                    case R.id.action_details:
                        //DetailsFragment fragment = new DetailsFragment();
                        Fragment fragment = DetailsFragment.newInstance(selectedId, 0);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                        actionBar.setTitle("Route Details");
                        return true;
                    default:
                }
                return false;
            }
        }

        @Override
        public int getItemCount() {
            return routeList.size();
        }
    }
}
