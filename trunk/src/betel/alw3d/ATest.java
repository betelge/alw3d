package betel.alw3d;

import betel.ATest.R;
import utils.StringLoader;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ATest extends Activity{
	
	private Model model;
	private View surface;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new Model();
        surface = new View(this, model);
       // setContentView(surface);
        TextView tv = new TextView(this);
        tv.setText("File: " + StringLoader.loadStringExceptionless(this, R.raw.object));
        setContentView(tv);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	surface.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	surface.onResume();
    }
}