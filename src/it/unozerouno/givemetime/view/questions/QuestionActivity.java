package it.unozerouno.givemetime.view.questions;

import it.unozerouno.givemetime.R;
import it.unozerouno.givemetime.controller.fetcher.DatabaseManager;
import it.unozerouno.givemetime.model.events.EventInstanceModel;
import it.unozerouno.givemetime.model.events.EventListener;
import it.unozerouno.givemetime.model.places.PlaceModel;
import it.unozerouno.givemetime.model.questions.FreeTimeQuestion;
import it.unozerouno.givemetime.model.questions.LocationMismatchQuestion;
import it.unozerouno.givemetime.model.questions.OptimizingQuestion;
import it.unozerouno.givemetime.model.questions.QuestionModel;
import it.unozerouno.givemetime.view.questions.fragments.FreeTimeFragment;
import it.unozerouno.givemetime.view.questions.fragments.MissingDataFragment;
import it.unozerouno.givemetime.view.questions.fragments.FreeTimeFragment.OnFreeTimeQuestionResponse;
import it.unozerouno.givemetime.view.questions.fragments.LocationMismatchFragment;
import it.unozerouno.givemetime.view.questions.fragments.LocationMismatchFragment.OnLocationMismatchQuestionResponse;
import it.unozerouno.givemetime.view.questions.fragments.MissingDataFragment.OnOptimizingQuestionResponse;
import it.unozerouno.givemetime.view.utilities.OnDatabaseUpdatedListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.widget.Toast;

