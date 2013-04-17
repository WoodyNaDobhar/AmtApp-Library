/*
 * Copyright(C) 2011+ Woody NaDobhar
 */	

package com.azuriteWeb.amtApp.Library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import com.google.ads.*;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AmtAppLibrary extends Activity{

	GridView MyGrid;
	ArrayList<Object> book = new ArrayList<Object>();
	ArrayList<ArrayList<Object>> books = new ArrayList<ArrayList<Object>>();
	private File filesDirectory = new File("/");
	private File imagesDirectory = new File("/");
	private static final int DIALOG_GET_NEW_CONENT = 1;
	private static final int DIALOG_SD_NOT_FOUND = 2;
	private AdView adView;
	String currentDate = (String) DateFormat.format("yyyy-MM-dd", new Date());

	//methods adapter
	ApplicationMethods AM;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		//app methods
		AM = new ApplicationMethods(this);

		//if there's an old data folder (an old holdover), nuke it
		/* TODO: nuke this before release to market */
		File rulesDir = new File(Environment.getExternalStorageDirectory().toString()+"/Android/data/AmtApp/");
		nukeOldRules(rulesDir);
		
		//the usual suspects
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//Preferences
		SharedPreferences settings = getSharedPreferences("amtAppLibraryPrefs", 0);
		String lastUpdate = settings.getString("lastUpdate", "1974-11-03");
		
		//ad view
		adView = new AdView(this, AdSize.BANNER, ApplicationMethods.BANNERKEY);

		//look up our LinearLayout
		LinearLayout adLayout = (LinearLayout)findViewById(R.id.linearLayout);
		
		//update our adView adParameters
		LinearLayout.LayoutParams adParameters = new LinearLayout.LayoutParams(320, 50);
		adParameters.gravity = Gravity.CENTER;
		adView.setLayoutParams(adParameters);

		//add the adView to the layout
		adLayout.addView(adView);
		
		//Ad request
		AdRequest adRequest = new AdRequest();
//		adRequest.addTestDevice("0B182972466F4643E1902C65F4B3B3BC");
		adView.loadAd(adRequest);

		//setup the view
		MyGrid = (GridView)findViewById(R.id.MyGrid);
		MyGrid.setAdapter(new ImageAdapter(this));
		
		//make sure there's mounted media to stick it in
		if(!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){

			//if not, we're done here.  Let them know.
			showDialog(DIALOG_SD_NOT_FOUND);
		}else{

			//only do this if you haven't today
			if(!lastUpdate.equals(currentDate)){
				//if it's online, check to see if there's new content
				if(isOnline()){
					if(AM.checkNewContent() == true){
						//ask 'em if they wanna go get the new content
						showDialog(DIALOG_GET_NEW_CONENT);
					}
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("lastUpdate", currentDate);
					editor.commit();
				}
			}
			
			//set our directories
			filesDirectory = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.files));
			filesDirectory.mkdir();
			imagesDirectory = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.images));
			imagesDirectory.mkdir();

			//get our list of files
			File[] files = filesDirectory.listFiles();
//			int fileCount = files.length;

			//get our list of images
			File[] images = imagesDirectory.listFiles();
//			int imageCount = images.length;
			
