package reporter66.ru.models;

import java.util.List;

import reporter66.ru.db.PostDataSource;

public class Post {
	private long id;
	private String title;
	private String text;
	private Float geo_lat;
	private Float geo_lng;
	private List<PostItem> galleryItems;

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return title;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Float getGeo_lat() {
		return geo_lat;
	}

	public void setGeo_lat(Float geo_lat) {
		this.geo_lat = geo_lat;
	}

	public Float getGeo_lng() {
		return geo_lng;
	}

	public void setGeo_lng(Float geo_lng) {
		this.geo_lng = geo_lng;
	}

	public List<PostItem> getGalleryItems() {
		return galleryItems;
	}

	public void setGalleryItems(List<PostItem> galleryItems) {
		this.galleryItems = galleryItems;
	}
	
}