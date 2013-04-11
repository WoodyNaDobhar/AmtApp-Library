/*
 * Copyright(C) 2011+ Woody NaDobhar
 */	

package com.azuriteWeb.amtApp.Library;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLfunctions{
	
	public static String getXML(String URL){

		String xml = null;

		try{
			//vars
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(URL);
			
			//get xml
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);
		}catch(UnsupportedEncodingException e){
			Log.e("UnsupportedEncodingException","Can't connect to server");
		}catch(MalformedURLException e){
			Log.e("MalformedURLException","Can't connect to server");
		}catch(IOException e){
			Log.e("IOException","Can't connect to server");
		}

		return xml;
	}
	
	public static Document XMLfromString(String xml){

		//vars
		Document docFromXML = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			docFromXML = (Document) db.parse(is);
		}catch(ParserConfigurationException e){
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		}catch(SAXException e){
			System.out.println("Wrong XML file structure: " + e.getMessage());
			return null;
		}catch(IOException e){
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}

		return docFromXML;

	}
	
	public final static String getElementValue( Node elem ){
		 Node kid;
		 if( elem != null){
			 if (elem.hasChildNodes()){
				 for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
					 if( kid.getNodeType() == Node.TEXT_NODE  ){
						 return kid.getNodeValue();
					 }
				 }
			 }
		 }
		 return "";
	 }

	public static String getValue(Element item, String string){		
		NodeList n = item.getElementsByTagName(string);		
		return XMLfunctions.getElementValue(n.item(0));
	}
}