public class QuestionActivity extends ActionBarActivity implements OnLocationMismatchQuestionResponse, OnFreeTimeQuestionResponse, OnOptimizingQuestionResponse{
	QuestionModel question;
	FreeTimeQuestion freeTimeQuestion;
	LocationMismatchQuestion locationMismatchQuestion;
	OptimizingQuestion optimizingQuestion;
	PlaceModel questionPlace;
	EventInstanceModel questionEvent;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.question_layout);
		super.onCreate(savedInstanceState);
		//If it's coming from a notification, the intent will contain a long Id
		Long lQuestionId = getIntent().getLongExtra(QuestionModel.QUESTION_ID, -1);
		//If it's coming from a stored notification, the intent will contain an int id.
		int iQuestionId = getIntent().getIntExtra(QuestionModel.QUESTION_ID, -1);
		//Getting the question
		iQuestionId = (iQuestionId != -1) ? iQuestionId : lQuestionId.intValue();
		question = DatabaseManager.getQuestion(this, iQuestionId);
		//Now we can fetch the event
		if(question!=null){
			//Setting time range for getting instances
			final Time genTime = question.getGenerationTime();
			long fiveMinutes = (long)1000*(long)60*(long)5;
			long aMonth = (long)1000*(long)60*(long)60*(long)24*(long)30;
			Time start =new Time();
			Time end =new Time();
			start.set(genTime.toMillis(false) - fiveMinutes);
			end.set(genTime.toMillis(false) + aMonth);
			DatabaseManager.getEventsInstances(question.getEventId(), start, end, this, new EventListener<EventInstanceModel>() {
				EventInstanceModel resultEvent = null;
				@Override
				public void onLoadCompleted() {
					questionEvent = resultEvent;
					
					
					loadQuestionProperties();
				}
				
				@Override
				public void onEventCreation(EventInstanceModel newEvent) {
					//We'll get the instance that is closest to the question generation
					if (resultEvent == null) resultEvent = newEvent;
					else {
						long newTimeDistance = newEvent.getStartingTime().toMillis(false) - genTime.toMillis(false);
						long oldTimeDistance = resultEvent.getStartingTime().toMillis(false) - genTime.toMillis(false);
						if (Math.abs(newTimeDistance)<Math.abs(oldTimeDistance)){
							resultEvent = newEvent; 
					}
					}
				}
				
				@Override
				public void onEventChange(EventInstanceModel newEvent) {
				}
			});
			
		}
	}
	/**
	 * Called when both question and event is loaded, it fills the model
	 */
	protected void loadQuestionProperties() {
		if (question instanceof FreeTimeQuestion){
			freeTimeQuestion = (FreeTimeQuestion) question;
			freeTimeQuestion.setClosestEvent(questionEvent);
			freeTimeFlow();
		}
		if (question instanceof OptimizingQuestion){
			optimizingQuestion = (OptimizingQuestion) question;
			optimizingQuestion.setEvent(questionEvent.getEvent());
			optimizingFlow();
		}
		if (question instanceof LocationMismatchQuestion){
			locationMismatchQuestion = (LocationMismatchQuestion) question;
			locationMismatchQuestion.setEvent(questionEvent);
			//Now we have fetched the question related event. We still need to get its location:
			locationMismatchQuestion.buildPlaceModel(new OnDatabaseUpdatedListener<PlaceModel>() {
				
				@Override
				protected void onUpdateFinished(PlaceModel questionPlaceModel) {
					//Only when we have also the PlaceModel we can continue with the flow
					questionPlace = questionPlaceModel;
					locationMismatchFlow();
				}
			});
		}
	}
	/**
	 * This is the flow when the question is for a location Mismatch
	 */
	private void locationMismatchFlow(){
		if(questionPlace == null){
			//Something gone wrong with reverse geocoding
			Toast.makeText(getApplicationContext(), "No suitable Places near your location", Toast.LENGTH_LONG).show();
		}
		LocationMismatchFragment locationMismatchFragment = new LocationMismatchFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.question_screen_container,locationMismatchFragment,"locationMismatchFragment").commit();
	}
	
	/**
	 * This is the flow when the question is for a freeTime suggestion
	 */
	private void freeTimeFlow(){
		FreeTimeFragment freeTimeFragment = new FreeTimeFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.question_screen_container,freeTimeFragment,"freeTimeFragment").commit();
	
	}
	
	/**
	 * This is the flow when the question is for a missing data event (OptimizingQuestion)
	 */
	private void optimizingFlow(){
		MissingDataFragment missingDataFragment = new MissingDataFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.question_screen_container,missingDataFragment,"missingDataFragment").commit();
	
	}
	
	@Override
	public FreeTimeQuestion loadFreeTimeQuestion() {
		if(question instanceof FreeTimeQuestion) return (FreeTimeQuestion) question;
		finish();
		return null;
	}
	@Override
	public void onUpdateClicked(FreeTimeQuestion question) {
		DatabaseManager.getInstance(getApplicationContext());
		DatabaseManager.updateEvent(getApplicationContext(), question.getClosestEvent());
		finish();
	}
	@Override
	public void onCancelClicked(FreeTimeQuestion question) {
		DatabaseManager.removeQuestion(question.getId());
		finish();
	}
	@Override
	public void onCreateClicked(FreeTimeQuestion question) {
		// TODO open New Event Activity
		
	}
	@Override
	public LocationMismatchQuestion loadLocationMismatchQuestion() {
		if(question instanceof LocationMismatchQuestion) return (LocationMismatchQuestion) question;
		finish();
		return null;
	}
	@Override
	public void onUpdateClicked(LocationMismatchQuestion question) {
		DatabaseManager.getInstance(getApplicationContext());
		DatabaseManager.updateEvent(getApplicationContext(), question.getEvent());
		finish();
	}
	@Override
	public void onCancelClicked(LocationMismatchQuestion question) {
		DatabaseManager.removeQuestion(question.getId());
		finish();
		
	}
	@Override
	public void onCreateClicked(LocationMismatchQuestion question) {
		// TODO open New Event Activity
		
	}
	@Override
	public OptimizingQuestion loadOptimizingQuestion() {
		if(question instanceof OptimizingQuestion) return (OptimizingQuestion) question;
		finish();
		return null;
	}
	@Override
	public void onUpdateClicked(OptimizingQuestion question) {
		DatabaseManager.getInstance(getApplicationContext());
		Time now = new Time();
		now.setToNow();
		EventInstanceModel eventContainer = new EventInstanceModel(question.getEvent(),now, now);
		DatabaseManager.updateEvent(getApplicationContext(), eventContainer);
		finish();
	}
	@Override
	public void onCancelClicked(OptimizingQuestion question) {
		DatabaseManager.removeQuestion(question.getId());
		finish();
	}
	
}
