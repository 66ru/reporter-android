package reporter66.ru;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public class reporterApplication extends Application {

	//private static reporterApplication singleton;
	private static Context context;
	
	/*public static reporterApplication getInstance() {
		return singleton;
	}*/
	public static Context getContext() {
		return context;
	}

	@Override
	public final void onCreate() 
	{
		//super.onCreate();
		context = this;
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
