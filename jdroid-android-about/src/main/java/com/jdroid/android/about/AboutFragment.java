package com.jdroid.android.about;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdroid.android.activity.ActivityLauncher;
import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.feedback.RateAppStats;
import com.jdroid.android.intent.IntentUtils;
import com.jdroid.android.recycler.AbstractRecyclerFragment;
import com.jdroid.android.recycler.RecyclerViewAdapter;
import com.jdroid.android.recycler.RecyclerViewType;
import com.jdroid.android.recycler.SimpleRecyclerViewType;
import com.jdroid.android.share.ShareUtils;
import com.jdroid.android.utils.AppUtils;
import com.jdroid.java.collections.Lists;

import java.util.List;

public class AboutFragment extends AbstractRecyclerFragment {
	
	private List<Object> aboutItems = Lists.newArrayList();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Header
		aboutItems.add(new String());

		final String website = getWebsite();
		if (website != null) {
			aboutItems.add(new AboutItem(R.drawable.ic_website_black_24dp, R.string.website) {
				@Override
				public void onSelected(Activity activity) {
					IntentUtils.startUrl(activity, website);
				}
			});
		}

		final String contactUsEmailAddress = getContactUsEmail();
		if (contactUsEmailAddress != null) {
			aboutItems.add(new AboutItem(R.drawable.ic_contact_us_black_24dp, R.string.contactUs) {

				@Override
				public void onSelected(Activity activity) {
					Intent intent = ShareUtils.createOpenMail(contactUsEmailAddress,
						AbstractApplication.get().getAppName());
					if (IntentUtils.isIntentAvailable(intent)) {
						startActivity(intent);
						AbstractApplication.get().getAnalyticsSender().trackContactUs();
					} else {
						// TODO Improve this adding a toast or something
						AbstractApplication.get().getExceptionHandler().logWarningException(
							"Error when sending email intent");
					}
				}
			});
		}

		if (AboutAppModule.get().getAboutContext().getSpreadTheLoveFragmentClass() != null) {
			aboutItems.add(new AboutItem(R.drawable.ic_spread_the_love_black_24dp, R.string.spreadTheLove) {

				@Override
				public void onSelected(Activity activity) {
					ActivityLauncher.launchActivity(SpreadTheLoveActivity.class);
				}
			});
		}
		aboutItems.add(new AboutItem(R.drawable.ic_libraries_black_24dp, R.string.libraries) {
			
			@Override
			public void onSelected(Activity activity) {
				ActivityLauncher.launchActivity(LibrariesActivity.class);
			}
		});
		if (AboutAppModule.get().getAboutContext().isBetaTestingEnabled()) {
			aboutItems.add(new AboutItem(R.drawable.ic_beta_black_24dp, R.string.beta) {

				@Override
				public void onSelected(Activity activity) {
					IntentUtils.startUrl(activity, AboutAppModule.get().getAboutContext().getBetaTestingUrl());
				}
			});
		}
		aboutItems.addAll(getCustomAboutItems());

		if (rateAppViewEnabled() && RateAppStats.displayRateAppView()) {
			// Footer
			aboutItems.add(new Object());
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		List<RecyclerViewType> recyclerViewTypes = Lists.newArrayList();
		recyclerViewTypes.add(new HeaderRecyclerViewType());
		recyclerViewTypes.add(new AboutRecyclerViewType());
		recyclerViewTypes.add(new FooterRecyclerViewType());

		setAdapter(new RecyclerViewAdapter(recyclerViewTypes, aboutItems));
	}
	
	protected String getWebsite() {
		return AbstractApplication.get().getAppContext().getWebsite();
	}

	protected String getContactUsEmail() {
		return AbstractApplication.get().getAppContext().getContactUsEmail();
	}

	protected List<AboutItem> getCustomAboutItems() {
		return Lists.newArrayList();
	}

	protected Boolean rateAppViewEnabled() {
		return true;
	}

	@Override
	protected Boolean isDividerItemDecorationEnabled() {
		return true;
	}

	public class HeaderRecyclerViewType extends RecyclerViewType<String, HeaderItemHolder> {

		@Override
		protected Class<String> getItemClass() {
			return String.class;
		}

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.about_header_view;
		}

		@Override
		public RecyclerView.ViewHolder createViewHolderFromView(View view) {
			HeaderItemHolder holder = new HeaderItemHolder(view);
			holder.appIcon = findView(view, R.id.appIcon);
			holder.appName = findView(view, R.id.appName);
			holder.version = findView(view, R.id.version);
			return holder;
		}

		@Override
		public void fillHolderFromItem(String item, HeaderItemHolder holder) {
			holder.appIcon.setImageResource(AbstractApplication.get().getLauncherIconResId());
			holder.appName.setText(AbstractApplication.get().getAppName());
			holder.version.setText(getString(R.string.version, AppUtils.getVersionName()));
			if (getAppContext().displayDebugSettings()) {
				holder.appIcon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AbstractApplication.get().getDebugContext().launchActivityDebugSettingsActivity();
					}
				});
			}
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return AboutFragment.this;
		}
	}

	public static class HeaderItemHolder extends RecyclerView.ViewHolder {

		protected ImageView appIcon;
		protected TextView appName;
		protected TextView version;
		protected TextView copyright;

		public HeaderItemHolder(View itemView) {
			super(itemView);
		}
	}

	public class FooterRecyclerViewType extends SimpleRecyclerViewType {

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.about_footer_view;
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return AboutFragment.this;
		}
	}

	public class AboutRecyclerViewType extends RecyclerViewType<AboutItem, AboutItemHolder> {

		@Override
		protected Class<AboutItem> getItemClass() {
			return AboutItem.class;
		}

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.default_item;
		}

		@Override
		public RecyclerView.ViewHolder createViewHolderFromView(View view) {
			AboutItemHolder holder = new AboutItemHolder(view);
			holder.image = findView(view, R.id.image);
			holder.name = findView(view, R.id.name);
			return holder;
		}

		@Override
		public void fillHolderFromItem(AboutItem item, AboutItemHolder holder) {
			holder.image.setImageResource(item.getIconResId());
			holder.name.setText(item.getNameResId());
		}

		@Override
		public void onItemSelected(AboutItem item, View view) {
			item.onSelected(getActivity());
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return AboutFragment.this;
		}
	}

	public static class AboutItemHolder extends RecyclerView.ViewHolder {

		protected ImageView image;
		protected TextView name;

		public AboutItemHolder(View itemView) {
			super(itemView);
		}
	}
}
