package com.bulgogi.recipe.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class FacebookHelper {
	private Activity activity;
	Session.StatusCallback statusCallback;
	
	public FacebookHelper(Activity activity, Bundle savedInstanceState, Session.StatusCallback statusCallback) {
		this.activity = activity;
		this.statusCallback = statusCallback;
		
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this.activity, null, this.statusCallback, savedInstanceState);
            }
            
            if (session == null) {
                session = new Session(this.activity);
            }
            
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this.activity).setCallback(this.statusCallback));
            }
        }
	}
	
	public void login() {
		Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(activity).setCallback(statusCallback));
        } else {
            Session.openActiveSession(activity, true, this.statusCallback);
        }
	}
	
	public void logout() {
		Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
	}
	
	public boolean isLogin() {
		Session session = Session.getActiveSession();
		return session.isOpened();
	}

	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
	}
	
	public void saveSession(Bundle outState) {
		Session session = Session.getActiveSession();
        Session.saveSession(session, outState);	
	}
	
	public void addSessionStatusCallback(Session.StatusCallback statusCallback) {
		Session.getActiveSession().addCallback(statusCallback);		
	}
	
	public void removeSessionStatusCallback(Session.StatusCallback statusCallback) {
		Session.getActiveSession().removeCallback(statusCallback);
	}
}
