/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;

public class Example extends Activity {

    // Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    public static final String APP_ID = "263693123726247";

    private LoginButton mLoginButton;
    private TextView mText;
    private TextView mIntroText;
    private Button mSearchButton;
    private EditText mCityTextField;
    private Spinner mHotelChainSpinner;

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    /* get search result*/
    public void getSearchResult(String cityInput, String hotelInput){
    	cityInput = cityInput.replaceAll(" ", "+");
    	String urlString = "http://cs-server.usc.edu:29568/hw8/hello?cityName="+cityInput+"&hotelChain="+hotelInput;
    	try {
			URL CGIURL = new URL(urlString);
			InputStream CGIStream = CGIURL.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(CGIStream));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			} 
			br.close();
			
			// find search result length
			int resultNum = 0;
			JSONObject json;
			try {
				json = new JSONObject(sb.toString());
				if(json.has("hotels")){
					JSONObject hotelsObject = json.getJSONObject("hotels");
					if(hotelsObject.has("hotel")){
						JSONArray hotelsArray = hotelsObject.getJSONArray("hotel");
						resultNum = hotelsArray.length();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			// if no search result
			if (resultNum == 0){
				AlertDialog.Builder builder = new AlertDialog.Builder(Example.this);
                builder.setMessage("No hotel found, try again.")
                       .setCancelable(false)
                       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                           }
                       });
                AlertDialog alert = builder.create();
                alert.show();
		    }else{
			
			Intent i = new Intent(this, activity2.class);
			i.putExtra("json", sb.toString());  
			startActivityForResult(i, 0);
		    }
			 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }
        
        setContentView(R.layout.main);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mText = (TextView) Example.this.findViewById(R.id.txt);
        mIntroText = (TextView) Example.this.findViewById(R.id.introText);
        mCityTextField = (EditText) findViewById(R.id.CityTextName);
        mHotelChainSpinner = (Spinner)findViewById(R.id.hotelChainText);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.hotelChainArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHotelChainSpinner.setAdapter(adapter);
        mSearchButton = (Button) findViewById(R.id.searchButton);

       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);

        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(this, mFacebook);
        
		       
        mSearchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	// get user input
            	String cityInput = mCityTextField.getText().toString(); 
            	String hotelInput = mHotelChainSpinner.getSelectedItem().toString(); 
            	// test if city name is none
            	if (cityInput.compareTo("") == 0){
            		AlertDialog.Builder builder = new AlertDialog.Builder(Example.this);
                    builder.setMessage("Please enter a city name")
                           .setCancelable(false)
                           .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                               }
                           });
                    AlertDialog alert = builder.create();
                    alert.show();
            	}
            	else{
            		getSearchResult(cityInput, hotelInput);
            	}
            }
        });
        
        // set invisible if session not valid
        mCityTextField.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
        mHotelChainSpinner.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
        mSearchButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
        mIntroText.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            mCityTextField.setVisibility(View.VISIBLE);
            mHotelChainSpinner.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);
            mIntroText.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
            mCityTextField.setVisibility(View.INVISIBLE);
            mHotelChainSpinner.setVisibility(View.INVISIBLE);
            mSearchButton.setVisibility(View.INVISIBLE);
            mIntroText.setVisibility(View.INVISIBLE);
            mSearchButton.setVisibility(View.INVISIBLE);
        }
    }
}
