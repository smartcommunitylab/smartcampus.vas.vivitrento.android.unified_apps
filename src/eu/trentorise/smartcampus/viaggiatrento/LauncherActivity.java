/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.viaggiatrento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.common.ViviTrentoHelper;
import eu.trentorise.smartcampus.jp.MonitorJourneyActivity;
import eu.trentorise.smartcampus.jp.PlanJourneyActivity;
import eu.trentorise.smartcampus.jp.ProfileActivity;
import eu.trentorise.smartcampus.jp.SavedJourneyActivity;
import eu.trentorise.smartcampus.jp.SmartCheckDirectActivity;
import eu.trentorise.smartcampus.jp.TutorialManagerActivity;
import eu.trentorise.smartcampus.jp.helper.CopyTask;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.OnTaskCompleted;
import eu.trentorise.smartcampus.jp.helper.UserRegistration;
import eu.trentorise.smartcampus.jp.notifications.BroadcastNotificationsActivity;
import eu.trentorise.smartcampus.jp.notifications.NotificationsFragmentActivityJP;
import eu.trentorise.smartcampus.mobilityservice.MobilityUserService;

public class LauncherActivity extends TutorialManagerActivity implements OnTaskCompleted {

	// public static final String FIRSTTIME = "load_first_time";
	// public static final String PREFS_NAME = "LauncherPreferences";

	public static final String UPDATE = "update";
	private String mToken = null;

