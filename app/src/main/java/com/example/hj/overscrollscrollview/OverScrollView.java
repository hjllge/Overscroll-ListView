package com.example.hj.overscrollscrollview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OverScrollView extends ScrollView {
	private static final String TAG = "OverScrollView";
	public OverScrollView(Context context) {
		super(context);
		init();
	}
	public OverScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private float itemHeight;
	private float maxScrollAmount;
	private void init() {
		setFillViewport(true);
		setOverScrollMode(OVER_SCROLL_NEVER);
		setVerticalScrollBarEnabled(false);
		setOnTouchListener(mOnTouchListener);

		itemHeight = (int)getResources().getDimension(R.dimen.itemHeight);
		post(new Runnable() {
			@Override
			public void run() {
				maxScrollAmount = listItemView.getHeight() - getHeight();
				maxDrag = getHeight();
//				maxTransY = maxDrag / OVER_DEG;

				if(header != null) {
					initialHeaderTransY = -header.getHeight();
					header.setTranslationY(initialHeaderTransY);
				} else {
					Log.d(TAG, "header is null.");
				}
				if(footer != null) {
					initialFooterTransY = footer.getHeight();
					footer.setTranslationY(initialFooterTransY);
				} else {
					Log.d(TAG, "footer is null.");
				}
			}
		});
		maxBouncingHeight = itemHeight * BOUN_HEIGHT_MULTI;	//calculate
	}

	private View listItemView;
	private View header;
	private View footer;
	private float initialHeaderTransY;
	private float initialFooterTransY;
	public void setData(String[] item, int resIdHeader, int resIdItem, int resIdFooter) {
		RelativeLayout relativeLayout = new RelativeLayout(getContext());
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		View view;
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setId(R.id.et_boun_1);	//id 그냥 무작위로 설정
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		for(int i=0; i<item.length; i++) {
			view = inflater.inflate(resIdItem, null);
			TextView tv = view.findViewById(R.id.textView1);
			tv.setText(item[i]);
			linearLayout.addView(view, params);
		}
		relativeLayout.addView(linearLayout, containerParams);

		if(resIdHeader != 0) {
			view = inflater.inflate(resIdHeader, null);
			RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			relativeLayout.addView(view, headerParams);
			header = view;
		}
		if(resIdFooter != 0) {
			view = inflater.inflate(resIdFooter, null);
			RelativeLayout.LayoutParams footerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			footerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			relativeLayout.addView(view, footerParams);
			footer = view;
		}

		addView(relativeLayout, params);
		listItemView = linearLayout;
	}


	/** BOUNCING **/
	private int BOUN_DEG = 5;	//double
	private float MULTIPLIER = 35.0F;
	private float BOUN_HEIGHT_MULTI = 1.3F;
	private float BOUN_K = (float)(Math.pow(BOUN_DEG + 1, BOUN_DEG + 1) / Math.pow(BOUN_DEG, BOUN_DEG));
	private int BOUNCING_DURATION = (int)(BOUN_K * MULTIPLIER);
	private float maxBouncingHeight;

	private Interpolator bounceInterpolator = new Interpolator() {
		@Override
		public float getInterpolation(float x) {
			return BOUN_K * (float)(x * Math.pow(1 - x, BOUN_DEG));
		}
	};

	private long preT;
	private int oldY;
	private int oldoldY;
	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		if(!MainActivity.EFFECT_ON) {
			super.onScrollChanged(x, y, oldx, oldy);
			return;
		}
		if(y == oldY) {
			super.onScrollChanged(x, y, oldx, oldy);
			return;
		}
		long time = System.currentTimeMillis();
//		Log.d(TAG, "y="+y+", oldy="+oldy+", time = "+time);
		if(!isTouching) {
			if(y == 0 || y == maxScrollAmount) {
				int direction = y == 0 ? 1 : -1;
				long delT = (time - preT) << 1;
				float bouncingHeight = MULTIPLIER * (oldoldY - y) / delT * direction;
				bouncingHeight = bouncingHeight > maxBouncingHeight ? maxBouncingHeight : bouncingHeight;
				if(vAnim != null && vAnim.isRunning()) {
					vAnim.cancel();
				}
				vAnim = ValueAnimator.ofFloat(0, bouncingHeight * direction);
//				Log.d(TAG, "bouncing duration = " + BOUNCING_DURATION + ", bouncingHeight = " + bouncingHeight + ", delT = " + delT);
				vAnim.addUpdateListener(mUpdateListener);
				vAnim.addListener(mListenerAdapter);
				vAnim.setInterpolator(bounceInterpolator);
				vAnim.setDuration(BOUNCING_DURATION);
				vAnim.start();
			}
		}
		preT = time;
		oldoldY = oldY;
		oldY = y;
		super.onScrollChanged(x, y, oldx, oldy);
	}

	/** OVERSCROLL **/
	private double OVER_DEG = 0.65;
//	private float maxTransY;
	private float maxDrag;
//	private float baseTransY = 0f;
	private boolean isTouching = false;
	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		private float startY = 0f;
		private float preY = 0f;
