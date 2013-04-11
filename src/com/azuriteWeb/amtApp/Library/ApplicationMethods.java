/*
 * Copyright(C) 2011+ Woody NaDobhar
 */	

package com.azuriteWeb.amtApp.Library;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ApplicationMethods extends Application{

	Context context;
	private ArrayList<String> onlineFileList = new ArrayList<String>();
	private ArrayList<String> localFileList = new ArrayList<String>();
	public static final String BANNERKEY = "a14f835d24e06e9";
	
	@Override
	public void onCreate(){
		super.onCreate();
	}

	public ApplicationMethods(Context ctx){
		context = ctx;
	}

	public Boolean checkNewContent(){
		
		//start with the online stuff
		//get our list of files
		String xml = XMLfunctions.getXML(context.getResources().getString(R.string.library_listing_addy));
		Document doc = XMLfunctions.XMLfromString(xml);

		//our files
		/*TODO: make this a little more distinguishing than 'all the files' */ 
		NodeList nl = doc.getElementsByTagName("file");
		 
		// looping through all item nodes
		for(int i = 0; i < nl.getLength(); i++){
			Element e =(Element)nl.item(i);
			onlineFileList.add(XMLfunctions.getValue(e, "name"));
		}
		
//		Log.i("online file list size:","test:"+onlineFileList.get(0));
		
		//now the local stuff
		//data root
		File dataRoot = new File(Environment.getExternalStorageDirectory().toString()+context.getResources().getString(R.string.sd_files_directory)+context.getResources().getString(R.string.files));
		
		//make it if it doesn't exist
		if(!dataRoot.exists()){
			dataRoot.mkdirs();
		}
		
		//get the files therein
		getLocalFiles(dataRoot);
//		Log.i("local file list size:","test:"+localFileList.size());

		//if the internet list is bigger than the local list, ask them if they wanna dl the new content
		if(onlineFileList.size() > localFileList.size()){
			return true;
		}else{
			//check each file by file size
			for(int i=0; i<nl.getLength(); i++){
				
				//our online file info
				Element e =(Element)nl.item(i);
				
				//local file info
//				Log.i("file path:", Environment.getExternalStorageDirectory().toString()+context.getResources().getString(R.string.sd_files_directory)+context.getResources().getString(R.string.files)+XMLfunctions.getValue(e, "name"));
				File f = new File(Environment.getExternalStorageDirectory().toString()+context.getResources().getString(R.string.sd_files_directory)+context.getResources().getString(R.string.files)+XMLfunctions.getValue(e, "name"));

				int onlineFileSize = Integer.parseInt(XMLfunctions.getValue(e, "size"));
//				Log.i("online file size:","test:"+onlineFileSize);
				
				int localFileSize =(int) f.length();
//				Log.i("local file size:","test:"+localFileSize);
				
				if(localFileSize != onlineFileSize){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void getLocalFiles(File file){

		if(file.isFile()){
			
			//log it
			localFileList.add(file.getName());
		}else if(file.isDirectory()){
			
			//get the files in this directory
			File[] listOfFiles = file.listFiles();
			System.out.print(listOfFiles);
			if(listOfFiles!=null){
				for(int i2=0; i2<listOfFiles.length; i2++){
					getLocalFiles(listOfFiles[i2]);
				}
			}
		}else{
			Log.i("File Error:", "Access Denied(I think)");
		}
	}
	
	//Checks whether the entered Arabic numeral is valid
	public boolean isValidArabic(int x){
		String num = String.valueOf(x);

		//Checks each character if it is a digit.
		for(int k = 0; k < num.length(); k++){
			if(Character.isDigit(num.charAt(k)) == false){
				return false;
			}
		}

		//Checking if the number is bigger than 3999 or smaller than 1
		if(x > 3999 ||x < 1){
			return false;
		}
		return true;
	}
	
	//Returns a string containing the entered Arabic numeral in Roman numeral form.
	public String toRoman(int num){

		if(isValidArabic(num)){  //	   Checking if the number is a valid Arabic number

			String Roman = ""; //	   This will be our result string.

			//	  Declare and Initiate our Arrays
			String onesArray[] ={"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
			String tensArray[] ={"X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
			String hundredsArray[] ={"C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};

			//	  Get the ones in the number
			int ones = num % 10;

			//	  Get the tens
			num =(num - ones) / 10;
			int tens = num % 10;

			//	  Get the hundreds
			num =(num - tens) / 10;
			int hundreds = num % 10;

			//	  Get and write the thousands in the number to our string
			num =(num - hundreds) / 10;
			for(int i = 0; i < num; i++){
				Roman += "M";
			}

			//	  Write the hundreds
			if(hundreds >= 1){
				Roman += hundredsArray[hundreds - 1];
			}

			//	  Write the tens
			if(tens >= 1){
				Roman += tensArray[tens - 1];
			}

			//	  And finally, write the ones
			if(ones >= 1){
				Roman += onesArray[ones - 1];
			}

			//	  Return our string.
			return String.valueOf(Roman);
		}else{
			return null;
		}
	}
}