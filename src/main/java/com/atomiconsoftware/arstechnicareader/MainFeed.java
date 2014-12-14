package com.atomiconsoftware.arstechnicareader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainFeed extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    ArrayList headlines, links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);

        final ListView mainListView = (ListView) findViewById(R.id.listView);
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(links.get(position).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mainListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowPosition = (mainListView == null || mainListView.getChildCount() == 0) ? 0 : mainListView.getChildAt(0).getTop();
                swipeContainer.setEnabled(topRowPosition >= 0);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UpdateListView();
                        swipeContainer.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        Spinner categories = (Spinner) findViewById(R.id.spinner);
        categories.setAdapter(ArrayAdapter.createFromResource(this, R.array.Categories, R.layout.support_simple_spinner_dropdown_item));

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        headlines = new ArrayList();
        links = new ArrayList();
        UpdateListView();

        categories.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        UpdateListView();
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void UpdateListView(){
        ListView mainListView = (ListView) findViewById(R.id.listView);
        Spinner categories = (Spinner) findViewById(R.id.spinner);
        String stringUrl = "";

        headlines.clear();
        links.clear();

        try {
            switch (categories.getSelectedItem().toString()){
                case "All":
                    stringUrl = getResources().getString(R.string.all_url);
                    break;
                case "Technology Lab":
                    stringUrl = getResources().getString(R.string.tech_lab_url);
                    break;
                case "Gear and Gadgets":
                    stringUrl = getResources().getString(R.string.gear_gadget_url);
                    break;
                case "Ministry of Innovation":
                    stringUrl = getResources().getString(R.string.ministry_url);
                    break;
                case "Risk Assessment":
                    stringUrl = getResources().getString(R.string.risk_assess_url);
                    break;
                case "Law and Disorder":
                    stringUrl = getResources().getString(R.string.law_disorder_url);
                    break;
                case "Infinite Loop":
                    stringUrl = getResources().getString(R.string.infinite_loop_url);
                    break;
                case "Opposable Thumbs":
                    stringUrl = getResources().getString(R.string.opposable_url);
                    break;
                case "The Scientific Method":
                    stringUrl = getResources().getString(R.string.sci_meth_url);
                    break;
                case "The Multiverse":
                    stringUrl = getResources().getString(R.string.multiverse_url);
                    break;
                case "Cars Technica":
                    stringUrl = getResources().getString(R.string.cars_url);
                    break;
                case "Staff Blogs":
                    stringUrl = getResources().getString(R.string.staff_url);
                    break;
                default:
                    stringUrl = getResources().getString(R.string.all_url);
            }
            URL url = new URL(stringUrl);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            // We will get the XML from an input stream
            xpp.setInput(getInputStream(url), "UTF_8");

            boolean insideItem = false;

            // Returns the type of current event: START_TAG, END_TAG, etc..
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideItem)
                            headlines.add(xpp.nextText()); //extract the headline
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideItem)
                            links.add(xpp.nextText()); //extract the link of article
                    }
                }else if(eventType== XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                    insideItem=false;
                }

                eventType = xpp.next(); //move to next element
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, headlines);

        mainListView.setAdapter(adapter);
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    protected void onItemClickListener(ListView l, View v, int position, long id) {
        Uri uri = Uri.parse(links.get(position).toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}


