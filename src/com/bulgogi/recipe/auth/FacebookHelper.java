package com.bulgogi.recipe.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.utils.PreferenceHelper;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class FacebookHelper {
	private Activity activity;
	private Session.StatusCallback statusCallback;
	private GraphUser user;
	private OnSessionListener onSessionListener;
	
	public interface OnSessionListener {
		public void onLoginComplete(String id, String name);
		public void onLogoutComplete();
	};
	
	public void setOnLoginListener(OnSessionListener onLoginListener) {
		this.onSessionListener = onLoginListener;
	}

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

			PreferenceHelper.getInstance().putString(Constants.PREF_NAME, null);
			PreferenceHelper.getInstance().putString(Constants.PREF_FACEBOOK_ID, null);

			Toast.makeText(activity, activity.getResources().getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
			
			if (onSessionListener != null) {
				onSessionListener.onLogoutComplete();
			}
		}
	}

	public boolean isLogin() {
		Session session = Session.getActiveSession();
		return session.isOpened();
	}

	public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		return Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
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

	public String getId() {
		if (user != null) {
			return user.getId();
		} else if (PreferenceHelper.getInstance().contains(Constants.PREF_FACEBOOK_ID)) {
			return PreferenceHelper.getInstance().getString(Constants.PREF_FACEBOOK_ID, new String());
		}
		return null;
	}

	public String getName() {
		if (user != null) {
			return user.getName();
		} else if (PreferenceHelper.getInstance().contains(Constants.PREF_NAME)) {
			return PreferenceHelper.getInstance().getString(Constants.PREF_NAME, new String());
		}
		return null;
	}

	public void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser graphUser, Response response) {
				if (session == Session.getActiveSession()) {
					if (graphUser != null) {
						user = graphUser;

						String name = user.getName();
						String id = user.getId();
						PreferenceHelper.getInstance().putString(Constants.PREF_NAME, name);
						PreferenceHelper.getInstance().putString(Constants.PREF_FACEBOOK_ID, id);
						
						if (onSessionListener != null) {
							onSessionListener.onLoginComplete(id, name);
						}
					}
				}

				if (response.getError() != null) {

				}
			}
		});

		request.executeAsync();
	}
}