//		private long preTime = 0;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
//			Log.d(TAG, "getScrollY = " + getScrollY());
			int currentScrollY = getScrollY();
			int most = 0;
			if(currentScrollY == 0) {            	//top most
				most = -1;
			} else if(currentScrollY == maxScrollAmount) {		//bottom most
				most = 1;
			}
			switch(event.getAction()) {
				case MotionEvent.ACTION_MOVE :
					isTouching = true;
					if(most == 0)
						break;
					if(startY == 0f) {
						startY = event.getY();
						//Log.d(TAG, "startY = " + startY + " is set.");
						if(vAnim != null && vAnim.isRunning()) {
							vAnim.cancel();
						}
						preY = startY;
//						preTime = System.currentTimeMillis();
					} else {
					//MotionEvent.ACTION_MOVE :
						if((most == -1 && event.getY() > startY) || (most == 1 && event.getY() < startY)) {
							if(!MainActivity.EFFECT_ON)
								break;
//							long curTime = System.currentTimeMillis();
							float curY = event.getY();
							double dist = curY - preY;
							float p = Math.abs(curY - startY) / maxDrag;
							float transY = (float)dist * (1 - (float)Math.pow(p, OVER_DEG));
							accumulateTranslationY(transY);
							preY = curY;
//							preTime = curTime;
							//Log.d(TAG, "transY = " + linearLayout.getTranslationY());
							return true;    //must
						}
					}
					break;

				case MotionEvent.ACTION_UP :
					startY = 0f;
					preY = 0f;
					isTouching = false;
					if(!MainActivity.EFFECT_ON)
						break;
					release();
					break;
			}

			return false;
		}
	};

	private void accumulateTranslationY(float transY) {
		listItemView.setTranslationY(listItemView.getTranslationY() + transY);
		if(header != null) {
			header.setTranslationY(header.getTranslationY() + transY);
		}
		if(footer != null) {
			footer.setTranslationY(footer.getTranslationY() + transY);
		}
	}

	private void applyTranslationY(float transY) {
		listItemView.setTranslationY(transY);
		if(header != null) {
			header.setTranslationY(initialHeaderTransY + transY);
		}
		if(footer != null) {
			footer.setTranslationY(initialFooterTransY + transY);
		}
	}

	/** RELEASE **/
	private int RELE_DEG = 8;
	private final Interpolator overscrollInterpolator = new Interpolator() {
		@Override
		public float getInterpolation(float input) {
			return 1f - (float)Math.pow(1 - input, RELE_DEG);
		}
	};
	private int RELE_DURATION = 1000;
	private void release() {
		float transY = listItemView.getTranslationY();
		if(transY == 0)
			return;
		if(vAnim != null && vAnim.isRunning()) {
			vAnim.cancel();
		}
		vAnim = ValueAnimator.ofFloat(transY, 0);
		vAnim.addUpdateListener(mUpdateListener);
		vAnim.addListener(mListenerAdapter);
		vAnim.setInterpolator(overscrollInterpolator);
		final int duration = (int)(RELE_DURATION * overscrollInterpolator.getInterpolation(Math.abs(transY) / maxDrag));
		vAnim.setDuration(duration);
		vAnim.start();
	}


	/** ANIMATION **/
	//private long time;
	private ValueAnimator vAnim;
	private ValueAnimator.AnimatorUpdateListener mUpdateListener =
			new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					/*
					//FPS
					long cur = System.currentTimeMillis();
					int del = (int)(cur - time);
					if(del != 0) {
						int fps = 1000 / del;
						Log.d(TAG, "fps = " + fps);
					}
					time = System.currentTimeMillis();
					*/

					float transY = (Float)animation.getAnimatedValue();
					applyTranslationY(transY);
				}
	};
	private AnimatorListenerAdapter mListenerAdapter = new AnimatorListenerAdapter() {
		private boolean canceled = false;
		@Override
		public void onAnimationCancel(Animator animation) {
			canceled = true;
//			baseTransY = linearLayout.getTranslationY();
			super.onAnimationCancel(animation);
		}
		@Override
		public void onAnimationEnd(Animator animation) {
			if(!canceled) {
//				Log.d(TAG, "onAnimationSafelyEnd");
//				baseTransY = 0f;
			}
			super.onAnimationEnd(animation);
		}
	};

    /** Only for debug **/
	public void debug() {
		Log.d(TAG, "scrollView : getScrollY = " + getScrollY());
		Log.d(TAG, "itemHeight = " + itemHeight);
		Log.d(TAG, "BOUN_DEG = " + BOUN_DEG + ", BOUN_K = " + BOUN_K + ", MULTIPLIER = " + MULTIPLIER);
		getScrollY();
	}
	public int getBOUN_DEG() {
		return this.BOUN_DEG;
	}
	public float getMULTIPLIER() {
		return this.MULTIPLIER;
	}
	public float getBOUN_HEIGHT_MULTI() {
		return this.BOUN_HEIGHT_MULTI;
	}
	public double getOVER_DEG() {
		return this.OVER_DEG;
	}
	public int getRELE_DEG() {
		return this.RELE_DEG;
	}
	public int getRELE_DURATION() {
		return this.RELE_DURATION;
	}

	public void setBOUN_DEG(int value) {
		this.BOUN_DEG = value;
	}
	public void setMULTIPLIER(float value) {
		this.MULTIPLIER = value;
	}
	public void setBOUN_HEIGHT_MULTI(float value) {
		this.BOUN_HEIGHT_MULTI = value;
	}
	public void setOVER_DEG(double value) {
		this.OVER_DEG = value;
	}
	public void setRELE_DEG(int value) {
		this.RELE_DEG = value;
	}
	public void setRELE_DURATION(int value) {
		this.RELE_DURATION = value;
	}
	public void newCalculate() {
		maxBouncingHeight = itemHeight * BOUN_HEIGHT_MULTI;

		BOUN_K = (float)(Math.pow(BOUN_DEG + 1, BOUN_DEG + 1) / Math.pow(BOUN_DEG, BOUN_DEG));
		BOUNCING_DURATION = (int)(BOUN_K * MULTIPLIER);
	}
}