//			Log.d("fileCount", ""+fileCount);
//			Log.d("imageCount", ""+imageCount);
			
			//go thru our files and make any cover images that need to be made and display everything
			for(File file : files){
				fileImageCheck(file, images);
			}
			
			//our listener
			MyGrid.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					
					//file
					String fileName = (String) books.get(position).get(2);
					
					//if it's real, get to it.
					if(fileName != null){
						Intent myIntent = new Intent(AmtAppLibrary.this, LibraryBookTableOfContents.class);
						myIntent.putExtra("filePath", Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.files)+fileName);
						startActivity(myIntent);
					}
				}
			});
		}
	}

	private void fileImageCheck(File file, File[] images) {
		if(file.isFile()){
			Boolean didFind = false;
			String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
			Log.d("file name", fileName);
			Log.d("images", Arrays.toString(images));
			//go thru the list of images, and let us know if it's in there.
			for(File image : images){
				String imageName = image.getName().substring(0, image.getName().lastIndexOf('.'));
				Log.d("image name", imageName);
				if(fileName.equals(imageName)){
					didFind = true;
				}
			}
			//didn't find it, make the image.
			if(!didFind){
				
				//get our pub and make the image
				EpubReader epubReader = new EpubReader();
				try{
					Book bookTemp = epubReader.readEpub(new FileInputStream(file));
					
					//get the image
					Bitmap coverImage = BitmapFactory.decodeStream(bookTemp.getCoverImage().getInputStream());
					try {
						File destination = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.images),fileName+".png");
						FileOutputStream output = new FileOutputStream(destination);
						coverImage.compress(Bitmap.CompressFormat.PNG, 90, output);
						
						//set our book info
						book.add(coverImage);
						book.add(fileName);
						book.add(file.getName());
						
						//add it to the books array
						ArrayList<Object> bookHolder = new ArrayList<Object>();
						bookHolder.add(0, "");
						bookHolder.add(1, "");
						bookHolder.add(2, "");
						Collections.copy(bookHolder,book);
						books.add(bookHolder);
						book.clear();
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}catch(FileNotFoundException e){
					Log.e("readEpub Not Found",e+"");
				}catch(IOException e){
					Log.e("readEpub IO",e+"");
				}
			}else{
				
				//get the image
				Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.images)+fileName+".png");
				
				//set our book info
				book.add(bitmap);
				book.add(fileName);
				book.add(file.getName());
				
				//add it to the books array
				ArrayList<Object> bookHolder = new ArrayList<Object>();
				bookHolder.add(0, "");
				bookHolder.add(1, "");
				bookHolder.add(2, "");
				Collections.copy(bookHolder,book);
				books.add(bookHolder);
				book.clear();
			}
		}
	}

	public class ImageAdapter extends BaseAdapter{
		
		Context MyContext;
		
		public ImageAdapter(Context _MyContext){
			MyContext = _MyContext;
		}
		
		@Override
		public int getCount(){
			/* Set the number of element we want on the grid */
//			Log.i("Books size:",""+books.size());
			return books.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			
			View MyView = convertView;
			
			if(convertView == null){
				/*we define the view that will display on the grid*/
				
				//Inflate the layout
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.grid_item, null);
				
				//set up our book
				ArrayList<Object> book = books.get(position);
				
				//Add The Text
				TextView tv = (TextView)MyView.findViewById(R.id.grid_item_text);
				String title = (String) book.get(1);
				if(title.length() > 16){
					tv.setText(title.substring(0, 13)+"...");
				}else{
					tv.setText(title);
				}

				//Add The Image
				ImageView iv = (ImageView)MyView.findViewById(R.id.grid_item_image);
				
				//get and resize the image
				Bitmap coverBig = (Bitmap) book.get(0);
				
				//actual width of the image (img is a Bitmap object)
				int width = coverBig.getWidth();
				int height = coverBig.getHeight();

				//new width / height
				int newWidth = 75;
				int newHeight = 95;

				//calculate the scale
				float scaleWidth = (float) newWidth / width;
				float scaleHeight = (float) newHeight / height;

				//create a matrix for the manipulation
				Matrix matrix = new Matrix();

				//resize the bit map
				matrix.postScale(scaleWidth, scaleHeight);

				//recreate the new Bitmap and set it back
				Bitmap cover = Bitmap.createBitmap(coverBig, 0, 0,width, height, matrix, true);
				
				//set it
				iv.setImageBitmap(cover);
			}
			return MyView;
		}

		@Override
		public Object getItem(int arg0){
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0){
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	void nukeOldRules(File rulesDir){
		if(rulesDir.isDirectory()){
			String[] children = rulesDir.list();
			for(int i = 0; i < children.length; i++){
				File temp =  new File(rulesDir, children[i]);
				if(temp.isDirectory()){
//					Log.d("nukeOldRules", "Recursive Call" + temp.getPath());
					nukeOldRules(temp);
				}else{
//					Log.d("DeleteRecursive", "Delete File" + temp.getPath());
					boolean b = temp.delete();
					if(b == false){
						Log.d("DeleteRecursive", "DELETE FAIL");
					}
				}
			}
			rulesDir.delete();
		}
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		AmtAppLibrary.this.startActivity(new Intent(AmtAppLibrary.this, AmtAppLibrary.class));
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.library, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		// Handle item selection
		switch(item.getItemId()){
			case R.id.update_library:
				Intent updateIntent = new Intent(AmtAppLibrary.this, UpdateLibrary.class);
				AmtAppLibrary.this.startActivity(updateIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
		case DIALOG_GET_NEW_CONENT:
			return new AlertDialog.Builder(AmtAppLibrary.this)
				.setTitle(R.string.get_new_content_title)
				.setMessage(getString(R.string.get_new_content))
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						AmtAppLibrary.this.startActivity(new Intent(AmtAppLibrary.this, UpdateLibrary.class));
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						dialog.cancel();
					}
				})
				.create();
		case DIALOG_SD_NOT_FOUND:
			return new AlertDialog.Builder(AmtAppLibrary.this)
			.setMessage(getString(R.string.sd_not_found))
			.setCancelable(false)
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					AmtAppLibrary.this.startActivity(new Intent(AmtAppLibrary.this, AmtAppLibrary.class));
				}
			})
			.create();
		}
		return null;
	}
	
	public boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni!=null && ni.isAvailable() && ni.isConnected()){
			return true;
		}else{
			return false; 
		}
	}
}