package reporter66.ru.models;

import java.util.List;

import reporter66.ru.db.PostDataSource;

public class Post {
	private long id;
	private String title;
	private String text;
	private Double geo_lat;
	private Double geo_lng;
	private List<PostItem> galleryItems;
	private long external_id;
	private String uid;
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

	public Double getGeo_lat() {
		return geo_lat;
	}

	public void setGeo_lat(Double geo_lat) {
		this.geo_lat = geo_lat;
	}

	public Double getGeo_lng() {
		return geo_lng;
	}

	public void setGeo_lng(Double geo_lng) {
		this.geo_lng = geo_lng;
	}

	public List<PostItem> getGalleryItems() {
		return galleryItems;
	}

	public void setGalleryItems(List<PostItem> galleryItems) {
		this.galleryItems = galleryItems;
	}
	
	public long getExternal_id() {
		return external_id;
	}

	public void setExternal_id(long external_id) {
		this.external_id = external_id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}