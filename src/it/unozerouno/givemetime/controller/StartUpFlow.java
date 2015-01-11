package it.unozerouno.givemetime.controller;

import com.google.android.gms.internal.db;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DialerFilter;
import it.unozerouno.givemetime.controller.fetcher.DatabaseManager;
import it.unozerouno.givemetime.controller.fetcher.DatabaseManager.Results;
import it.unozerouno.givemetime.model.UserKeyRing;
import it.unozerouno.givemetime.utils.AsyncTaskWithListener;
import it.unozerouno.givemetime.utils.GiveMeLogger;
import it.unozerouno.givemetime.view.utilities.ApiLoginInterface;

/**
 * This class contains the startUp flow the application follow everytime it startsup
 * @author Edoardo Giacomello
 *
 */
public final class StartUpFlow extends Fragment{
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		runCompleteFlow();
	}

	
	//TODO: Complete the startup flow.
	
	public void runCompleteFlow() {
		//For some step it is mandatory to be executed synchronously
			//1-Log the user
		try{
			GiveMeLogger.log("Starting Startup Flow");
		   login();
		}
		  catch (Exception e) {
			GiveMeLogger.log("Cannot Login: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * User Login. Flow continues at onActivityResult
	 */
	private void login(){
		Intent loginIntent = new Intent(this.getActivity(),ApiLoginInterface.class);
    	startActivityForResult(loginIntent, ApiLoginInterface.RequestCode.LOGIN);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
	 	if (requestCode == ApiLoginInterface.RequestCode.LOGIN && resultCode == ApiLoginInterface.ResultCode.DONE){
	 		//The user is successfully logged. Proceeding with next stage:
	 		flow();
       	}
	}
	
	
	private void flow(){
		//2-Check if all crucial variables are set properly [UserKeyRing, DB, etc]
		if(!UserKeyRing.checkVariables(getActivity())){
			//If here, something went terribly wrong. Resetting application and showing tutorial again will restore crucial variables
			UserKeyRing.setFirstLogin(getActivity(), true);
			GiveMeLogger.log("A variable is missing! Reloading app");
			getActivity().finish();
			
		}    	
	    
		//These stages can be performed Asynchronously
    	
		//3-Db Synchronization (Fetch from Gcalendar)
		GiveMeLogger.log("DB Synchronization Started");
		AsyncTask<Void, Void, Boolean> synchronizer = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				return DatabaseManager.synchronize(getActivity());
			}
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (result) GiveMeLogger.log("DB Syncronization OK");
				else GiveMeLogger.log("DB Syncronization FAILED");
			}
		}.execute();
		
    	
    	
		//4-Collect new data from service (questions, etc)
		
    	
    	
		//5-If not already running, start the service
		
    	
    	
		//6-Propose service collected questions
    	
    	
    	
    	//0: Done
		GiveMeLogger.log("GiveMeTime Ready");				
	}
	
	
	
	
	public class FlowErrorException extends RuntimeException {
		private static final long serialVersionUID = 2617564239734342535L;
		public FlowErrorException(String detail) {
			GiveMeLogger.log("Startup Error: " + detail);
			}
	}
}