package reporter66.ru;

import android.app.Application;
import android.content.res.Configuration;

public class reporterApplication extends Application {

	private static reporterApplication singleton;
	
	public static reporterApplication getInstance() {
		return singleton;
	}

	@Override
	public final void onCreate() 
	{
		super.onCreate();
		singleton = this;
	}

	@Override
	public final void onTerminate() 
	{
		super.onTerminate();
	}
	
	@Override
	public final void onLowMemory() 
	{
		super.onLowMemory();
	}
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}

}
