package com.baronzhang.android.weather.presenter;

import android.content.Context;
import android.widget.Toast;

import com.baronzhang.android.library.util.RxSchedulerUtils;
import com.baronzhang.android.weather.ApplicationModule;
import com.baronzhang.android.weather.contract.HomePageContract;
import com.baronzhang.android.weather.model.db.dao.WeatherDao;
import com.baronzhang.android.weather.model.preference.PreferenceHelper;
import com.baronzhang.android.weather.model.preference.WeatherSettings;
import com.baronzhang.android.weather.model.repository.WeatherDataRepository;
import com.baronzhang.android.weather.presenter.component.DaggerPresenterComponent;
import com.baronzhang.android.weather.util.ActivityScoped;

import javax.inject.Inject;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author baronzhang (baron[dot]zhanglei[at]gmail[dot]com)
 */
@ActivityScoped
public final class HomePagePresenter implements HomePageContract.Presenter {

    private final Context context;
    private final HomePageContract.View weatherView;

    private CompositeSubscription subscriptions;

    @Inject
    WeatherDao weatherDao;

    @Inject
    HomePagePresenter(Context context, HomePageContract.View view) {

        this.context = context;
        this.weatherView = view;
        this.subscriptions = new CompositeSubscription();
        weatherView.setPresenter(this);
        weatherView.setPresenter(this);

        DaggerPresenterComponent.builder()
                .applicationModule(new ApplicationModule(context))
                .build().inject(this);
    }

    @Override
    public void subscribe() {
        String cityId = PreferenceHelper.getSharedPreferences().getString(WeatherSettings.SETTINGS_CURRENT_CITY_ID.getId(), "");
        loadWeather(cityId);
    }

    @Override
    public void loadWeather(String cityId) {
        Subscription subscription = WeatherDataRepository.getWeather(context, cityId, weatherDao)
                .compose(RxSchedulerUtils.normalSchedulersTransformer())
                .subscribe(weatherView::displayWeatherInformation, throwable -> {
                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
        subscriptions.add(subscription);
    }

    @Override
    public void unSubscribe() {
        subscriptions.clear();
    }
}
