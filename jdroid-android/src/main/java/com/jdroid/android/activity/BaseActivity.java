package com.jdroid.android.activity;

import org.slf4j.Logger;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdSize;
import com.jdroid.android.AbstractApplication;
import com.jdroid.android.ActivityLauncher;
import com.jdroid.android.R;
import com.jdroid.android.ad.AdLoader;
import com.jdroid.android.analytics.AnalyticsSender;
import com.jdroid.android.context.DefaultApplicationContext;
import com.jdroid.android.context.SecurityContext;
import com.jdroid.android.debug.DebugSettingsActivity;
import com.jdroid.android.debug.PreHoneycombDebugSettingsActivity;
import com.jdroid.android.domain.User;
import com.jdroid.android.exception.DefaultExceptionHandler;
import com.jdroid.android.intent.ClearTaskIntent;
import com.jdroid.android.loading.DefaultLoadingDialogBuilder;
import com.jdroid.android.loading.LoadingDialogBuilder;
import com.jdroid.android.usecase.DefaultAbstractUseCase;
import com.jdroid.android.usecase.UseCase;
import com.jdroid.android.usecase.listener.DefaultUseCaseListener;
import com.jdroid.android.utils.AndroidUtils;
import com.jdroid.java.utils.ExecutorUtils;
import com.jdroid.java.utils.LoggerUtils;

/**
 * 
 * @author Maxi Rosson
 */
public class BaseActivity implements ActivityIf {
	
	private final static Logger LOGGER = LoggerUtils.getLogger(BaseActivity.class);
	
	private Activity activity;
	protected Dialog loadingDialog;
	private BroadcastReceiver clearTaskBroadcastReceiver;
	
	/**
	 * @param activity
	 */
	public BaseActivity(Activity activity) {
		this.activity = activity;
	}
	
	public ActivityIf getActivityIf() {
		return (ActivityIf)activity;
	}
	
