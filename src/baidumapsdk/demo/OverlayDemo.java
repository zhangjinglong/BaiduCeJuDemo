package baidumapsdk.demo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;

/**
 * 演示覆盖物的用法
 */
public class OverlayDemo extends Activity {

	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerA;
	private InfoWindow mInfoWindow;
	private List<LatLng> points;//所有点击的点的坐标
	private List<Marker> markers;
	private List<Overlay> lines;
	private long totalJuli=0;
	// 初始化全局 bitmap 信息，不用时及时 recycle
	BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overlay);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(msu);
		points=new ArrayList<LatLng>();
		markers=new ArrayList<Marker>();
		lines=new ArrayList<Overlay>();
		mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
			
			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				// 显示marker
				OverlayOptions ooA = new MarkerOptions().position(arg0).icon(bdA)
						.zIndex(9).draggable(true);
				mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
				markers.add(mMarkerA);
				points.add(arg0);
				juli(points);
			}
		});
	}
	//对距离进行初始化
	public String getFormatJuli(long juli){
		DecimalFormat df = new DecimalFormat(".#");
		int allJuli1 = (int) Math.round(juli);// 清除小数部分
		if (allJuli1 >= 1000) {
			return df.format(allJuli1 / 1000.00) + "km";
		} else {
			return allJuli1 + "m";
		}
	}
	public void juli(List<LatLng> points){
		if(points.size()>=2){
		totalJuli+=DistanceUtil.getDistance(points.get(points.size()-2), points.get(points.size()-1));
		PolylineOptions polylineoption = new PolylineOptions();
		polylineoption.color(Color.parseColor("#0000ff"));
		polylineoption.width(6);
		polylineoption.points(points);//定义折线
		Overlay over=mBaiduMap.addOverlay(polylineoption);   //添加折线到地图上
		lines.add(over);
		//showinfowindow
		Button tv=new Button(getApplicationContext());
		tv.setBackgroundColor(Color.parseColor("#ffffff"));
		tv.setTextColor(Color.parseColor("#ff9966"));
		tv.setText(getFormatJuli(totalJuli));
		mInfoWindow = new InfoWindow(tv, points.get(points.size()-1), -47);
		mBaiduMap.showInfoWindow(mInfoWindow);
		}else{
			//showinfowindow
			Button tv=new Button(getApplicationContext());
			tv.setBackgroundColor(Color.parseColor("#ffffff"));
			tv.setText("0m");
			tv.setTextColor(Color.parseColor("#ff9966"));
			mInfoWindow = new InfoWindow(tv, points.get(points.size()-1), -47);
			mBaiduMap.showInfoWindow(mInfoWindow);
		}
	}

	/**
	 * 清除所有Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
		points.clear();
		totalJuli=0;
	}

	/**
	 * 重新添加Overlay
	 * 
	 * @param view
	 */
	public void resumeOverlay(View view) {
		if(points.size()>1){
			totalJuli-=DistanceUtil.getDistance(points.get(points.size()-2), points.get(points.size()-1));
			points.remove(points.size()-1);
			Toast.makeText(getApplicationContext(), "markers:"+(markers.size()-1), 5000).show();
			Toast.makeText(getApplicationContext(), "lines:"+(lines.size()-1), 5000).show();
			markers.get(markers.size()-1).remove();//删除marker
			markers.remove(markers.size()-1);
			lines.get(lines.size()-1).remove();//删除折线
			lines.remove(lines.size()-1);
			mBaiduMap.hideInfoWindow();
			Button tv=new Button(getApplicationContext());
			tv.setBackgroundColor(Color.parseColor("#ffffff"));
			tv.setTextColor(Color.parseColor("#ff9966"));
			if(points.size()==1){
				tv.setText("0m");
			}else{
				tv.setText(getFormatJuli(totalJuli));
			}
			mInfoWindow = new InfoWindow(tv, points.get(points.size()-1), -47);
			mBaiduMap.showInfoWindow(mInfoWindow);
		}else{
			clearOverlay(null);
		}
		
		mMapView.invalidate();
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.onDestroy();
		super.onDestroy();
		// 回收 bitmap 资源
		bdA.recycle();
	}

}
