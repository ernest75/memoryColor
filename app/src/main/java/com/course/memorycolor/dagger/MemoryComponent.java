package com.course.memorycolor.dagger;

import android.app.Fragment;

import com.course.memorycolor.GameScreen;
import com.course.memorycolor.PlayerData;
import com.course.memorycolor.fragments.EasyFragment;
import com.course.memorycolor.fragments.HardFragment;
import com.course.memorycolor.fragments.MediumFragment;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Provides;

/**
 * Created by ernestschneiderolcina on 20/3/18.
 */
@Singleton
@Component(modules = {AppModule.class})
public interface MemoryComponent {

    public void injectGameScreen(GameScreen gameScreen);
    public void injectPlayerData(PlayerData playerData);
    public void injectEasyFragment(EasyFragment easyFragment);
    public void injectMediumFragment(MediumFragment mediumFragment);
    public void injectHardFragment(HardFragment hardFragment);

}