	@Override
	protected void initDataManagement(Bundle savedInstanceState) {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		try {
			initGlobalConstants();
			SCAccessProvider accessprovider = SCAccessProvider.getInstance(LauncherActivity.this);
			if (!accessprovider.isLoggedIn(this)) {
				showLoginDialog(accessprovider);
			} else {
				JPParamsHelper.init(getApplicationContext());
				ViviTrentoHelper.init(getApplicationContext());
				super.initDataManagement(savedInstanceState);
				// JPHelper.init(getApplicationContext());
				prepareView();
			}
		}

		catch (Exception e) {
			Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (JPHelper.isInitialized()) {
			JPHelper.getLocationHelper().start();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (JPHelper.isInitialized()) {
			JPHelper.getLocationHelper().stop();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSupportMenuInflater().inflate(R.menu.launchergripmenu, menu);
		menu.getItem(0).setVisible(false);
		SubMenu submenu = menu.getItem(1).getSubMenu();
		submenu.clear();
		submenu.setIcon(R.drawable.ic_action_overflow);

		// tutorial
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_tutorial, Menu.NONE, R.string.menu_tutorial);
		// preferences
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_pref, Menu.NONE, R.string.btn_myprofile);
		// about pages
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.about, Menu.NONE, R.string.about);

		if (JPHelper.isUserAnonymous(this)) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.upgrade_user_menu, Menu.NONE, R.string.upgrade_user_menu);
		}
		// if (SCAccessProvider.isUserAnonymous(this)) {
		// submenu.add(Menu.CATEGORY_SYSTEM, R.id.upgrade_user_menu, Menu.NONE,
		// R.string.upgrade_user_menu);
		// }

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == R.id.upgrade_user_menu) {
			// promote user
			UserRegistration.upgradeuser(this);
		} else if (item.getItemId() == R.id.about) {
			// FragmentTransaction fragmentTransaction =
			// getSupportFragmentManager().beginTransaction();
			// Fragment fragment = new AboutFragment();
			// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// fragmentTransaction.replace(Config.mainlayout, fragment,
			// "about");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			// fragmentTransaction.commit();
			startActivity(new Intent(getApplicationContext(), AboutActivity.class));
		} else if (item.getItemId() == R.id.menu_item_pref) {
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNewIntent(Intent arg0) {
		super.onNewIntent(arg0);
		if (getResources().getString(R.string.smartcampus_action_start).equals(arg0.getAction())) {
			try {
				// SCAccessProvider provider =
				SCAccessProvider.getInstance(LauncherActivity.this);
			} catch (Exception e) {
				Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
	}

	/*
	 * Manage the result after login if in sharedpreferences are stored
	 * itinerarys -> upgrade user else if result is ok -> anonymous else is
	 * cancelled
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			try {
				SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
				MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(this) + JPHelper.MOBILITY_URL);
				if (sharedPref.contains(JPHelper.MY_ITINERARIES)) {
					mToken = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
					if (mToken == null) {
						Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
					} else {
						// set user to not anonymous
						JPHelper.setUserAnonymous(this, false);
						supportInvalidateOptionsMenu();
						JPHelper.readAccountProfile(new CopyTask(sharedPref, userService, resultCode, data, this, this));
						ViviTrentoHelper.init(getApplicationContext());
					}
				}

				else if (resultCode == RESULT_OK) {
					mToken = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
					if (mToken == null) {
						Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
					} else {
						JPHelper.init(getApplicationContext());
						ViviTrentoHelper.init(getApplicationContext());
						prepareView();
						supportInvalidateOptionsMenu();
						if (JPHelper.isFirstLaunch(this)) {
							showTourDialog();
							JPHelper.disableFirstLaunch(this);
						}
					}

				} else if (resultCode == RESULT_CANCELED) {
					Toast.makeText(this, getString(R.string.token_required), Toast.LENGTH_LONG).show();
					// clean shared preferences
					finish();
				} else {
					Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
					// clean shared preferences
					finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void prepareView() {
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		List<View> list = createButtons();
		LinearLayout ll = null;
		LinearLayout parent = (LinearLayout) findViewById(R.id.homelayout);
		parent.removeAllViews();
		for (int i = 0; i < list.size(); i++) {
			if (ll == null) {
				ll = new LinearLayout(this);
				ll.setOrientation(LinearLayout.HORIZONTAL);
				ll.setGravity(Gravity.TOP | Gravity.CENTER);
				ll.setWeightSum(3);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(0, 32, 0, 0);
				parent.addView(ll, layoutParams);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.weight = 1;
			ll.addView(list.get(i), layoutParams);
			if ((i + 1) % 3 == 0) {
				ll = null;
			}
		}
	}

	// /**
	// * @param accessprovider
	// * @throws OperationCanceledException
	// * @throws AuthenticatorException
	// * @throws IOException
	// * @throws AACException
	// */
	// private void ensureToken(final SCAccessProvider accessprovider)
	// throws OperationCanceledException, AuthenticatorException,
	// IOException, AACException {
	// // or is logged == false ???
	// if (accessprovider.readToken(this) == null) {
	// // if (accessprovider.readUserData(this, null) == null) {
	// if (!accessprovider.isLoggedIn(this)){
	// showLoginDialog(accessprovider);
	// } else {
	// // accessprovider.login(LauncherActivity.this,
	// accessprovider.isUserAnonymous(this) ? "anonymous" : null);
	// accessprovider.login(LauncherActivity.this, null);
	// }
	// } else {
	// if (JPHelper.isFirstLaunch(this)) {
	// showTourDialog();
	// JPHelper.disableFirstLaunch(this);
	// }
	// }
	// }

	/**
	 * @param accessprovider
	 */
	private void showLoginDialog(final SCAccessProvider accessprovider) {
		// dialogbox for registration
		DialogInterface.OnClickListener updateDialogClickListener;

		updateDialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:

						// yes -> accessprovider.getAuthToken(this,
						// null);-> shared preferences "registred" true
						JPHelper.setUserAnonymous(LauncherActivity.this, false);
						JPHelper.login(LauncherActivity.this);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// no -> accessprovider.getAuthToken(this,
						// "anonymous"); -> shared preferences
						// "registred" true
						// con bundle
						// accessprovider.login(LauncherActivity.this,
						// "anonymous");
						JPHelper.setUserAnonymous(LauncherActivity.this, true);
						JPHelper.login(LauncherActivity.this);
						break;
					}
					supportInvalidateOptionsMenu();
				} catch (Exception e) {
					Toast.makeText(LauncherActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage(getString(R.string.auth_required))
				.setPositiveButton(android.R.string.yes, updateDialogClickListener)
				.setNegativeButton(R.string.not_now, updateDialogClickListener).show();
	}

	public void goToFunctionality(View view) {
		Intent intent;
		int viewId = view.getId();
		if (viewId == R.id.btn_planjourney) {
			intent = new Intent(this, PlanJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorrecurrent) {
			intent = new Intent(this, MonitorJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_broadcast) {
			intent = new Intent(this, BroadcastNotificationsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_myprofile) {
			intent = new Intent(this, ProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorsaved) {
			intent = new Intent(this, SavedJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_notifications) {
			intent = new Intent(this, NotificationsFragmentActivityJP.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else {
			String[] smartNames = getResources().getStringArray(R.array.smart_checks_list);
			TypedArray smartIds = getResources().obtainTypedArray(R.array.smart_check_list_ids);
			for (int i = 0; i < smartNames.length; i++) {
				String scName = smartNames[i];
				if (smartIds.getResourceId(i, 0) == viewId) {
					SmartCheckDirectActivity.startSmartCheck(this, scName);
					return;
				}
			}
			smartIds.recycle();

			Toast toast = Toast.makeText(getApplicationContext(), R.string.tmp, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
	}

	private void initGlobalConstants() throws NameNotFoundException, NotFoundException {
		GlobalConfig.setAppUrl(this, getResources().getString(R.string.smartcampus_app_url));
		// Constants.setAuthUrl(this,
		// getResources().getString(R.string.smartcampus_auth_url));
		// GlobalConfig.setAppUrl(this,
		// getResources().getString(R.string.smartcampus_app_url));
		// SharedPreferences settings =
		// LauncherActivity.this.getSharedPreferences(PREFS_NAME, 0);
		// settings.edit().putBoolean(FIRSTTIME, true).commit();
	}

	private List<View> createButtons() {
		List<View> list = new ArrayList<View>();
		// First, set the smart check options
		String[] smartNames = getResources().getStringArray(R.array.smart_checks_list);
		List<String> smartNamesFiltered = Arrays.asList(JPParamsHelper.getSmartCheckOptions());
		TypedArray smartIds = getResources().obtainTypedArray(R.array.smart_check_list_ids);
		TypedArray smartIcons = getResources().obtainTypedArray(R.array.smart_check_list_icons);
		for (int i = 0; i < smartNames.length; i++) {
			if (smartNamesFiltered.contains(smartNames[i])) {
				Button b = (Button) getLayoutInflater().inflate(R.layout.home_btn, new LinearLayout(getApplicationContext()),
						false);
				b.setText(smartNames[i]);
				b.setId(smartIds.getResourceId(i, 0));
				b.setCompoundDrawablesWithIntrinsicBounds(null, smartIcons.getDrawable(i), null, null);
				list.add(b);
			}
		}
		smartIcons.recycle();
		smartIds.recycle();
		String[] allNames = getResources().getStringArray(R.array.main_list);
		TypedArray allIds = getResources().obtainTypedArray(R.array.main_list_ids);
		TypedArray allIcons = getResources().obtainTypedArray(R.array.main_list_icons);
		for (int i = 0; i < allNames.length; i++) {
			Button b = (Button) getLayoutInflater()
					.inflate(R.layout.home_btn, new LinearLayout(getApplicationContext()), false);
			b.setText(allNames[i]);
			b.setId(allIds.getResourceId(i, 0));
			b.setCompoundDrawablesWithIntrinsicBounds(null, allIcons.getDrawable(i), null, null);
			list.add(b);
		}
		allIcons.recycle();
		allIds.recycle();
		return list;
	}

	@Override
	public void onTaskCompleted(String result) {
		mToken = result;
	}

}