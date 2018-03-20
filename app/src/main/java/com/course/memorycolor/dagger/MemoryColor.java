package com.course.memorycolor.dagger;

import android.app.Activity;
import android.app.Application;

import com.course.memorycolor.GameScreen;

/**
 * Created by ernestschneiderolcina on 20/3/18.
 */

public class MemoryColor extends Application {

    private static MemoryComponent mMemoryComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mMemoryComponent = DaggerMemoryComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static MemoryComponent getMemoryComponent() {
        return mMemoryComponent;
    }
}
