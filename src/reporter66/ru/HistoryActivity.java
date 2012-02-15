/**
 * 
 */
package reporter66.ru;

import java.util.ArrayList;
import java.util.List;

import reporter66.ru.db.PostDataSource;
import reporter66.ru.db.PostItemDataSource;
import reporter66.ru.models.Post;
import reporter66.ru.models.PostItem;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author bazilio
 *
 */
public class HistoryActivity extends ListActivity {
	public static PostDataSource postDataSource;
	public static PostItemDataSource postItemsSource;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_list);

		postDataSource = new PostDataSource(this);
		postDataSource.open();

		List<Post> posts = postDataSource.getAllPosts();
		String[] values = new String[posts.size()];
		
		int i = 0;
		for(Post post : posts){
			values[i++] = post.getTitle();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.history_list_item, R.id.title, values);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}
	
	
	public class HistoryListAdapter extends ArrayAdapter<String> {
		private final String[] values;
		private final Context context;
		private List<Post> items = new ArrayList<Post>();

		public HistoryListAdapter(Context context, String[] values, List<Post> itemsInput) {
			super(context, R.layout.history_list_item, values);
			this.context = context;
			this.values = values;
			this.items = itemsInput;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.history_list_item, parent, false);
			TextView title = (TextView) rowView.findViewById(R.id.title);
			TextView text = (TextView) rowView.findViewById(R.id.text);

			title.setText(items.get(position).getTitle());
			text.setText(items.get(position).getText());

			return rowView;
		}
	}
}