	protected Activity getActivity() {
		return activity;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getAndroidApplicationContext()
	 */
	@Override
	public DefaultApplicationContext getAndroidApplicationContext() {
		return AbstractApplication.get().getAndroidApplicationContext();
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#shouldRetainInstance()
	 */
	@Override
	public Boolean shouldRetainInstance() {
		throw new IllegalArgumentException();
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#getContentView()
	 */
	@Override
	public int getContentView() {
		return getActivityIf().getContentView();
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#onBeforeSetContentView()
	 */
	@Override
	public Boolean onBeforeSetContentView() {
		return true;
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#onAfterSetContentView(android.os.Bundle)
	 */
	@Override
	public void onAfterSetContentView(Bundle savedInstanceState) {
		// Do Nothing
	}
	
	public void beforeOnCreate() {
	}
	
	public void onCreate(Bundle savedInstanceState) {
		LOGGER.trace("Executing onCreate on " + activity);
		AbstractApplication.get().setCurrentActivity(activity);
		
		AbstractApplication.get().initExceptionHandlers();
		
		ActionBar actionBar = getActivityIf().getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
		}
		
		if (getActivityIf().onBeforeSetContentView()) {
			if (getContentView() != 0) {
				activity.setContentView(getContentView());
				getActivityIf().onAfterSetContentView(savedInstanceState);
			}
			if (AndroidUtils.isPreHoneycomb()) {
				clearTaskBroadcastReceiver = new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent) {
						Boolean requiresAuthentication = intent.getBooleanExtra(
							ClearTaskIntent.REQUIRES_AUTHENTICATION_EXTRA, true);
						if (!requiresAuthentication || getActivityIf().requiresAuthentication()) {
							activity.finish();
						}
					}
				};
				activity.registerReceiver(clearTaskBroadcastReceiver, ClearTaskIntent.newIntentFilter());
			}
		}
		
		AdLoader.loadAd(activity, (ViewGroup)(activity.findViewById(R.id.adViewContainer)), getActivityIf().getAdSize());
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#isLauncherActivity()
	 */
	@Override
	public Boolean isLauncherActivity() {
		return false;
	}
	
	public void onContentChanged() {
	}
	
	public void onSaveInstanceState(Bundle outState) {
		LOGGER.trace("Executing onSaveInstanceState on " + activity);
		dismissLoading();
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		LOGGER.trace("Executing onRestoreInstanceState on " + activity);
	}
	
	public void onStart() {
		LOGGER.trace("Executing onStart on " + activity);
		AbstractApplication.get().setCurrentActivity(activity);
		AnalyticsSender.get().onActivityStart(activity);
	}
	
	public void onResume() {
		LOGGER.trace("Executing onResume on " + activity);
		AbstractApplication.get().setInBackground(false);
		AbstractApplication.get().setCurrentActivity(activity);
		
		ActionBar actionBar = getActivityIf().getSupportActionBar();
		if (actionBar != null) {
			DefaultApplicationContext context = AbstractApplication.get().getAndroidApplicationContext();
			if (!context.isProductionEnvironment() && context.displayDebugSettings()) {
				int actionbarBackground = getActionBarDrawableResource();
				if (context.isHttpMockEnabled()) {
					actionbarBackground = R.color.actionbarMockBackground;
				}
				actionBar.setBackgroundDrawable(getActivity().getResources().getDrawable(actionbarBackground));
			}
		}
	}
	
	protected int getActionBarDrawableResource() {
		return R.drawable.abs__ab_transparent_dark_holo;
	}
	
	public void onPause() {
		LOGGER.trace("Executing onPause on " + activity);
		AbstractApplication.get().setInBackground(true);
	}
	
	public void onStop() {
		LOGGER.trace("Executing onStop on " + activity);
		AnalyticsSender.get().onActivityStop(activity);
	}
	
	public void onDestroy() {
		LOGGER.trace("Executing onDestroy on " + activity);
		if (clearTaskBroadcastReceiver != null) {
			activity.unregisterReceiver(clearTaskBroadcastReceiver);
		}
		dismissLoading();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		AbstractApplication.get().setCurrentActivity(activity);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getActivityIf().getMenuResourceId() != 0) {
			MenuInflater inflater = getActivityIf().getSupportMenuInflater();
			inflater.inflate(getActivityIf().getMenuResourceId(), menu);
			getActivityIf().doOnCreateOptionsMenu(menu);
		}
		return true;
	}
	
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		if (getActivityIf().getMenuResourceId() != 0) {
			android.view.MenuInflater inflater = activity.getMenuInflater();
			inflater.inflate(getActivityIf().getMenuResourceId(), menu);
			getActivityIf().doOnCreateOptionsMenu(menu);
		}
		return true;
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#getMenuResourceId()
	 */
	@Override
	public int getMenuResourceId() {
		return 0;
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#doOnCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void doOnCreateOptionsMenu(Menu menu) {
		if (!getAndroidApplicationContext().displayDebugSettings()) {
			MenuItem menuItem = menu.findItem(R.id.debugSettingsItem);
			if (menuItem != null) {
				menuItem.setVisible(false);
			}
		}
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#doOnCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public void doOnCreateOptionsMenu(android.view.Menu menu) {
		// Do Nothing by Default
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			ActivityLauncher.launchHomeActivity();
			return true;
		} else if (item.getItemId() == R.id.debugSettingsItem) {
			Class<? extends Activity> targetActivity;
			if (AndroidUtils.isPreHoneycomb()) {
				targetActivity = PreHoneycombDebugSettingsActivity.class;
			} else {
				targetActivity = DebugSettingsActivity.class;
			}
			ActivityLauncher.launchActivity(targetActivity);
			return true;
		}
		return false;
	}
	
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			ActivityLauncher.launchHomeActivity();
			return true;
		} else if (item.getItemId() == R.id.debugSettingsItem) {
			Class<? extends Activity> targetActivity;
			if (AndroidUtils.isPreHoneycomb()) {
				targetActivity = PreHoneycombDebugSettingsActivity.class;
			} else {
				targetActivity = DebugSettingsActivity.class;
			}
			ActivityLauncher.launchActivity(targetActivity);
			return true;
		}
		return false;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#showLoadingOnUIThread()
	 */
	@Override
	public void showLoadingOnUIThread() {
		showLoadingOnUIThread(new DefaultLoadingDialogBuilder());
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#showLoading()
	 */
	@Override
	public void showLoading() {
		showLoading(new DefaultLoadingDialogBuilder());
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#showLoading(com.jdroid.android.loading.LoadingDialogBuilder)
	 */
	@Override
	public void showLoading(LoadingDialogBuilder builder) {
		if ((loadingDialog == null) || (!loadingDialog.isShowing())) {
			loadingDialog = builder.build(activity);
			loadingDialog.show();
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#showLoadingOnUIThread(com.jdroid.android.loading.LoadingDialogBuilder)
	 */
	@Override
	public void showLoadingOnUIThread(final LoadingDialogBuilder builder) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showLoading(builder);
			}
		});
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#dismissLoading()
	 */
	@Override
	public void dismissLoading() {
		if (loadingDialog != null) {
			loadingDialog.dismiss();
			loadingDialog = null;
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#dismissLoadingOnUIThread()
	 */
	@Override
	public void dismissLoadingOnUIThread() {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dismissLoading();
			}
		});
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeOnUIThread(java.lang.Runnable)
	 */
	@Override
	public void executeOnUIThread(Runnable runnable) {
		if (activity.equals(AbstractApplication.get().getCurrentActivity())) {
			activity.runOnUiThread(runnable);
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getInstance(java.lang.Class)
	 */
	@Override
	public <I> I getInstance(Class<I> clazz) {
		return AbstractApplication.getInstance(clazz);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getExtra(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E> E getExtra(String key) {
		Bundle extras = activity.getIntent().getExtras();
		return extras != null ? (E)extras.get(key) : null;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onResumeUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener)
	 */
	@Override
	public void onResumeUseCase(DefaultAbstractUseCase useCase, DefaultUseCaseListener listener) {
		onResumeUseCase(useCase, listener, UseCaseTrigger.MANUAL);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onResumeUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener,
	 *      com.jdroid.android.activity.BaseActivity.UseCaseTrigger)
	 */
	@Override
	public void onResumeUseCase(final DefaultAbstractUseCase useCase, final DefaultUseCaseListener listener,
			final UseCaseTrigger useCaseTrigger) {
		if (useCase != null) {
			ExecutorUtils.execute(new Runnable() {
				
				@Override
				public void run() {
					useCase.addListener(listener);
					if (useCase.isNotified()) {
						if (useCaseTrigger.equals(UseCaseTrigger.ALWAYS)) {
							useCase.run();
						}
					} else {
						if (useCase.isInProgress()) {
							listener.onStartUseCase();
						} else if (useCase.isFinishSuccessful()) {
							listener.onFinishUseCase();
							useCase.markAsNotified();
						} else if (useCase.isFinishFailed()) {
							try {
								listener.onFinishFailedUseCase(useCase.getRuntimeException());
							} finally {
								useCase.markAsNotified();
							}
						} else if (useCase.isNotInvoked()
								&& (useCaseTrigger.equals(UseCaseTrigger.ONCE) || useCaseTrigger.equals(UseCaseTrigger.ALWAYS))) {
							useCase.run();
						}
					}
				}
			});
		}
	}
	
	public enum UseCaseTrigger {
		MANUAL,
		ONCE,
		ALWAYS;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onPauseUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener)
	 */
	@Override
	public void onPauseUseCase(final DefaultAbstractUseCase userCase, final DefaultUseCaseListener listener) {
		if (userCase != null) {
			userCase.removeListener(listener);
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeUseCase(com.jdroid.android.usecase.UseCase)
	 */
	@Override
	public void executeUseCase(UseCase<?> useCase) {
		ExecutorUtils.execute(useCase);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeUseCase(com.jdroid.android.usecase.UseCase, java.lang.Long)
	 */
	@Override
	public void executeUseCase(UseCase<?> useCase, Long delaySeconds) {
		ExecutorUtils.schedule(useCase, delaySeconds);
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onStartUseCase()
	 */
	@Override
	public void onStartUseCase() {
		getActivityIf().showLoadingOnUIThread();
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onUpdateUseCase()
	 */
	@Override
	public void onUpdateUseCase() {
		// Do nothing by default
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onFinishUseCase()
	 */
	@Override
	public void onFinishUseCase() {
		// Do nothing by default
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onFinishFailedUseCase(java.lang.RuntimeException)
	 */
	@Override
	public void onFinishFailedUseCase(RuntimeException runtimeException) {
		if (getActivityIf().goBackOnError(runtimeException)) {
			DefaultExceptionHandler.markAsGoBackOnError(runtimeException);
		} else {
			DefaultExceptionHandler.markAsNotGoBackOnError(runtimeException);
		}
		getActivityIf().dismissLoadingOnUIThread();
		throw runtimeException;
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onFinishCanceledUseCase()
	 */
	@Override
	public void onFinishCanceledUseCase() {
		// Do nothing by default
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#goBackOnError(java.lang.RuntimeException)
	 */
	@Override
	public Boolean goBackOnError(RuntimeException runtimeException) {
		return true;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#findView(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends View> V findView(int id) {
		return (V)activity.findViewById(id);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#findViewOnActivity(int)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V extends View> V findViewOnActivity(int id) {
		return (V)activity.findViewById(id);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#inflate(int)
	 */
	@Override
	public View inflate(int resource) {
		return LayoutInflater.from(activity).inflate(resource, null);
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#requiresAuthentication()
	 */
	@Override
	public Boolean requiresAuthentication() {
		return true;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getUser()
	 */
	@Override
	public User getUser() {
		return SecurityContext.get().getUser();
	}
	
	public Boolean isAuthenticated() {
		return SecurityContext.get().isAuthenticated();
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#getSupportMenuInflater()
	 */
	@Override
	public MenuInflater getSupportMenuInflater() {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#getSupportActionBar()
	 */
	@Override
	public ActionBar getSupportActionBar() {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getArgument(java.lang.String)
	 */
	@Override
	public <E> E getArgument(String key) {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getArgument(java.lang.String, java.lang.Object)
	 */
	@Override
	public <E> E getArgument(String key, E defaultValue) {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getAdSize()
	 */
	@Override
	public AdSize getAdSize() {
		return AdSize.SMART_BANNER;
	}
}
