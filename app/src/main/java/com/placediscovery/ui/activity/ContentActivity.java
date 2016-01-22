package com.placediscovery.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.placediscovery.ImageLoader.ImageLoader;
import com.placediscovery.MongoLabPlace.Place;
import com.placediscovery.MongoLabPlace.PlaceQueryBuilder;
import com.placediscovery.MongoLabPlace.UpdatePlaceAsyncTask;
import com.placediscovery.MongoLabUser.UpdateUserAsyncTask;
import com.placediscovery.MongoLabUser.User;
import com.placediscovery.MongoLabUser.UserQueryBuilder;
import com.placediscovery.MongoLabUser.UserStatus;
import com.placediscovery.R;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ContentActivity extends AppCompatActivity implements
        BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    String selectedCity;
    private SliderLayout mDemoSlider;   //this is imageslider used
    private RatingBar ratingBar;
    User loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        UserStatus userStatus = new UserStatus();
        loggedInUser = new User(userStatus);

        mDemoSlider = (SliderLayout)findViewById(R.id.contentPageImageSlider);

        //Following is the upper toolbar code which is not needed for now.
        /*
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My title");     //title for the toolbar
        */

        Intent intent = getIntent();
        final int imageviewId = intent.getExtras().getInt("imageviewId");
        final ArrayList<Place> places = (ArrayList<Place>) intent.getExtras()
                .getSerializable("placesObject");
        selectedCity = intent.getExtras().getString("selectedCity");

        final Place selectedPlace = places.get(imageviewId);
        String place_name = selectedPlace.getName();
        String place_content = selectedPlace.getContent();
        String image_url = selectedPlace.getImageURL();
        final double currentRating = Double.parseDouble(selectedPlace.getAverageRating());
        final int currentCount = Integer.parseInt(selectedPlace.getCount());
        String timings = selectedPlace.getTimings();
        String ticket = selectedPlace.getTicket();
        String bestTime = selectedPlace.getBestTime();
        String toDo = selectedPlace.getToDo();


        TextView t1 = (TextView)findViewById(R.id.place_name);
        TextView t2 = (TextView)findViewById(R.id.currentratingtext);
        TextView t3 = (TextView)findViewById(R.id.place_content);
        LinearLayout timingsLayout = (LinearLayout)findViewById(R.id.timings);
        TextView timingsValue = (TextView)findViewById(R.id.timingsValue);
        LinearLayout ticketLayout = (LinearLayout)findViewById(R.id.ticket);
        TextView ticketValue = (TextView)findViewById(R.id.ticketValue);
        LinearLayout bestTimeLayout = (LinearLayout)findViewById(R.id.bestTime);
        TextView bestTimeValue = (TextView)findViewById(R.id.bestTimeValue);
        LinearLayout toDoLayout = (LinearLayout)findViewById(R.id.toDo);
        TextView toDoValue = (TextView)findViewById(R.id.toDoValue);

        t1.setText(place_name);
        t2.setText(currentRating+"/5");
        t3.setText(Html.fromHtml(place_content));
        if(timings.equals(""))
            timingsLayout.setVisibility(LinearLayout.GONE);
        else
            timingsValue.setText(" "+timings);

        if(ticket.equals(""))
            ticketLayout.setVisibility(LinearLayout.GONE);
        else
            ticketValue.setText(" "+ticket);

        if(bestTime.equals(""))
            bestTimeLayout.setVisibility(LinearLayout.GONE);
        else
            bestTimeValue.setText(" "+bestTime);

        if(toDo.equals(""))
            toDoLayout.setVisibility(LinearLayout.GONE);
        else
            toDoValue.setText(" "+toDo);

        String[] image_urls = image_url.split(",");

        if(image_urls.length<=1) {
            // following is old imageloader code
            int loader = R.drawable.loader;         //loader image
            String hd_url = image_url.substring(0, image_url.length() - 6) + ".jpg";
            // ImageLoader class instance
            ImageLoader imgLoader = new ImageLoader(getApplicationContext());
            // whenever you want to load an image from url
            // call DisplayImage function
            // url - image url to load
            // loader - loader image, will be displayed before getting image
            // image - ImageView
            imgLoader.DisplayImage(hd_url, loader, (ImageView) findViewById(R.id.contentPageImage));

        } else {


            for (String url : image_urls) {
                DefaultSliderView textSliderView = new DefaultSliderView(this);
                // initialize a SliderLayout
                textSliderView
                        .image(url.substring(0, url.length() - 6) + ".jpg")      //for higher quality '_n' was removed from url
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);

                //when you want to add your extra information
//            textSliderView.bundle(new Bundle());
//            textSliderView.getBundle()
//                    .putString("extra",name);

                mDemoSlider.addSlider(textSliderView);
            }
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Stack);       //replace "Stack" by other transformers to implement different kind of slider animations
            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.setDuration(4000);
            mDemoSlider.addOnPageChangeListener(this);
        }

        //some toolbar code
        /*
        Toolbar topToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        */

//        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
//
//        /*
//        * This is the listener for rating bar, edit it to change functionality
//        * */
//        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//
//                //TODO: add for rating by user and also change to suitable UI for Rating
//                if (UserStatus.isLoginStatus()) {
//                    int newCount = currentCount + 1;
//                    double newRating = (currentRating * currentCount + rating) / newCount;
//                    selectedPlace.setCount(String.valueOf(newCount));
//                    selectedPlace.setAverageRating(String.valueOf(newRating));
//
//                    UpdatePlaceAsyncTask tsk = new UpdatePlaceAsyncTask(selectedCity);
//                    tsk.execute(selectedPlace);
//
//                    UpdateUserRatingAsyncTask task = new UpdateUserRatingAsyncTask();
//                    task.execute(loggedInUser,selectedPlace.getPlace_id(),Float.toString(rating));
//
//                    Toast.makeText(ContentActivity.this, "New Rating: " + rating,
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(ContentActivity.this, "Please Login", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserStatus.isLoginStatus()) {

                    /*TODO: place_id or name of place (we have to devise a method to display saved places in SavedPlaces.java
                    * TODO: check for adding same place multiple items
                    *
                    * */
                    loggedInUser.setSavedplaces(loggedInUser.getSavedplaces() + "," + places.get(imageviewId).getPlace_id());

                    UpdateUserAsyncTask tsk = new UpdateUserAsyncTask();
                    tsk.execute(loggedInUser);

                    Toast.makeText(ContentActivity.this, "Added to List", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ContentActivity.this, "Please login first", Toast.LENGTH_LONG).show();
                }
            }

        });



    }

    class UpdateUserRatingAsyncTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            User user = (User) params[0];
            String place_id = (String) params[1];
            String rating = (String) params[2];

            try {

                UserQueryBuilder qb = new UserQueryBuilder();
                URL url = new URL(qb.buildUsersUpdateURL(user.getUser_id()));
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("Accept", "application/json");

                OutputStreamWriter osw = new OutputStreamWriter(
                        connection.getOutputStream());

                osw.write(qb.addNewRatingbyUser(place_id,rating));
                osw.flush();
                osw.close();
                return connection.getResponseCode() < 205;
            } catch (Exception e) {
                e.getMessage();
                return false;
            }

        }

    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {

    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

}
