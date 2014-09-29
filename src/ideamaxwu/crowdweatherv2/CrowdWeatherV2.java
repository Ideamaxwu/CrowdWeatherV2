package ideamaxwu.crowdweatherv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker.OnValueChangeListener;

public class CrowdWeatherV2 extends Activity {
	private List<View> lists;
	private TextView appTitle;
	private ImageView[] dots;
	private int currentIndex;
	private final int DAY_TAG_ACTION = R.id.tag_first;
	private final int DAY_TAG_DATE = R.id.tag_second;

	private final int PageNum = 4;
	private List<String[]> wData;

	private Handler handler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initDots();
		if (Location() == null)
		new AlertDialog.Builder(CrowdWeatherV2.this)
				.setMessage("无法定位，请连接网络，默认为上海！")
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
							}
						}).show();
		// 开一条子线程加载网络数据
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					String loc=Location();
					if(loc==null)loc="上海";
					Thread.sleep(2000);
					// JsonWebData解析网络中json中的数据
					JsonWebData webdata = new JsonWebData();
					List<String> data = webdata.getData(loc);
					// 发送消息
					handler.sendMessage(handler.obtainMessage(0, data));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		try {
			// 开启线程
			new Thread(runnable).start();
			// handler与线程之间的通信及数据处理
			handler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == 0) {
						// msg.obj是获取handler发送信息传来的数据
						@SuppressWarnings("unchecked")
						List<String> data = (List<String>) msg.obj;
						// 给PageView绑定数据
						initWData(data);
						initPage();
					}
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		//使 UI 线程中有网络访问
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
	String Location() {
		// TODO Auto-generated method stub
		// Location
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		android.location.Location location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			double latitude = location.getLatitude(); // 经度
			double longitude = location.getLongitude(); // 纬度
			double altitude = location.getAltitude(); // 海拔
			Log.v("OutPut", latitude + "," + longitude + "," + altitude);
			return latitude + "|" + longitude;
		} else {
			Log.v("OutPut", "Location null");
			return null;
		}
	}

	void initWData(List<String> data) {
		wData = new ArrayList<String[]>();
		for (int i = 0; i < 4; i++) {
			String[] onedaydata = new String[] { data.get(0), dayChange(i),
					data.get(i * 4 + 1), data.get(i * 4 + 2),
					data.get(i * 4 + 3), data.get(i * 4 + 4) };
			wData.add(onedaydata);
		}
	}

	String dayChange(int offset) {
		int year = 0;
		int month = 0;
		int day = 0;
		Calendar now = new GregorianCalendar();
		year = now.get(Calendar.YEAR);
		month = now.get(Calendar.MONTH) + 1;// 0 is Jan
		day = now.get(Calendar.DAY_OF_MONTH);
		int maxday = getMaxDay(year, month);
		if (day + offset > maxday) {
			if (month == 12) {
				month = 1;
				year += 1;
				day = day + offset - maxday;
			} else {
				month += 1;
				day = day + offset - maxday;
			}
		} else {
			day += offset;
		}
		return year + "年" + month + "月" + day + "日";
	}

	int getMaxDay(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, 1);
		c.add(Calendar.DAY_OF_YEAR, -1);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	private void initPage() {
		final int PageCount = PageNum;
		lists = new ArrayList<View>();
		for (int i = 0; i < PageCount; i++) {
			LinearLayout linear = (LinearLayout) LayoutInflater.from(this)
					.inflate(R.layout.view_pager_item, null);
			GridView gv = (GridView) linear.findViewById(R.id.grid_view);
			appTitle = (TextView) linear.findViewById(R.id.year_month_day);
			gv.setAdapter(new ItemSelectorAdapter(this, i));
			lists.add(linear);
		}
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(lists);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPagerChangeListener());
	}

	private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.indicator);
		dots = new ImageView[PageNum];
		for (int i = 0; i < PageNum; i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(false);
			dots[i].setTag(i);
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(true);
	}

	private void setCurDot(int positon) {
		if (positon < 0 || positon > PageNum - 1 || currentIndex == positon) {
			return;
		}
		dots[positon].setEnabled(true);
		dots[currentIndex].setEnabled(false);
		currentIndex = positon;
	}

	class ItemSelectorAdapter extends BaseAdapter {
		private Context context = null;
		private int year = 0;
		private int month = 0;
		private int day = 0;
		private int pageid = 0;
		private String[] data;

		public ItemSelectorAdapter(Context context, int i) {
			this.context = context;
			context.getResources();
			pageid = i;
			dayChanged(i);
			appTitle.setText("CrowdWeather");
		}

		public void dayChanged(int offset) {
			Calendar now = new GregorianCalendar();
			year = now.get(Calendar.YEAR);
			month = now.get(Calendar.MONTH) + 1;// 0 is Jan
			day = now.get(Calendar.DAY_OF_MONTH);
			int maxday = getMaxDay(year, month);
			if (day + offset > maxday) {
				if (month == 12) {
					month = 1;
					year += 1;
					day = day + offset - maxday;
				} else {
					month += 1;
					day = day + offset - maxday;
				}
			} else {
				day += offset;
			}

			bindData(offset);
		}

		public int getMaxDay(int year, int month) {
			Calendar c = Calendar.getInstance();
			c.set(year, month, 1);
			c.add(Calendar.DAY_OF_YEAR, -1);
			return c.get(Calendar.DAY_OF_MONTH);
		}

		private void bindData(int offset) {
			data = wData.get(offset);
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			if (data != null) {
				return data[position];
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater li = LayoutInflater.from(context);
			final LinearLayout ll = (LinearLayout) li.inflate(
					R.layout.grid_view_item, null);
			TextView tv = (TextView) ll.findViewById(R.id.grid_item_tv);
			TextView value = (TextView) ll.findViewById(R.id.grid_item_value);
			switch (position) {
			case 0:
				tv.setText("地点");
				break;
			case 1:
				tv.setText("日期");
				break;
			case 2:
				tv.setText("天气");
				break;
			case 3:
				tv.setText("风力");
				break;
			case 4:
				tv.setText("最高");
				break;
			case 5:
				tv.setText("最低");
				break;

			}
			value.setText(data[position]);
			ll.setOnTouchListener(new OnTouchListener() {
				long firstClick;
				long lastClick;
				long downtime;
				int count;
				int finalval;
				String strdate;
				Integer straction;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					View lv = ((LinearLayout) v)
							.findViewById(R.id.grid_item_tv);
					strdate = (String) lv.getTag(DAY_TAG_DATE);
					straction = (Integer) lv.getTag(DAY_TAG_ACTION);
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// 如果第二次点击 距离第一次点击时间过长 那么将第二次点击看为第一次点击
						if (firstClick != 0
								&& System.currentTimeMillis() - firstClick > 300) {
							count = 0;
						}
						count++;
						if (count == 1) {
							firstClick = System.currentTimeMillis();

						} else if (count == 2) {
							lastClick = System.currentTimeMillis();
							// 两次点击小于300ms 也就是连续点击
							if (lastClick - firstClick < 300) {// 判断是否是执行了双击事件
								String sendpara = Calendar.getInstance().get(
										Calendar.YEAR)
										+ "-"
										+ Calendar.getInstance().get(
												Calendar.MONTH)
										+ "-"
										+ Calendar.getInstance().get(
												Calendar.DAY_OF_MONTH)
										+ "-"
										+ Calendar.getInstance().get(
												Calendar.HOUR_OF_DAY)
										+ "-"
										+ Calendar.getInstance().get(
												Calendar.MINUTE)
										+ "-"
										+ Calendar.getInstance().get(
												Calendar.MILLISECOND)
										+ ",loc,"
										+ pageid
										+ ","
										+ straction
										+ ","
										+ strdate;
								Toast.makeText(CrowdWeatherV2.this, sendpara,
										Toast.LENGTH_SHORT).show();
								Client c = new Client();
								c.Send(new String[] { "double" },
										new String[] { sendpara });
							}
						}
						downtime = System.currentTimeMillis();
						break;
					case MotionEvent.ACTION_MOVE:
						break;
					case MotionEvent.ACTION_UP:
						if (System.currentTimeMillis() - downtime > 1500) {
							final String sendpara = Calendar.getInstance().get(
									Calendar.YEAR)
									+ "-"
									+ Calendar.getInstance()
											.get(Calendar.MONTH)
									+ "-"
									+ Calendar.getInstance().get(
											Calendar.DAY_OF_MONTH)
									+ "-"
									+ Calendar.getInstance().get(
											Calendar.HOUR_OF_DAY)
									+ "-"
									+ Calendar.getInstance().get(
											Calendar.MINUTE)
									+ "-"
									+ Calendar.getInstance().get(
											Calendar.MILLISECOND)
									+ ",loc,"
									+ pageid + "," + straction + "," + strdate;
							Toast.makeText(CrowdWeatherV2.this, sendpara,
									Toast.LENGTH_SHORT).show();
							if (straction == 0 || straction == 1) {
								// do nothing
							} else if (straction == 2) {
								final EditText editText = new EditText(
										CrowdWeatherV2.this);
								new AlertDialog.Builder(CrowdWeatherV2.this)
										.setTitle("请输入天气：")
										.setView(editText)
										.setPositiveButton(
												"确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
														Toast.makeText(
																CrowdWeatherV2.this,
																"天气："
																		+ editText
																				.getText()
																				.toString(),
																Toast.LENGTH_SHORT)
																.show();
														Client c = new Client();
														c.Send(new String[] { "long" },
																new String[] { sendpara
																		+ ","
																		+ editText
																				.getText()
																				.toString() });
													}
												})
										.setNegativeButton(
												"取消",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
													}
												}).show();
							} else

							if (straction == 3) {
								final EditText editText = new EditText(
										CrowdWeatherV2.this);
								new AlertDialog.Builder(CrowdWeatherV2.this)
										.setTitle("请输入风力：")
										.setView(editText)
										.setPositiveButton(
												"确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
														Toast.makeText(
																CrowdWeatherV2.this,
																"风力："
																		+ editText
																				.getText()
																				.toString(),
																Toast.LENGTH_SHORT)
																.show();
														Client c = new Client();
														c.Send(new String[] { "long" },
																new String[] { sendpara
																		+ ","
																		+ editText
																				.getText()
																				.toString() });
													}
												})
										.setNegativeButton(
												"取消",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
													}
												}).show();
							} else {
								// TODO Auto-generated method stub
								NumberPicker mPicker = new NumberPicker(
										CrowdWeatherV2.this);
								mPicker.setMinValue(Integer.parseInt(strdate) - 10);
								mPicker.setMaxValue(Integer.parseInt(strdate) + 10);
								mPicker.setValue(Integer.parseInt(strdate));
								mPicker.setOnValueChangedListener(new OnValueChangeListener() {

									@Override
									public void onValueChange(
											NumberPicker picker, int oldVal,
											int newVal) {
										// TODO Auto-generated method stub
										finalval = newVal;
										Log.v("OutPut", String.valueOf(newVal));
									}
								});

								AlertDialog mAlertDialog = new AlertDialog.Builder(
										CrowdWeatherV2.this)
										.setTitle("NumberPicker")
										.setView(mPicker)
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
														Toast.makeText(
																CrowdWeatherV2.this,
																"Picker Click - "
																		+ finalval,
																Toast.LENGTH_SHORT)
																.show();
														Client c = new Client();
														c.Send(new String[] { "long" },
																new String[] { sendpara
																		+ ","
																		+ finalval });
														Log.v("OutPut",
																"Picker Click - "
																		+ finalval);
													}

												}).create();
								mAlertDialog.show();
								//
							}// end of else
							/*
							 * Client c = new Client(); c.Send(new String[] {
							 * "long" }, new String[] { sendpara });
							 */
						}
						break;
					}
					return true;
				}

			});

			tv.setTag(DAY_TAG_ACTION, position);
			getPressItem(tv);
			return ll;
		}

		private void getPressItem(TextView textView) {
			String strItem = getSelectedItem(textView.getTag(DAY_TAG_ACTION)
					.toString());
			String str = data[Integer.parseInt(strItem)];
			textView.setTag(DAY_TAG_DATE, str);
		}

		private String getSelectedItem(String day) {
			if (day.indexOf("\n") != -1) {
				return day.substring(0, day.indexOf("\n"));
			}
			return day;
		}

	}

	class ViewPagerChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int postion) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			setCurDot(position);
		}

	}
}