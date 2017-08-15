package com.baronzhang.android.weather.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baronzhang.android.library.fragment.BaseFragment;
import com.baronzhang.android.library.util.DateConvertUtils;
import com.baronzhang.android.weather.R;
import com.baronzhang.android.weather.contract.HomePageContract;
import com.baronzhang.android.weather.model.db.entities.minimalist.AirQualityLive;
import com.baronzhang.android.weather.model.db.entities.minimalist.WeatherForecast;
import com.baronzhang.android.weather.model.db.entities.minimalist.LifeIndex;
import com.baronzhang.android.weather.model.db.entities.minimalist.Weather;
import com.baronzhang.android.weather.view.adapter.DetailAdapter;
import com.baronzhang.android.weather.view.adapter.ForecastAdapter;
import com.baronzhang.android.weather.view.adapter.LifeIndexAdapter;
import com.baronzhang.android.weather.view.entity.WeatherDetail;
import com.baronzhang.android.weather.view.widget.CannotScrollGridLayoutManager;
import com.baronzhang.android.weather.view.widget.CannotScrollLinearLayoutManager;
import com.baronzhang.android.widget.IndicatorView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomePageFragment extends BaseFragment implements HomePageContract.View {

    //基本天气信息
    @BindView(R.id.cv_weather_information)
    CardView weatherInformationCardView;
    @BindView(R.id.temp_text_view)
    TextView tempTextView;
    @BindView(R.id.weather_text_view)
    TextView weatherNameTextView;
    @BindView(R.id.publish_time_text_view)
    TextView realTimeTextView;

    //AQI
    @BindView(R.id.cv_aqi)
    CardView aqiCardView;
    @BindView(R.id.tv_aqi)
    TextView aqiTextView;
    @BindView(R.id.tv_quality)
    TextView qualityTextView;
    @BindView(R.id.indicator_view_aqi)
    IndicatorView aqiIndicatorView;
    @BindView(R.id.tv_advice)
    TextView adviceTextView;
    @BindView(R.id.tv_city_rank)
    TextView cityRankTextView;

    //详细天气信息
    @BindView(R.id.detail_recycler_view)
    RecyclerView detailRecyclerView;

    //预报
    @BindView(R.id.forecast_recycler_view)
    RecyclerView forecastRecyclerView;

    //生活指数
    @BindView(R.id.index_card_view)
    CardView indexCardView;
    @BindView(R.id.life_index_recycler_view)
    RecyclerView lifeIndexRecyclerView;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private Unbinder unbinder;

    private Weather weather;

    private List<WeatherDetail> weatherDetails;
    private List<WeatherForecast> weatherForecasts;
    private List<LifeIndex> lifeIndices;

    private DetailAdapter detailAdapter;
    private ForecastAdapter forecastAdapter;
    private LifeIndexAdapter lifeIndexAdapter;

    private HomePageContract.Presenter presenter;

    public HomePageFragment() {

    }

    public static HomePageFragment newInstance() {

        return new HomePageFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        //天气详情
        detailRecyclerView.setLayoutManager(new CannotScrollGridLayoutManager(getActivity(), 3));
        weatherDetails = new ArrayList<>();
        detailAdapter = new DetailAdapter(weatherDetails);
        detailAdapter.setOnItemClickListener((adapterView, view, i, l) -> {
        });
        forecastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        detailRecyclerView.setAdapter(detailAdapter);

        //天气预报
        forecastRecyclerView.setLayoutManager(new CannotScrollLinearLayoutManager(getActivity()));
        weatherForecasts = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(weatherForecasts);
        forecastAdapter.setOnItemClickListener((adapterView, view, i, l) -> {
        });
        forecastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        forecastRecyclerView.setAdapter(forecastAdapter);

        //生活指数
        lifeIndexRecyclerView.setLayoutManager(new CannotScrollGridLayoutManager(getActivity(), 4));
        lifeIndices = new ArrayList<>();
        lifeIndexAdapter = new LifeIndexAdapter(getActivity(), lifeIndices);
        lifeIndexAdapter.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(HomePageFragment.this.getContext(), lifeIndices.get(i).getDetails(), Toast.LENGTH_LONG).show());
        lifeIndexRecyclerView.setItemAnimator(new DefaultItemAnimator());
        lifeIndexRecyclerView.setAdapter(lifeIndexAdapter);

        aqiIndicatorView.setIndicatorValueChangeListener((currentIndicatorValue, stateDescription, indicatorTextColor) -> {
            aqiTextView.setText(String.valueOf(currentIndicatorValue));
            if (TextUtils.isEmpty(weather.getAirQualityLive().getQuality())) {
                qualityTextView.setText(stateDescription);
            } else {
                qualityTextView.setText(weather.getAirQualityLive().getQuality());
            }
            aqiTextView.setTextColor(indicatorTextColor);
            qualityTextView.setTextColor(indicatorTextColor);
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        assert presenter != null;
        presenter.subscribe();
    }

    @OnClick(R.id.cv_aqi)
    void onAQIClick() {

        Toast.makeText(getActivity(), "点击了控制污染指数模块", Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void displayWeatherInformation(Weather weather) {

        this.weather = weather;
        onFragmentInteractionListener.updatePageTitle(weather.getCityName());
        tempTextView.setText(weather.getWeatherLive().getTemp());
        weatherNameTextView.setText(weather.getWeatherLive().getWeather());
        realTimeTextView.setText(getString(R.string.string_publish_time) + DateConvertUtils.timeStampToDate(weather.getWeatherLive().getTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));

        AirQualityLive airQualityLive = weather.getAirQualityLive();
        aqiIndicatorView.setIndicatorValue(airQualityLive.getAqi());
        adviceTextView.setText(airQualityLive.getAdvice());
        String rank = airQualityLive.getCityRank();
        cityRankTextView.setText(TextUtils.isEmpty(rank) ? "首要污染物: " + airQualityLive.getPrimary() : rank);

        weatherDetails.clear();
        weatherDetails.addAll(createDetails(weather));
        detailAdapter.notifyDataSetChanged();

        weatherForecasts.clear();
        weatherForecasts.addAll(weather.getWeatherForecasts());
        forecastAdapter.notifyDataSetChanged();

        lifeIndices.clear();
        lifeIndices.addAll(weather.getLifeIndexes());
        lifeIndexAdapter.notifyDataSetChanged();
    }

    private List<WeatherDetail> createDetails(Weather weather) {

        List<WeatherDetail> details = new ArrayList<>();
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "体感温度", weather.getWeatherLive().getFeelsTemperature() + "°C"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "湿度", weather.getWeatherLive().getHumidity() + "%"));
//        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "气压", (int) Double.parseDouble(weather.getWeatherLive().getAirPressure()) + "hPa"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "紫外线指数", weather.getWeatherForecasts().get(0).getUv()));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "降水量", weather.getWeatherLive().getRain() + "mm"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "降水概率", weather.getWeatherForecasts().get(0).getPop() + "%"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "能见度", weather.getWeatherForecasts().get(0).getVisibility() + "km"));
        return details;
    }

    @Override
    public void setPresenter(HomePageContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unSubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnFragmentInteractionListener {
        void updatePageTitle(String title);
    }
}
