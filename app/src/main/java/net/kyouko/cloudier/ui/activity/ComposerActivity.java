package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.squareup.picasso.Picasso;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.ImageHostingResponse;
import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.TweetResult;
import net.kyouko.cloudier.model.UploadImageResult;
import net.kyouko.cloudier.ui.widget.listener.TweetTextCountWatcher;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.DateTimeUtil;
import net.kyouko.cloudier.util.FileUtil;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.PreferenceUtil;
import net.kyouko.cloudier.util.RequestUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposerActivity extends AppCompatActivity {

    public final static int TYPE_NEW = 0;
    public final static int TYPE_COMMENT = 1;
    public final static int TYPE_RETWEET = 2;

    public final static int REQUEST_IMAGE_PICKER = 0;

    private final static String[] FILE_PROJECTION = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA};


    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.background) View background;
    @BindView(R.id.composer) CardView cardView;
    @BindView(R.id.avatar) SimpleDraweeView avatar;
    @BindView(R.id.nickname) TextView nickname;
    @BindView(R.id.username) TextView username;
    @BindView(R.id.content) EditText content;
    @BindView(R.id.source_card) CardView sourceCard;
    @BindView(R.id.source_nickname) TextView sourceNickname;
    @BindView(R.id.source_time) TextView sourceTime;
    @BindView(R.id.source_content) TextView sourceContent;
    @BindView(R.id.word_count) TextView wordCount;
    @BindView(R.id.images_wrapper) HorizontalScrollView imagesWrapper;
    @BindView(R.id.images) LinearLayout imagesLayout;
    @BindView(R.id.button_add_image) View addImageButton;
    @BindView(R.id.button_add_image_icon) ImageView addImageButtonIcon;
    @BindView(R.id.button_add_topic) View addTopicButton;

    private Account account;

    private int composerType = TYPE_NEW;
    private SourceTweet sourceTweet;

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private List<UploadImageView> uploadImageViews = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();

    private MenuItem sendMenuItem;
    private ImageView sendButton;
    private ProgressBar progressBar;

    private int shortAnimationDuration;

    private View.OnClickListener onSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (hasWordCountExceeded()) {
                playWordCountExceededAnimation();
            } else {
                switch (composerType) {
                    case TYPE_NEW:
                    default:
                        uploadImagesAndSendTweet(false);
                        break;
                    case TYPE_COMMENT:
                        comment();
                        break;
                    case TYPE_RETWEET:
                        uploadImagesAndSendTweet(true);
                        break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        ButterKnife.bind(this);

        fetchComposerType();

        initView();
        if (getIntent().getAction() != null) {
            if ((getIntent().getAction().equals(Intent.ACTION_SEND) ||
                    getIntent().getAction().equals(Intent.ACTION_SEND_MULTIPLE)) &&
                    getIntent().getType() != null) {
                fetchSharedContent();
            }
        }
    }


    private void fetchComposerType() {
        composerType = getIntent().getIntExtra("TYPE", TYPE_NEW);
        switch (composerType) {
            case TYPE_NEW:
            default:
                setTitle(R.string.title_activity_composer);
                break;
            case TYPE_COMMENT:
                setTitle(R.string.title_activity_composer_comment);
                break;
            case TYPE_RETWEET:
                setTitle(R.string.title_activity_composer_retweet);
                break;
        }
    }


    private void initView() {
        initToolbar();
        initComposer();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelComposing();
            }
        });
    }


    private void initComposer() {
        account = AuthUtil.readAccount(this);

        avatar.setImageURI(Uri.parse(ImageUtil.getInstance(this).parseImageUrl(account.avatarUrl)));
        nickname.setText(account.nickname);
        username.setText(getString(R.string.text_pattern_username, account.username));

        switch (composerType) {
            case TYPE_NEW:
            default:
                content.setHint(R.string.text_hint_composer_content);
                break;
            case TYPE_COMMENT:
            case TYPE_RETWEET:
                content.setHint(R.string.text_hint_composer_content_comment);
                break;
        }

        TweetTextCountWatcher watcher = new TweetTextCountWatcher(wordCount);
        content.addTextChangedListener(watcher);
        watcher.applyWordCountAvailable(content.getText().toString());
        content.setText(getIntent().getStringExtra("CONTENT"));

        wordCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (hasWordCountExceeded()) {
                    content.setText(content.getText().subSequence(0, content.getText().length() - 1));
                }
                content.setSelection(content.length());
            }
        });

        if (composerType == TYPE_COMMENT || composerType == TYPE_RETWEET) {
            sourceTweet = (SourceTweet) getIntent().getSerializableExtra("TWEET");
            sourceCard.setVisibility(View.VISIBLE);

            String sourceTweetNickname = getIntent().getStringExtra("SOURCE_NICKNAME");
            if (sourceTweetNickname != null && sourceTweetNickname.length() > 0) {
                sourceNickname.setText(sourceTweetNickname);
            } else {
                if (sourceTweet instanceof Tweet && ((Tweet) sourceTweet).sourceTweet != null) {
                    sourceNickname.setText(((Tweet) sourceTweet).sourceTweet.nickname);
                } else {
                    sourceNickname.setText(sourceTweet.nickname);
                }
            }

            String sourceTweetTime = getIntent().getStringExtra("SOURCE_TIME");
            if (sourceTweetTime != null && sourceTweetTime.length() > 0) {
                sourceTime.setText(sourceTweetTime);
            } else {
                if (sourceTweet instanceof Tweet && ((Tweet) sourceTweet).sourceTweet != null) {
                    sourceTime.setText(DateTimeUtil.getDateTimeDescription(this,
                            ((Tweet) sourceTweet).sourceTweet.timestamp));
                } else {
                    sourceTime.setText(DateTimeUtil.getDateTimeDescription(this, sourceTweet.timestamp));
                }
            }

            String sourceTweetContent = getIntent().getStringExtra("SOURCE_CONTENT");
            if (sourceTweetContent != null && sourceTweetContent.length() > 0) {
                sourceContent.setText(sourceTweetContent);
                sourceContent.setVisibility(View.VISIBLE);
            } else {
                sourceContent.setVisibility(View.GONE);
            }

            if (composerType == TYPE_RETWEET) {
                addImageButton.setVisibility(View.VISIBLE);
                addImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (imageUris.size() < 9) {
                            pickImage();
                        }
                    }
                });
            } else {
                addImageButton.setVisibility(View.GONE);
            }
        } else {
            sourceCard.setVisibility(View.GONE);

            addImageButton.setVisibility(View.VISIBLE);
            addImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imageUris.size() < 9) {
                        pickImage();
                    }
                }
            });
        }

        addTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectionStart = content.getSelectionStart();
                int selectionEnd = content.getSelectionEnd();
                String topic = getString(R.string.text_pattern_topic);
                content.setText(content.getText().replace(selectionStart, selectionEnd, topic));
                content.setSelection(selectionStart + 1, selectionStart + topic.length() - 1);
            }
        });

        if (content.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            content.setSelection(0, 0);
        }
    }


    private void fetchSharedContent() {
        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (action.equals(Intent.ACTION_SEND)) {
            if (type.equals("text/plain")) {
                String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    content.setText(sharedText);
                }
            } else if (type.startsWith("image/")) {
                Uri sharedImageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (sharedImageUri != null) {
                    imageUris.add(sharedImageUri);
                    displayImages();
                }
            }
        } else if (action.equals(Intent.ACTION_SEND_MULTIPLE) && type.startsWith("image/")) {
            ArrayList<Uri> sharedImageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (sharedImageUris != null) {
                imageUris.addAll(sharedImageUris);
                displayImages();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer, menu);

        sendMenuItem = menu.findItem(R.id.action_send);

        sendButton = (ImageView) menu.findItem(R.id.action_send).getActionView();
        if (sendButton != null) {
            int sendImageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    getResources().getDisplayMetrics());
            sendButton.setLayoutParams(new ViewGroup.LayoutParams(sendImageSize, sendImageSize));

            int progressBarSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                    getResources().getDisplayMetrics());
            int progressBarPaddingEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    16, getResources().getDisplayMetrics());
            progressBar = new ProgressBar(this);
            progressBar.setLayoutParams(new ViewGroup.LayoutParams(
                    progressBarSize + progressBarPaddingEnd, progressBarSize));
            progressBar.setPadding(0, 0, progressBarPaddingEnd, 0);

            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);


            sendButton.setScaleType(ImageView.ScaleType.CENTER);

            int[] attrs = new int[]{android.R.attr.actionBarItemBackground};
            TypedArray typedArray = obtainStyledAttributes(attrs);
            sendButton.setBackground(typedArray.getDrawable(0));
            typedArray.recycle();

            sendButton.setImageResource(R.drawable.ic_send_white_24dp);
            sendButton.setOnClickListener(onSendButtonClickListener);
        }
        return true;
    }


    private void playWordCountExceededAnimation() {
        ViewAnimator
                .animate(wordCount)
                .translationX(0, 25, -25, 0)
                .repeatCount(2)
                .duration(100)
                .start();
    }


    private void playSendingTweetAnimation() {
        sendButton.setOnClickListener(null);

        final float sendButtonX = sendButton.getX();

        ViewAnimator
                .animate(sendButton)
                .translationX(sendButton.getWidth())
                .interpolator(new AccelerateInterpolator())
                .duration(350)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        sendMenuItem.setActionView(progressBar);
                        sendButton.setX(sendButtonX);
                        ViewAnimator
                                .animate(progressBar)
                                .scale(1)
                                .duration(shortAnimationDuration)
                                .start();
                    }
                })
                .start();
    }


    private void playSendingFailedAnimation() {
        final float sendButtonX = sendButton.getX();
        ViewAnimator
                .animate(progressBar)
                .duration(shortAnimationDuration)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        sendMenuItem.setActionView(sendButton);
                        ViewAnimator
                                .animate(sendButton)
                                .translationX(sendButtonX - sendButton.getWidth(), sendButtonX)
                                .interpolator(new DecelerateInterpolator())
                                .duration(350)
                                .onStop(new AnimationListener.Stop() {
                                    @Override
                                    public void onStop() {
                                        sendButton.setOnClickListener(onSendButtonClickListener);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }


    private boolean hasWordCountExceeded() {
        return (TweetTextCountWatcher.getWordCountAvailable(content.getText().toString()) < 0);
    }


    private void pickImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .folderTitle(getString(R.string.title_activity_image_picker_folders))
                .single()
                .showCamera(true)
                .start(REQUEST_IMAGE_PICKER);
    }


    private void displayImages() {
        if (imageUris.size() < 9) {
            addImageButtonIcon.setImageDrawable(getDrawable(R.drawable.ic_image_black_54alpha_24dp));
        }

        if (imageUris.isEmpty()) {
            imagesWrapper.setVisibility(View.GONE);
        } else {
            imagesWrapper.setVisibility(View.VISIBLE);
            imagesLayout.removeAllViewsInLayout();
            uploadImageViews.clear();

            for (int i = 0; i < imageUris.size(); i += 1) {
                final int imageIndex = i;
                Uri imageUri = imageUris.get(imageIndex);

                View view = getLayoutInflater().inflate(R.layout.template_composer_image, imagesLayout, false);
                UploadImageView uploadImageView = new UploadImageView(view);

                Picasso.with(this)
                        .load(imageUri)
                        .placeholder(R.color.grey_300)
                        .fit()
                        .centerCrop()
                        .into(uploadImageView.image);
                uploadImageView.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageUris.remove(imageIndex);
                        displayImages();
                    }
                });

                imagesLayout.addView(view);
                uploadImageViews.add(uploadImageView);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> selectedImages =
                    data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            if (selectedImages.size() == 1) {
                imageUris.add(Uri.parse("file://" + selectedImages.get(0).getPath()));
            }
            if (imageUris.size() == 9) {
                addImageButtonIcon.setImageDrawable(getDrawable(R.drawable.ic_image_black_38alpha_24dp));
            }
            displayImages();
        }
    }


    private void uploadImagesAndSendTweet(boolean isRetweet) {
        content.setEnabled(false);

        playSendingTweetAnimation();

        uploadImages(isRetweet);
    }


    private void uploadImages(final boolean isRetweet) {
        imageUrls.clear();

        for (UploadImageView uploadImageView : uploadImageViews) {
            uploadImageView.delete.setVisibility(View.GONE);
        }

        final String imageHostingService = PreferenceUtil.with(this)
                .getString(PreferenceUtil.PREF_IMAGE_HOSTING_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!imageUris.isEmpty()) {
                    for (int i = 0; i < imageUris.size(); i += 1) {
                        Uri imageUri = imageUris.get(i);

                        final UploadImageView uploadImageView = uploadImageViews.get(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadImageView.delete.setVisibility(View.GONE);
                                uploadImageView.progress.getIndeterminateDrawable().setColorFilter(
                                        getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                                uploadImageView.progress.setVisibility(View.VISIBLE);
                            }
                        });

                        File imageFile = null;
                        if (imageUri.getScheme().toLowerCase().equals("content")) {
                            InputStream inputStream = null;
                            try {
                                inputStream = getContentResolver().openInputStream(imageUri);
                                imageFile = FileUtil.writeToTemporaryImageFile(
                                        ComposerActivity.this, inputStream);
                            } catch (FileNotFoundException e) {
                                // Ignore
                            } finally {
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        // Ignore
                                    }
                                }
                            }
                        } else {
                            imageFile = new File(imageUri.getPath());
                        }

                        if (imageFile == null) {
                            continue;
                        }

                        File compressedImageFile = new Compressor.Builder(ComposerActivity.this)
                                .setQuality(75)
                                .setMaxWidth(1920)
                                .setMaxHeight(1080)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .build()
                                .compressToFile(imageFile);
                        RequestBody imageBody = RequestBody.create(MediaType.parse("image/jpeg"),
                                compressedImageFile);
                        Call<ImageHostingResponse> uploadImageCall;
                        if (imageHostingService.equals(ImageUtil.IMAGE_HOSTING_SERVICE_IMGUR)) {
                            uploadImageCall = RequestUtil.getImgurApiInstance().uploadImage(imageBody);
                        } else {
                            uploadImageCall = RequestUtil.getItorrApiInstance().uploadImage(imageBody);
                        }

                        try {
                            Response<ImageHostingResponse> response = uploadImageCall.execute();
                            if (response.body() != null) {
                                String imgurUrl = response.body().imageUrl;

                                Call<UploadImageResult> uploadImageFromUrlCall =
                                        RequestUtil.getApiInstance().uploadImageFromUrl(
                                                RequestUtil.getConstantParams(),
                                                RequestUtil.getOAuthParams(ComposerActivity.this),
                                                imgurUrl
                                        );
                                Response<UploadImageResult> uploadResponse =
                                        uploadImageFromUrlCall.execute();
                                if (uploadResponse.body() != null) {
                                    imageUrls.add(uploadResponse.body().imageUrl);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            uploadImageView.progress.setVisibility(View.GONE);
                                            uploadImageView.success.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onFailedToUploadImages(isRetweet);
                                        }
                                    });
                                    return;
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFailedToUploadImages(isRetweet);
                                    }
                                });
                                return;
                            }
                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onFailedToUploadImages(isRetweet);
                                }
                            });
                            return;
                        }
                    }
                }
                if (isRetweet) {
                    retweet();
                } else {
                    sendTweet();
                }
            }
        }).start();
    }


    private void onFailedToUploadImages(final boolean isRetweet) {
        content.setEnabled(true);

        Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_upload_images,
                Snackbar.LENGTH_SHORT)
                .setAction(R.string.title_action_retry,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                uploadImagesAndSendTweet(isRetweet);
                            }
                        })
                .show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playSendingFailedAnimation();
            }
        }, 1000);

        displayImages();
    }


    private void sendTweet() {
        String imageUrlsString = "";
        if (!imageUrls.isEmpty()) {
            imageUrlsString = ImageUtil.getInstance(this)
                    .parseImageUrl(imageUrls.get(0), ImageUtil.QUALITY_ORIGINAL);
            for (int i = 1; i < imageUrls.size(); i += 1) {
                imageUrlsString += "," + ImageUtil.getInstance(this)
                        .parseImageUrl(imageUrls.get(i), ImageUtil.QUALITY_ORIGINAL);
            }
        }
        Call<TweetResult> sendTweetCall = RequestUtil.getApiInstance().postTweet(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                content.getText().toString(), imageUrlsString);
        sendTweetCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_send_tweet,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        uploadImagesAndSendTweet(false);
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void comment() {
        content.setEnabled(false);

        playSendingTweetAnimation();

        Call<TweetResult> commentCall = RequestUtil.getApiInstance().comment(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                sourceTweet.id, content.getText().toString());
        commentCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_comment,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        comment();
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void retweet() {
        String imageUrlsString = "";
        if (!imageUrls.isEmpty()) {
            imageUrlsString = ImageUtil.getInstance(this)
                    .parseImageUrl(imageUrls.get(0), ImageUtil.QUALITY_ORIGINAL);
            for (int i = 1; i < imageUrls.size(); i += 1) {
                imageUrlsString += "," + ImageUtil.getInstance(this)
                        .parseImageUrl(imageUrls.get(i), ImageUtil.QUALITY_ORIGINAL);
            }
        }

        Call<TweetResult> retweetCall = RequestUtil.getApiInstance().retweet(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                sourceTweet.id, content.getText().toString(), imageUrlsString);
        retweetCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_retweet,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        retweet();
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void getTweet(String tweetId) {
        Call<Tweet> tweetCall = RequestUtil.getApiInstance().getTweet(
                RequestUtil.getOAuthParams(this), tweetId);
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                finishComposing(response.body());
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                finishComposing(null);
            }
        });
    }


    @Override
    public void onBackPressed() {
        cancelComposing();
    }


    private void cancelComposing() {
        Intent result = new Intent();
        result.putExtra("CONTENT", content.getText().toString());
        setResult(RESULT_CANCELED, result);
        finish();
    }


    private void finishComposing(@Nullable Tweet tweet) {
        Intent result = new Intent();
        result.putExtra("CONTENT", content.getText().toString());
        if (tweet != null) {
            result.putExtra("TWEET", tweet);
        }
        setResult(RESULT_OK, result);
        finish();
    }


    public static class UploadImageView {

        @BindView(R.id.image) ImageView image;
        @BindView(R.id.delete) ImageButton delete;
        @BindView(R.id.progress) ProgressBar progress;
        @BindView(R.id.success) ImageView success;

        UploadImageView(View view) {
            ButterKnife.bind(this, view);
        }

    }

}
