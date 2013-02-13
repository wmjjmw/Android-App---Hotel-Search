package com.facebook.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook.DialogListener;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class activity2 extends ListActivity { 
	public static final String APP_ID = "263693123726247";
	final Context context = this;
	private Facebook mFacebook;
	//private AsyncFacebookRunner mAsyncRunner;
	
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main2); 
        mFacebook = new Facebook(APP_ID);
        SessionStore.restore(mFacebook, this);
        //mAsyncRunner = new AsyncFacebookRunner(mFacebook);
        // initialization
        names = new ArrayList<String>();
        images = new ArrayList<String>();
        location = new ArrayList<String>();
        reviews = new ArrayList<String>();
        ratings = new ArrayList<String>();
        reviewURLs = new ArrayList<String>();
        
        // get input Json
        String value = "";
        Bundle extras = getIntent().getExtras(); 
        if(extras !=null){
        	value = extras.getString("json");
        }
        
        // parse Json
        parseJson(value);
        
       
        // set list adapter
        setListAdapter(new TextImageAdapter(this));  
    }  
    
    private void parseJson(String value){
    	JSONObject json;
		try {
			json = new JSONObject(value);
			if(json.has("hotels")){
				JSONObject hotelsObject = json.getJSONObject("hotels");
				if(hotelsObject.has("hotel")){
					JSONArray hotelsArray = hotelsObject.getJSONArray("hotel");
					for(int j = 0; j < hotelsArray.length(); j++){
						JSONObject hotelAtIndex = hotelsArray.getJSONObject(j);
						String hotelName = hotelAtIndex.getString("name");
						hotelName.replaceAll("\\*", "\\,");
						String hotelLocation = hotelAtIndex.getString("location");
						hotelLocation = hotelLocation.replaceAll("\\*", ",");
						String hotelStar = hotelAtIndex.getString("no_of_stars");
						String hotelReview = hotelAtIndex.getString("no_of_reviews");
						String hotelImage = hotelAtIndex.getString("image_url");
						hotelImage = hotelImage.replaceAll("!", ":");
						String hotelReviewUrl = hotelAtIndex.getString("review_url");
						hotelReviewUrl = hotelReviewUrl.replaceAll("!", ":");
						
						// add to arrayList
						names.add(hotelName);
						images.add(hotelImage);
						location.add(hotelLocation);
						reviews.add(hotelReview);
						ratings.add(hotelStar);
						reviewURLs.add("http://www.tripadvisor.com"+hotelReviewUrl);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private Bitmap getImageBitmap(String url) { 
        Bitmap bm = null; 
        try { 
            URL aURL = new URL(url); 
            URLConnection conn = aURL.openConnection(); 
            conn.connect(); 
            InputStream is = conn.getInputStream(); 
            BufferedInputStream bis = new BufferedInputStream(is); 
            bm = BitmapFactory.decodeStream(bis); 
            bis.close(); 
            is.close(); 
       } catch (IOException e) {  
       } 
       return bm; 
    } 

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Log.d("my tag" ,String.format("%d", position));
    	// open new dialog
    	final Dialog dialog = new Dialog(context);
    	dialog.setContentView(R.layout.dialog);
    	 
    	/* set the custom dialog components - text, image and button */
    	// name
    	TextView hotelNameDialog = (TextView) dialog.findViewById(R.id.nameDialog);
    	hotelNameDialog.setText(names.get(position));
    	// image
    	ImageView HotelImageDialog = (ImageView) dialog.findViewById(R.id.imageDialog);
    	HotelImageDialog.setImageBitmap(getImageBitmap(images.get(position)));
    	// review
    	TextView hotelReviewDialog = (TextView) dialog.findViewById(R.id.reviewDialog);
    	hotelReviewDialog.setText("Reviews:"+reviews.get(position));
    	// rating
    	RatingBar hotelRatingDialog = (RatingBar) dialog.findViewById(R.id.ratingBarDialog);
    	float ratingFloat = Float.valueOf(ratings.get(position).trim()).floatValue();
    	hotelRatingDialog.setRating(ratingFloat);
    	
    	params = new Bundle();
    	params.putString("name", names.get(position));
		params.putString("picture", images.get(position));
		params.putString("caption", "check this hotel");
		params.putString("link", reviewURLs.get(position));
		params.putString("description", "The hotel is located at " + location.get(position) + " and has a rating of " + ratings.get(position) + "/5");
    	
		JSONObject prop1 = new JSONObject();
		try {
			prop1.put("text", "here");
			prop1.put("href", reviewURLs.get(position));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject attachment = new JSONObject();
		try {
			attachment.put("Find the hotel reviews", prop1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("1", attachment.toString());
		params.putString("properties",attachment.toString());
    	Button dialogButton = (Button) dialog.findViewById(R.id.postDialog);
    	
    	dialogButton.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			mFacebook = new Facebook(APP_ID);    			
    			mFacebook.dialog(activity2.this, "feed", params, new DialogListener() {

    	            @Override
    	            public void onFacebookError(FacebookError e) {
    	                // TODO Auto-generated method stub

    	            }

    	            @Override
    	            public void onError(DialogError e) {
    	                // TODO Auto-generated method stub

    	            }

    	            @Override
    	            public void onComplete(Bundle values) {
    	                final String postId = values.getString("post_id");
    	                if (postId != null) {
    	                    Log.d("FACEBOOK", "Dialog Success! post_id=" + postId);
    	                    Context context = getApplicationContext();
    	                    CharSequence text = "Post Successful!";
    	                    int duration = Toast.LENGTH_SHORT;

    	                    Toast toast = Toast.makeText(context, text, duration);
    	                    toast.setGravity(Gravity.CENTER, 0, 0);
    	                    toast.show();
    	                } else {
    	                    Log.d("FACEBOOK", "No wall post made");
    	                }
    	                dialog.dismiss();
    	            }

    	            @Override
    	            public void onCancel() {
    	                // TODO Auto-generated method stub

    	            }
    	        });     
    		}
    	});
    	 
    	dialog.show();
	}
    
    
    private class TextImageAdapter extends BaseAdapter{  
        private Context mContext;  
        public TextImageAdapter(Context context) {  
            this.mContext=context;  
        }   
        public int getCount() {  
            return names.size();  
        }  
  
        public Object getItem(int position) {  
            return null;  
        }  
  
        public long getItemId(int position) {  
            return 0;  
        }   
        public View getView(int position, View convertView, ViewGroup parent) {    
            if(convertView==null){  
                convertView=LayoutInflater.from(mContext).inflate(R.layout.main2, null);  
                ItemViewCache viewCache=new ItemViewCache();  
                viewCache.mTextView=(TextView)convertView.findViewById(R.id.text);  
                viewCache.mImageView=(ImageView)convertView.findViewById(R.id.images);  
                viewCache.mLocationView=(TextView)convertView.findViewById(R.id.locationText); 
                convertView.setTag(viewCache);  
            }  
            ItemViewCache cache=(ItemViewCache)convertView.getTag(); 
            // set hotel image
            cache.mImageView.setImageBitmap(getImageBitmap(images.get(position)));
            cache.mImageView.setAdjustViewBounds (true);
            cache.mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // set hotel name
            cache.mTextView.setText(names.get(position));
            // set hotel location
            cache.mLocationView.setText(location.get(position));
            return convertView;  
        }  
    }   
    private static class ItemViewCache{  
        public TextView mTextView;  
        public ImageView mImageView;
        public TextView mLocationView;
    }    
    private ArrayList<String> names;
    private ArrayList<String> images; 
    private ArrayList<String> location;
    private ArrayList<String> reviews;
    private ArrayList<String> reviewURLs;
    private ArrayList<String> ratings;
    Bundle params;
}
