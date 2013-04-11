/*
 * Copyright(C)2011+ Woody NaDobhar
 */	

package com.azuriteWeb.amtApp.Library;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class LibraryBookTableOfContents extends ExpandableListActivity{
	
	//our vars
	private static final int DIALOG_SD_NOT_FOUND = 1;
	String filePath;
	ArrayList<String> chapters = new ArrayList<String>();
	private AdView adView;
	
	//methods adapter
	ApplicationMethods AM;
	
	public void onCreate(Bundle savedInstanceState){
		
		//app methods
		AM = new ApplicationMethods(this);
		
		//the usual suspects
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_toc);
		
		//ad view
		adView = new AdView(this, AdSize.BANNER, ApplicationMethods.BANNERKEY);

		//look up our LinearLayout
		LinearLayout adLayout = (LinearLayout)findViewById(R.id.linearLayout);
		
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
			
			//make sure there's mounted media to stick it in
			if(!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
				//if not, we're done here.	Let them know.
				showDialog(DIALOG_SD_NOT_FOUND);
			}else{
				try{
					
					// read epub file
					EpubReader epubReader = new EpubReader();
					Book book = epubReader.readEpub(new FileInputStream(filePath));

					//set the book's title
					TextView tvTitle =(TextView)findViewById(R.id.toc_title);
					tvTitle.setText(book.getTitle());

					//set the list adapater
					SimpleExpandableListAdapter expListAdapter =
						new SimpleExpandableListAdapter(
							this,
							createGroupList(book),			  // Creating group List.
							R.layout.book_toc_group_row,	// Group item layout XML.
							new String[] { "chapters" },  // the key of group item.
							new int[] { R.id.row_name },	// ID of each group item.-Data under the key goes into this TextView.
							createChildList(book),			  // childData describes second-level entries.
							R.layout.book_toc_child_row,	// Layout for sub-level entries(second level).
							new String[] {"subChapters"},	  // Keys in childData maps to display.
							new int[] { R.id.grp_child}	 // Data under the keys above go into these TextViews.
						);
					setListAdapter(expListAdapter);	   // setting the adapter in the list.
				}catch(FileNotFoundException e){
					Log.e("readEpub Not Found",e+"");
				}catch(IOException e){
					Log.e("readEpub IO",e+"");
				}
			}
		}
	}

	//create groups
	private List<HashMap<String, String>> createGroupList(Book book){
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		int i = 0;
		for(TOCReference tocReference : book.getTableOfContents().getTocReferences()){
			i++;
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("chapters", AM.toRoman(i)+". "+tocReference.getTitle()); //the key and it's value.
			result.add(m);
		}
		return(List<HashMap<String, String>>)result;
	}
	
	//create children
	private List<ArrayList<HashMap<String, String>>> createChildList(Book book){

		ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();
		List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();
		Iterator<TOCReference> tocReference = tocReferences.iterator();
		while (tocReference.hasNext()) {
			ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
			TOCReference ourRef = tocReference.next();
			List<TOCReference> childrenList = ourRef.getChildren();
			Iterator<TOCReference> children = childrenList.iterator();
			while(children.hasNext()){
				TOCReference ourChild = children.next();
				HashMap<String, String> child = new HashMap<String, String>();
				child.put("subChapters", ourChild.getTitle());
				secList.add(child);
			}
			result.add( secList );
		}
		return result;
	}
	
	public void onContentChanged(){
		System.out.println("onContentChanged");
		super.onContentChanged();
	}
 
	//what to do when a group is clicked
	public void onGroupExpand(int groupPosition){
		try{
			 System.out.println("Group exapanding Listener => groupPosition = " + groupPosition);
		}catch(Exception e){
			System.out.println(" groupPosition Errrr +++ " + e.getMessage());
		}
	}
	
	//what to do when a child is clicked
	public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id){
		System.out.println("Inside onChildClick at groupPosition = " + groupPosition +" Child clicked at position " + childPosition);
		Intent readIntent = new Intent(LibraryBookTableOfContents.this, LibraryBookContent.class);
		readIntent.putExtra("filePath", filePath);
		readIntent.putExtra("chapterID", groupPosition);
		readIntent.putExtra("headingID", childPosition);
//		readIntent.putExtra("wholeChapter", true);
		LibraryBookTableOfContents.this.startActivity(readIntent);
		return true;
	}
}