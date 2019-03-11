package in.arjsna.filechooser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by arjun on 16/2/16.
 */
public class FileSelectFragment extends Fragment implements GestureDetector.OnGestureListener {
  public static final String BUCKET_NAME = "BUCKET_NAME";
  public static final String BUCKET_ID = "BUCKET_ID";
  public static final String BUCKET_COVER_IMAGE_FILE_PATH = "BUCKET_COVER_IMAGE_FILE_PATH";
  public static final String BUCKET_CONTENT_COUNT =
      "BUCKET_CONTENT_COUNT";
  private static final float SCROLL_THRESHOLD = 10;
  private String bucketName;
  private String bucketId;
  private RecyclerView mFilesListView;
  private ArrayList<FileItem> files = new ArrayList<>();
  private FilesListAdapter mFilesListAdapter;
  private int fileType;
  private float mDownX;
  private float mDownY;
  private boolean isOnClick;
  private int bucketContentCount;
  private String bucketCoverImageFilePath;
  private TextView titleView;
  private TextView mAdddFilesButton;
  private View mRootView;
  private ProgressBar mProgressBar;

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_file_select, container, false);
    bucketName = getArguments().getString(BUCKET_NAME);
    bucketId = getArguments().getString(BUCKET_ID);
    bucketCoverImageFilePath = getArguments().getString(BUCKET_COVER_IMAGE_FILE_PATH);
    bucketContentCount = getArguments().getInt(BUCKET_CONTENT_COUNT, 0);
    fileType =
        getArguments().getInt(FileLibUtils.FILE_TYPE_TO_CHOOSE, FileLibUtils.FILE_TYPE_IMAGES);
    setUpActionBar();
    initialiseViews();
    bindEvents();
    fetchFiles();
    return mRootView;
  }

  private void bindEvents() {
    /**
     * アルバム追加ボタン押下時イベント
     */
    mAdddFilesButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
          Intent intent = new Intent();
          intent.putExtra("selectedBucketId", bucketId);
          intent.putExtra("selectedRepresentImageFilePath", bucketCoverImageFilePath);
          intent.putExtra("selectedBuckedName", bucketName);
          getActivity().setResult(Activity.RESULT_OK, intent);
          getActivity().finish();
      }
    });

    // 各種タッチイベント
    final GestureDetector gestureDetector =
        new GestureDetector(getActivity(), FileSelectFragment.this);
    mFilesListView.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        View view = mFilesListView.findChildViewUnder(event.getX(), event.getY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
          case MotionEvent.ACTION_DOWN:

            mDownX = event.getX();
            mDownY = event.getY();
            isOnClick = true;
            break;
          case MotionEvent.ACTION_MOVE:

            if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD
                || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
              isOnClick = false;
            }
            break;
        }
        return gestureDetector.onTouchEvent(event);
      }
    });
  }

  private void fetchFiles() {
    Single<Boolean> fileItems = Single.fromCallable(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        files.clear();
        files.addAll(FileLibUtils.getFilesInBucket(getActivity(), bucketId, fileType));
        return true;
      }
    });
    fileItems.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<Boolean>() {
          @Override public void onSubscribe(@NonNull Disposable d) {
            mProgressBar.setVisibility(View.VISIBLE);
            mFilesListView.setVisibility(View.GONE);
          }

          @Override public void onSuccess(@NonNull Boolean aBoolean) {
            titleView.setText(
                getResources().getString(R.string.selected_item_count, bucketName, files.size()));
            mFilesListAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            mFilesListView.setVisibility(View.VISIBLE);
          }

          @Override public void onError(@NonNull Throwable e) {
            Log.e("Error loading file ", e.getMessage());
          }
        });
  }

  private void initialiseViews() {
    mProgressBar = (ProgressBar) mRootView.findViewById(R.id.file_load_pb);
    mAdddFilesButton = (TextView) mRootView.findViewById(R.id.add_file_button);
    mFilesListView = (RecyclerView) mRootView.findViewById(R.id.files_list);
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
    mFilesListView.setLayoutManager(gridLayoutManager);
    mFilesListAdapter = new FilesListAdapter(getActivity(), files, selectedItemChangeListener);
    mFilesListView.setAdapter(mFilesListAdapter);
  }

  public FilesListAdapter.SelectedItemChangeListener selectedItemChangeListener =
      new FilesListAdapter.SelectedItemChangeListener() {
        @Override
        public void onSelectedItemsCountChanged() {
          // Add Fileイベント。現在は使用しない為、何もしない。
        }
      };

  private void setUpActionBar() {
    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.file_chooser_toolbar);
    toolbar.setTitle("");
    titleView = (TextView) toolbar.findViewById(R.id.file_chooser_toolBarTitle);
  }

  @Override public boolean onDown(MotionEvent e) {
    return true;
  }

  @Override public void onShowPress(MotionEvent e) {
  }

  @Override public boolean onSingleTapUp(MotionEvent e) {
    return false;
  }

  // ファイル一覧画面スクロール時
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override public void onLongPress(MotionEvent e) {
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return false;
  }

  //    @Override
  //    public void onBackPressed() {
  //        Intent result = new Intent();
  //        result.putExtra(BUCKET_ID, bucketId);
  //        result.putExtra(BUCKET_CONTENT_COUNT, files.size());
  //        setResult(Activity.RESULT_OK, result);
  //        super.onBackPressed();
  //    }
}
