package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.daimajia.slider.library.R;

import java.io.File;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.daimajia.slider.library.SliderTypes.DefaultSliderView} and
 * {@link com.daimajia.slider.library.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mUrl;
    private File mFile;
    private int mRes;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mErrorDisappear;

    private ImageLoadListener mLoadListener;

    private String mDescription;

//    private Picasso mPicasso;
    // wonseokchoijk 画像ライブラリをPicassoからGlideに変更
    private RequestManager glideRequestManager;

    public static ImageView.ScaleType IMAGEVIEW_SCALE_TYPE = ImageView.ScaleType.FIT_CENTER;

    public static int BACKGROUND_COLOR = android.R.color.white;

    private ImageView mTargetImageView;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.Fit;

    public enum ScaleType{
        CenterCrop, CenterInside, Fit, FitCenterCrop
    }

    protected BaseSliderView(Context context) {
        mContext = context;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     * @param disappear
     * @return
     */
    public BaseSliderView errorDisappear(boolean disappear){
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * the description of a slider image.
     * @param description
     * @return
     */
    public BaseSliderView description(String description){
        mDescription = description;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     * @param url
     * @return
     */
    public BaseSliderView image(String url){
        if(mFile != null || mRes != 0){
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     * @param file
     * @return
     */
    public BaseSliderView image(File file){
        if(mUrl != null || mRes != 0){
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(int res){
        if(mUrl != null || mFile != null){
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    /**
     * lets users add a bundle of additional information
     * @param bundle
     * @return
     */
    public BaseSliderView bundle(Bundle bundle){
        mBundle = bundle;
        return this;
    }

    public String getUrl(){
        return mUrl;
    }

    public boolean isErrorDisappear(){
        return mErrorDisappear;
    }

    public int getEmpty(){
        return mEmptyPlaceHolderRes;
    }

    public int getError(){
        return mErrorPlaceHolderRes;
    }

    public String getDescription(){
        return mDescription;
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * set a slider image click listener
     * @param l
     * @return
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l){
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     * @param v the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, ImageView targetImageView){
        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mOnSliderClickListener != null){
                mOnSliderClickListener.onSliderClick(me);
            }
            }
        });

        if (targetImageView == null)
            return;

        mTargetImageView = targetImageView;
        mTargetImageView.setScaleType(IMAGEVIEW_SCALE_TYPE);
        mTargetImageView.setBackgroundColor(ContextCompat.getColor(mContext, BACKGROUND_COLOR));

        if (mLoadListener != null) {
            mLoadListener.onStart(me);
        }

        // 画像ライブラリをPicasso⇒Glideに変更
//        Picasso p = (mPicasso != null) ? mPicasso : Picasso.with(mContext);
        RequestManager p = (glideRequestManager != null) ? glideRequestManager : Glide.with(mContext);
//        RequestCreator rq = null;
        RequestBuilder rq = null;
        if(mUrl!=null){
            rq = p.load(mUrl);
        }else if(mFile != null){
            rq = p.load(mFile);
        }else if(mRes != 0){
            rq = p.load(mRes);
        }else{
            return;
        }

        if(rq == null){
            return;
        }

        RequestOptions ro = new RequestOptions();
        switch (mScaleType){

            case Fit:
              rq.apply(ro.fitCenter());
              break;
            case CenterCrop:
                rq.apply(ro.centerCrop());
                break;
            case CenterInside:
                rq.apply(ro.centerInside());
                break;
        }

        rq.listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                if(mLoadListener != null){
                    mLoadListener.onEnd(false,me);
                }
                if(v.findViewById(R.id.loading_bar) != null){
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                if(v.findViewById(R.id.loading_bar) != null){
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }
                return false;
            }
        }).into(mTargetImageView);
    }

    public ImageView getImageView() {
        return mTargetImageView;
    }

    public BaseSliderView setScaleType(ScaleType type){
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType(){
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     * @return
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     * @param l
     */
    public void setOnImageLoadListener(ImageLoadListener l){
        mLoadListener = l;
    }

    public interface OnSliderClickListener {
        public void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     * @return
     */
    public Bundle getBundle(){
        return mBundle;
    }

    public interface ImageLoadListener{
        public void onStart(BaseSliderView target);
        public void onEnd(boolean result,BaseSliderView target);
    }
}
