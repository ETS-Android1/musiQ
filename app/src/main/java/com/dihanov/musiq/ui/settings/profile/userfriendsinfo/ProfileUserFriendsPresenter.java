package com.dihanov.musiq.ui.settings.profile.userfriendsinfo;

import com.dihanov.musiq.db.UserSettingsRepository;
import com.dihanov.musiq.models.UserFriends;
import com.dihanov.musiq.service.LastFmApiClient;
import com.dihanov.musiq.util.AppLog;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileUserFriendsPresenter implements ProfileUserFriendsContract.Presenter {
    private final String TAG = getClass().getSimpleName();

    private LastFmApiClient lastFmApiClient;
    private ProfileUserFriendsContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private UserSettingsRepository userSettingsRepository;

    @Inject
    public ProfileUserFriendsPresenter(LastFmApiClient lastFmApiClient, UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.lastFmApiClient = lastFmApiClient;
    }

    @Override
    public void takeView(ProfileUserFriendsContract.View view) {
        this.view = view;
    }

    @Override
    public void leaveView() {
        this.view = null;
    }

    @Override
    public void fetchFriends(int limit) {
        lastFmApiClient.getLastFmApiService()
                .getUserFriends(userSettingsRepository.getUsername(), 1, limit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<UserFriends>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                        if (view != null){
                            view.showProgressBar();
                        }
                    }

                    @Override
                    public void onNext(UserFriends userFriends) {
                        if(userFriends != null){
                            if(userFriends.getFriends() != null
                                    && userFriends.getFriends().getUser() != null
                                    && !userFriends.getFriends().getUser().isEmpty()
                                    && view != null){
                                view.loadFriends(userFriends.getFriends().getUser());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLog.log(TAG, e.getMessage());
                        if (view != null){
                            view.hideProgressBar();
                        }
                        compositeDisposable.clear();
                    }

                    @Override
                    public void onComplete() {
                        if (view != null){
                            view.hideProgressBar();
                        }
                        compositeDisposable.clear();
                    }
                });
    }
}