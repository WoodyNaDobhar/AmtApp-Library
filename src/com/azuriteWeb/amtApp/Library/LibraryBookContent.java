/*
 * Copyright(C)2011+ Woody NaDobhar
 */	

package com.azuriteWeb.amtApp.Library;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class LibraryBookContent extends Activity{
	
	//our vars
	private static final int DIALOG_SD_NOT_FOUND = 1;
	String filePath;
	int chapterID;
	int headingID;
	boolean wholeChapter;
	ArrayList<String> chapters = new ArrayList<String>();
	private AdView adView;
	
	//methods adapter
	ApplicationMethods AM;
	
	public void onCreate(Bundle savedInstanceState){
		
		//app methods
		AM = new ApplicationMethods(this);
		
		//the usual suspects
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_contents);
		
		//ad view
		adView = new AdView(this, AdSize.BANNER, ApplicationMethods.BANNERKEY);

		//look up our LinearLayout
		LinearLayout adLayout =(LinearLayout)findViewById(R.id.linearLayout);
		
		//update our adView adParameters
		LinearLayout.LayoutParams adParameters = new LinearLayout.LayoutParams(320, 50);
		adParameters.gravity = Gravity.BOTTOM;
		adParameters.gravity = Gravity.CENTER;
		adView.setLayoutParams(adParameters);

		//add the adView to the layout
		adLayout.addView(adView);
		
		//Ad request
		AdRequest adRequest = new AdRequest();
//		adRequest.addTestDevice("0B182972466F4643E1902C65F4B3B3BC");
		adView.loadAd(adRequest);
		
		//get our intent vars
		Bundle extras = getIntent().getExtras();
		if(extras != null && !extras.getString("filePath").equals(null)){
			
			//set the vars
			filePath = extras.getString("filePath");
			chapterID = extras.getInt("chapterID");
			headingID = extras.getInt("headingID");
			wholeChapter = extras.getBoolean("wholeChapter");
			int hCount = 0;
			
			//make sure there's mounted media to stick it in
			if(!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
				//if not, we're done here.	Let them know.
				showDialog(DIALOG_SD_NOT_FOUND);
			}else{
				try{
					
					// read epub file
					EpubReader epubReader = new EpubReader();
					Book book = epubReader.readEpub(new FileInputStream(filePath));
					String data = new String(book.getContents().get(chapterID).getData());
					String dump = new String();
					
					//if it's not 'whole chapter', we've gotta split up the string
					if(headingID != 0){
						
						//adjust for 'whole chapter'
						headingID = headingID - 1;
						
						//parse the data
						Document doc = Jsoup.parse(data);
						
						//get everything from our first h2 down
						Element firstH2 = doc.select("h2").first();
						Elements siblings = firstH2.siblingElements();
						
						//go thru the elements
						for(int i = 1; i < siblings.size(); i++){
							
							//this element
							Element sibling = siblings.get(i);
							
							//is it a h2?
							if(!"h2".equals(sibling.tagName())){
								
								//no?  if it's the right one, add it to our dump string
								if(hCount-1 == headingID){
									dump = dump.concat(sibling.toString());
								}
							}else{
								
								//yes?  Is it the one they've asked for?
								if(hCount == headingID){
									
									//add it to the dump string
									dump = dump.concat(sibling.toString());
								}
								
								//let's not waste time here any longer
								if(hCount > headingID){
									break;
								}
								hCount++;
							}
						}
					}else{
						//I can't believe I ate the whole thing.
						dump = data;
					}

					//set up the webView
					WebView webView =(WebView) findViewById(R.id.webview);
					webView.setBackgroundColor(0);
					String baseUrl="file:/"+filePath;
					webView.loadDataWithBaseURL(baseUrl, dump, "text/html", "utf-8", null);
				}catch(FileNotFoundException e){
					Log.e("readEpub Not Found",e+"");
				}catch(IOException e){
					Log.e("readEpub IO",e+"");
				}
			}
		}
	}
}