package org.javatosf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class RestApplication {
	private static final String clientId 		= "<client Id>"; //3MVG9Y6dy9NBhz.o3MVG9Y6d_Btp4xp6BdUCjTimkI9fQfVvVhjijCchKy9NBhz.o
	private static final String clientSecret 	= "<clientSecret>"; //1236547125478963
	private static final String redirectUri 	= "<redirect URI>"; //https://localhost:8443/RestTest/oauth/_callback2
	private static String tokenUrl 				= "";
	private static final String environment 	= "https://login.salesforce.com";
	private static final String userName		= "<salesforce username>";
	private static final String password		= "<salesforce password>"; // password = password + security token
	private static String accessToken			=	null;
	private static String instanceUrl			=	null;
	
	public static void main(String[] args) throws HttpException, IOException, Exception{
		// TODO Auto-generated method stub
		System.out.println("--------- Getting the access token -------------");
		tokenUrl	=	environment + "/services/oauth2/token"; // Standard REST API Endpoint
		HttpClient httpClient = new HttpClient();
		PostMethod post =	new PostMethod(tokenUrl);
		post.addParameter("grant_type", "password");
		post.addParameter("client_id", clientId);
		post.addParameter("client_secret", clientSecret);
		post.addParameter("redirect_uri",redirectUri);
		post.addParameter("username", userName);
		post.addParameter("password", password);
		try{
			httpClient.executeMethod(post);
			System.out.println(":::: Response Body:"+post.getResponseBodyAsStream());
			JSONObject authResponse	=	new JSONObject(new JSONTokener(new InputStreamReader(post.getResponseBodyAsStream())));
			System.out.println("---- Authentication Response:"+authResponse);
			accessToken		=	authResponse.getString("access_token");
			instanceUrl		=	authResponse.getString("instance_url");
			System.out.println("::::: Access Token:"+accessToken);
			System.out.println("::::: instance Url:"+instanceUrl);
			//new RestApplication().createAccountStandardSFService(instanceUrl,accessToken);
			new RestApplication().updateAccount("0019000001UohevAAB", instanceUrl, accessToken);
			//
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			post.releaseConnection();
		}
	}
	
	// Method to call the own custom service
	private String createAccount(String instanceUrl, String accessToken) throws UnsupportedEncodingException{
		//String accountId;
		HttpClient httpClient = new HttpClient();
		JSONObject account		=	new JSONObject();
		try{
			account.put("name", "JAVA to SF FROM Lap");
			account.put("email", "kalebraju23@gmail.com");
			account.put("website","https://login.salesforce.com");
			
		} catch(JSONException ex){
			
		}
		
		PostMethod post	=	new PostMethod(instanceUrl+"/services/apexrest/restapicalls");
		post.setRequestHeader("Authorization", "OAuth "+accessToken);
		post.setRequestEntity(new StringRequestEntity(account.toString(),"application/json",null));
		try{
			httpClient.executeMethod(post);
			System.out.println("HTTP Status:"+post.getStatusCode());
			if(post.getStatusCode() != 1){
				System.out.println("::: Response from SF:"+post.getResponseBodyAsString());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	private String createAccountStandardSFService(String instanceUrl, String accessToken)throws Exception{
		HttpClient httpClient 	= 	new HttpClient();
		JSONObject account		=	new JSONObject();
		PostMethod post	=	new PostMethod(instanceUrl+"/services/data/v20.0/sobjects/Account/");
		String accountId = "";
		try{
			account.put("name", "Account From Standard Service");
			account.put("AccountNumber", "SBIN1234");
			post.setRequestHeader("Authorization", "OAuth "+accessToken);
			post.setRequestEntity(new StringRequestEntity(account.toString(),"application/json",null));
			httpClient.executeMethod(post);
			System.out.println("Http Status"+post.getStatusCode()+" creating account\n\n");
			if(post.getStatusCode() != 400){
				try{
					JSONObject response	=	new JSONObject(new JSONTokener(new InputStreamReader(post.getResponseBodyAsStream())));
					System.out.println("Create Response:"+response.toString(2));
					if(response.getBoolean("Success")){
						accountId = response.getString("id");
						System.out.println(":::: New Record id:"+accountId+"\n\n");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	private void showAccount(String accountId, String instanceUrl, String accessToken) throws Exception{
		HttpClient httpClient	=	new HttpClient();
		GetMethod get			=	new GetMethod(instanceUrl+"/services/data/v20.0/sobjects/Account/"+accountId);
		get.setRequestHeader("Authorization","OAuth "+accessToken);
		
		try{
			
			System.out.println(":: Response:"+httpClient.executeMethod(get));
			if(get.getStatusCode()==HttpStatus.SC_OK){
				try{
					JSONObject response	=	new JSONObject(new JSONTokener(new InputStreamReader(get.getResponseBodyAsStream())));
					System.out.println(":::: Query Response:"+response.toString(2));
					Iterator iterator = response.keys();
					while(iterator.hasNext()){
						String key	=	(String)iterator.next();
						String value	=	response.getString(key);
						System.out.println(key+":"+(value!=null ? value :"")+"\n");
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			get.releaseConnection();
		}
	}
	
	private void updateAccount(String accountId, String instanceUrl, String accessToken) throws Exception{
		HttpClient httpClient	=	new HttpClient();
		PostMethod get			=	new PostMethod(instanceUrl+"/services/data/v20.0/sobjects/Account/"+accountId){
			@Override
			public String getName(){
				return "PATCH";
			}
		};
		//get.setRequestHeader("Authorization","OAuth "+accessToken);
		try{
			JSONObject account		=	new JSONObject();
			account.put("name", "Account From Standard Service Updated");
			account.put("Site","https://login.salesforce.com");
			get.setRequestHeader("Authorization", "OAuth "+accessToken);
			get.setRequestEntity(new StringRequestEntity(account.toString(),"application/json",null));
			httpClient.executeMethod(get);
			JSONObject response	=	new JSONObject(new JSONTokener(new InputStreamReader(get.getResponseBodyAsStream())));
			System.out.println(":::: Response from SF:"+response);
			/*
			if(get.getStatusCode()==HttpStatus.SC_OK){
				try{
					JSONObject response	=	new JSONObject(new JSONTokener(new InputStreamReader(get.getResponseBodyAsStream())));
					System.out.println(":::: Query Response:"+response.toString(2));
					Iterator iterator = response.keys();
					while(iterator.hasNext()){
						String key	=	(String)iterator.next();
						String value	=	response.getString(key);
						System.out.println(key+":"+(value!=null ? value :"")+"\n");
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			get.releaseConnection();
		}
	}
	
	
	
}
