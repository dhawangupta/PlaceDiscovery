package com.placediscovery.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.placediscovery.MongoLabUser.User;
import com.placediscovery.MongoLabUser.UserQueryBuilder;
import com.placediscovery.MongoLabUser.UserStatus;
import com.placediscovery.R;
import com.placediscovery.HelperClasses.HelperMethods;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class
        LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    protected EditText _emailText;
    protected EditText _passwordText;
    protected Button _loginButton;
    protected TextView _signupLink;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _signupLink = (TextView) findViewById(R.id.link_signup);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        GetUserAsyncTask task = new GetUserAsyncTask();
        task.execute();
    }

    public void onLoginSuccess(User u) {
        UserStatus.setUserStatus(u);
        Intent moreDetailsIntent = new Intent(LoginActivity.this, HomePageActivity.class);
        Toast.makeText(getApplicationContext(), "Welcome " + u.name +
                ", You are now logged in.", Toast.LENGTH_SHORT).show();
        startActivity(moreDetailsIntent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed, Please try again!!", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if(!HelperMethods.isInternetAvailable(this))
            valid = false;
        return valid;
    }

    private class GetUserAsyncTask extends AsyncTask<User, Void, ArrayList<User>> {
        String server_output = null;
        String temp_output = null;
        @Override
        protected void onPreExecute() {
            // update the UI immediately after the task is executed
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LoginActivity.this, "",
                    "signing in...", false);
        }
        @Override
        protected ArrayList<User> doInBackground(User... arg0) {

            ArrayList<User> users = new ArrayList<>();  //list of all the users in db
            try
            {

                UserQueryBuilder qb = new UserQueryBuilder();
                URL url = new URL(qb.buildUsersGetURL());
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                while ((temp_output = br.readLine()) != null) {
                    server_output = temp_output;
                }

                // create a basic db list
                String mongoarray = "{ artificial_basicdb_list: "+server_output+"}";
                Object o = com.mongodb.util.JSON.parse(mongoarray);


                DBObject dbObj = (DBObject) o;
                BasicDBList usersList = (BasicDBList) dbObj.get("artificial_basicdb_list");

                for (Object obj : usersList) {
                    DBObject userObj = (DBObject) obj;

                    User temp = new User();

                    temp.setUser_id(userObj.get("_id").toString());
                    temp.setName(userObj.get("name").toString());
                    temp.setEmail(userObj.get("email").toString());
                    temp.setPassword(userObj.get("password").toString());
                    temp.setSavedplaces(userObj.get("savedplaces").toString());

                    BasicDBList ratingsList = (BasicDBList) userObj.get("ratings");
                    BasicDBObject[] ratingsArr = ratingsList.toArray(new BasicDBObject[0]);
                    temp.setRatings(ratingsArr);

                    users.add(temp);
                }

            }catch (Exception e) {
                e.getMessage();
            }

            return users;
        }

        protected void onPostExecute(ArrayList<User> returnValues) {
            if(progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();}

            if (returnValues.size() != 0) {
                String email = _emailText.getText().toString();
                String password = _passwordText.getText().toString();
                progressDialog.dismiss();
                for (User x : returnValues) {
                    if (x.email.equals(email) && x.password.equals(password)) {
                        onLoginSuccess(x);
                        break;
                    }
                }

                if(!UserStatus.LoginStatus)
                    Toast.makeText(getApplicationContext(),"Incorrent Username or Password",Toast.LENGTH_SHORT).show();

                _loginButton.setEnabled(true);
            } else{
                onLoginFailed();
            }
        }
    }

}
