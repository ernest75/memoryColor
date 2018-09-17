package com.course.memorycolor.dagger;

import android.app.Application;
import android.content.Context;

import com.course.memorycolor.model.ModelMemoryColor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ernestschneiderolcina on 20/3/18.
 */

@Module
public class AppModule {

    Application mApp;


    public AppModule(Application app){
        mApp = app; }

    @Provides
    Application provideApplication(){ return mApp; }

    @Provides
    @Named("application_context")
    Context provideAppContext(){ return mApp ; }

    @Provides
    @Singleton
    ModelMemoryColor getModel(){
        return new ModelMemoryColor(provideAppContext());
    }


}
