package block.com.blockchain.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;

import block.com.blockchain.R;
import block.com.blockchain.bean.MotifyUserBean;
import block.com.blockchain.bean.ResultInfo;
import block.com.blockchain.bean.UserBean;
import block.com.blockchain.customview.BasicEditInfoView;
import block.com.blockchain.customview.BasicInfoView;
import block.com.blockchain.request.HttpConstant;
import block.com.blockchain.request.HttpSendClass;
import block.com.blockchain.request.SenUrlClass;
import block.com.blockchain.utils.DialogUtil;
import block.com.blockchain.utils.FileUtils;
import block.com.blockchain.utils.PhoneAdapterUtils;
import block.com.blockchain.utils.PicUtils;
import block.com.blockchain.utils.SDCardUtils;
import block.com.blockchain.utils.ScreenUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ts on 2018/5/14.
 */

public class MyInfoActivity extends BaseActivity {

    @BindView(R.id.small_img)
    ImageView smallImg;
    @BindView(R.id.person_nick_name)
    BasicEditInfoView personNickName;
    @BindView(R.id.person_name)
    BasicEditInfoView personName;
    @BindView(R.id.person_phone)
    BasicInfoView personPhone;
    @BindView(R.id.person_sex)
    BasicInfoView personSex;
    @BindView(R.id.person_birthday)
    BasicInfoView personBirthday;
    @BindView(R.id.person_work)
    BasicEditInfoView personWork;
    @BindView(R.id.person_signature)
    BasicEditInfoView personSignature;
    @BindView(R.id.person_title)
    Toolbar personTitle;
    @BindView(R.id.parent_layout)
    LinearLayout parent_layout;

    private File picFile;
    private String upLoadPath = "";
    private PopupWindow popupWindow;//性别选择弹框
    protected static final int IMAGE_ALBUM = 155;// 相册
    protected static final int IMAGE_TAK = 154; // 拍照
    protected static final int IMAGE_CUT = 152; // 裁剪
    protected static final int DATE = 153; // 日期

    private MotifyUserBean oldUserBean = null;
    private AjaxParams motifyParams;//修改后的请求
    private PopupWindow popupWindowDate;
    private String date = "";
    private String tempDate = "";
    private String cutPicPath = "";
    private Uri photoURI = null;

    @Override
    public void init() {
        noStatusBar();
        setContentView(R.layout.activity_my_info);
        ButterKnife.bind(this);
        parent_layout.setPadding(0, HttpConstant.PhoneInfo.STATUS_HEIGHT, 0, 0);
        oldUserBean = (MotifyUserBean) getIntent().getSerializableExtra("user_info");
        if (oldUserBean != null) {
            dataSet(oldUserBean);
        } else {
            getUserInfo();
        }
        smallImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgChoose();
            }
        });
        personSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopwindow("男", "女", "取消", new OnPopWindowClickLisenter() {
                    @Override
                    public void onButtonOne() {
                        oldUserBean.setHas_sex(true);
                        oldUserBean.setSex(1);
                        personSex.setRightMsg("男");
                    }

                    @Override
                    public void onButtonTwo() {
                        oldUserBean.setHas_sex(true);
                        oldUserBean.setSex(0);
                        personSex.setRightMsg("女");
                    }
                });
            }
        });
        personBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePick();
            }
        });
        personTitle.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (oldUserBean != null) {
                    motifyParams = oldUserBean.getMotifyParams();
                    if (motifyParams != null) {
                        showView("修改资料", "资料已经发生改变\n是否保存修改", "确定", "取消");
                    } else {
                        finish();
                    }

                } else {
                    finish();
                }

            }
        });
    }

    /**
     * 获取资料
     */
    private void getUserInfo() {
        AjaxParams params = new AjaxParams();
        params.put("type", 1 + "");
        HttpSendClass.getInstance().getWithToken(params, SenUrlClass.USER_INFO, new
                AjaxCallBack<ResultInfo<MotifyUserBean>>() {
                    @Override
                    public void onSuccess(ResultInfo<MotifyUserBean> resultInfo) {
                        super.onSuccess(resultInfo);
                        if (resultInfo.status.equals("success")) {
                            oldUserBean = resultInfo.data;
                            dataSet(oldUserBean);
                        } else {
                            Toast.makeText(MyInfoActivity.this, resultInfo.message, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, String strMsg) {
                        super.onFailure(t, strMsg);
                        Toast.makeText(MyInfoActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 修改资料
     */
    private void motify(AjaxParams ajaxParams) {
        HttpSendClass.getInstance().postWithToken(ajaxParams, SenUrlClass.MOTIFY_USER, new
                AjaxCallBack<ResultInfo<UserBean>>() {
                    @Override
                    public void onSuccess(ResultInfo<UserBean> resultInfo) {
                        super.onSuccess(resultInfo);
                        if (resultInfo.status.equals("success")) {
                            setResult(RESULT_OK);
                            finish();
                            Toast.makeText(MyInfoActivity.this, getResources().getString(R.string.motify_success), Toast
                                    .LENGTH_SHORT)
                                    .show();
//                            dataSet(resultInfo.data);
                        } else {
                            Toast.makeText(MyInfoActivity.this, resultInfo.message, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, String strMsg) {
                        super.onFailure(t, strMsg);
                        Toast.makeText(MyInfoActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 数据组装
     *
     * @param userBean
     */
    private void dataSet(UserBean userBean) {
        personNickName.setRightMsg(userBean.getNickname());
        personName.setRightMsg(userBean.getReal_name());
        if (userBean.getSex() == 1) {
            personSex.setRightMsg(R.string.person_sex_man);
        } else if (userBean.getSex() == 0) {
            personSex.setRightMsg(R.string.person_sex_woman);
        } else {
            personSex.setRightMsg("");
        }
        personPhone.setRightMsg(userBean.getMobile());
        personBirthday.setRightMsg(userBean.getBirthday());
        personWork.setRightMsg(userBean.getEnterprise());
        personSignature.setRightMsg(userBean.getSelf_sign());
        RequestOptions options = new RequestOptions();
        options.placeholder(R.mipmap.default_head);
        options.error(R.mipmap.default_head);
        Glide.with(this).load(HttpConstant.HTTPHOST + userBean.getPic_url()).apply(options).into(smallImg);

        //放在后面是因为在组装数据时候会调用TEXTWater
        personName.setOnEditChangeListener(new MyTextWatcher(personName));
        personWork.setOnEditChangeListener(new MyTextWatcher(personWork));
        personNickName.setOnEditChangeListener(new MyTextWatcher(personNickName));
        personSignature.setOnEditChangeListener(new MyTextWatcher(personSignature));
    }

    /**
     * 修改数据
     *
     * @param id
     * @param text
     */
    private void setHasChanged(@IdRes int id, String text) {
        switch (id) {
            case R.id.person_name:
                oldUserBean.setHas_name(true);
                oldUserBean.setReal_name(text);
                break;
            case R.id.person_work:
                oldUserBean.setHas_work(true);
                oldUserBean.setEnterprise(text);
                break;
            case R.id.person_nick_name:
                oldUserBean.setHas_nick(true);
                oldUserBean.setNickname(text);
                break;
            case R.id.person_signature:
                oldUserBean.setHas_sign(true);
                oldUserBean.setSelf_sign(text);
                break;
        }
    }

    /**
     * 图片选择弹出框
     */
    private void imgChoose() {
        showPopwindow("拍照", "相册", "取消", new OnPopWindowClickLisenter() {
            @Override
            public void onButtonOne() {
                if (Environment.getExternalStorageState() != null) {
                    String filePath = SDCardUtils.getCacheDir(MyInfoActivity.this) + "/" + System.currentTimeMillis
                            () + ".jpg";
                    picFile = FileUtils.createFile(filePath);
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat
                            .checkSelfPermission(MyInfoActivity.this,
                                    Manifest.permission.CAMERA)) {
                        // 启动拍照,并保存到临时文件
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        photoURI = FileProvider.getUriForFile(MyInfoActivity.this, getApplicationContext()
                                .getPackageName
                                        () + ".provider", picFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, IMAGE_TAK);
                    } else {
                        ActivityCompat.requestPermissions(MyInfoActivity.this, new String[]{Manifest.permission
                                .CAMERA}, 1);
                    }
                } else {
                    // 启动拍照,并保存到临时文件
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
                    startActivityForResult(intent, IMAGE_TAK);

                }
            }

            @Override
            public void onButtonTwo() {
                if (Environment.getExternalStorageState() != null) {
                    String filePath = SDCardUtils.getCacheDir(MyInfoActivity.this) + "/" + System.currentTimeMillis
                            () + ".jpg";
                    picFile = FileUtils.createFile(filePath);
                }
                if (Build.VERSION.SDK_INT >= 23) {

                    if (PackageManager.PERMISSION_GRANTED != ContextCompat
                            .checkSelfPermission(MyInfoActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(MyInfoActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                2);
                    } else {
                        Intent intent2 = new Intent(Intent.ACTION_PICK, null);
                        intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent2, IMAGE_ALBUM);
                    }
                } else {
                    Intent intent2 = new Intent(Intent.ACTION_PICK, null);
                    intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent2, IMAGE_ALBUM);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 启动拍照,并保存到临时文件
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(MyInfoActivity.this, getApplicationContext()
                        .getPackageName
                                () + ".provider", picFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, IMAGE_TAK);
            } else {
                Toast.makeText(MyInfoActivity.this, this.getResources().getString(R.string.app_name), Toast
                        .LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent2 = new Intent(Intent.ACTION_PICK, null);
                intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent2, IMAGE_ALBUM);
            } else {
                Toast.makeText(MyInfoActivity.this, this.getResources().getString(R.string.no_permission), Toast
                        .LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri uri = PhoneAdapterUtils.geturi(data, this);
                    startPhotoZoom(uri, 999, 998);
                }
                break;
            case IMAGE_TAK:
                if (resultCode == RESULT_OK) {
                    startPhotoZoom(photoURI, 999, 998);
                }
                break;
            case IMAGE_CUT:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = BitmapFactory.decodeFile(upLoadPath);
                    String path = null;
                    if (bitmap != null) {
                        path = PicUtils.compressAndSave(bitmap, 0.8f);
                        bitmap.recycle();
                    }
                    if (path != null) {
                        oldUserBean.setPic_url(path);
                    } else {
                        oldUserBean.setPic_url(upLoadPath);
                    }
                    oldUserBean.setHas_url(true);
                    Glide.with(this).load(upLoadPath).into(smallImg);
                }
                break;
            case DATE:
                if (resultCode == RESULT_OK) {
                    date = data.getStringExtra("date");
                    personBirthday.setRightMsg(date);
                    oldUserBean.setBirthday(date);
                    oldUserBean.setHas_birth(true);
                }
                break;
        }
    }

    /**
     * 文本监听
     */
    class MyTextWatcher implements TextWatcher {
        View view;

        public MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setHasChanged(view.getId(), editable.toString());
        }
    }

    /**
     * 获取相册中的绝对路径
     *
     * @param data
     * @return
     */
    private String getAbsPath(Intent data) {
        if (data == null || data.getData() == null) {
            return picFile.getAbsolutePath();
        }

        Uri uri = PhoneAdapterUtils.geturi(data, this);
        String[] proj = {MediaStore.Images.Media.DATA};
        // 好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        // 按我个人理解 这个是获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        // 将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        // 最后根据索引值获取图片路径
        String path = cursor.getString(column_index);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null)
            Toast.makeText(this, "bitmap!=null", Toast.LENGTH_SHORT).show();
        return path;
    }

    /**
     * 性别选择弹出框
     */
    protected void showPopwindow(String text1, String text2, String text3, final OnPopWindowClickLisenter
            onPopWindowClickLisenter) {
        final String textStr1 = TextUtils.isEmpty(text1) == true ? "" : text1;
        final String textStr2 = TextUtils.isEmpty(text1) == true ? "" : text2;
        final String textStr3 = TextUtils.isEmpty(text1) == true ? "" : text3;
        DialogUtil.showPopupWindow(this, R.layout.layout_photo_camera, new DialogUtil.OnEventListener() {
            @Override
            public void eventListener(View parentView, Object window) {
                popupWindow = (PopupWindow) window;
                TextView view1 = (TextView) parentView.findViewById(R.id.tv_from_camera);
                TextView view2 = (TextView) parentView.findViewById(R.id.tv_from_photos);
                TextView view3 = (TextView) parentView.findViewById(R.id.tv_cancel);
                view1.setText(textStr1);
                view2.setText(textStr2);
                view3.setText(textStr3);
                view1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        if (onPopWindowClickLisenter != null)
                            onPopWindowClickLisenter.onButtonOne();

                    }
                });
                view2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        if (onPopWindowClickLisenter != null)
                            onPopWindowClickLisenter.onButtonTwo();

                    }
                });
                view3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                    }
                });
            }
        });
    }

    public interface OnPopWindowClickLisenter {
        void onButtonOne();

        void onButtonTwo();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (oldUserBean != null) {
                motifyParams = oldUserBean.getMotifyParams();
                if (motifyParams != null) {
                    showView("修改资料", "资料已经发生改变\n是否保存修改", "确定", "取消");
                    return true;
                }
            }

        }
        return super.onKeyUp(keyCode, event);
    }

    // 返回弹框
    private void showView(String title, String count, String left, String right) {
        DialogUtil.showPromptDialog(MyInfoActivity.this, title, count, left, null, right,
                new DialogUtil.OnMenuClick() {

                    @Override
                    public void onRightMenuClick() {
                        finish();
                    }

                    @Override
                    public void onLeftMenuClick() {
                        motify(motifyParams);
                    }

                    @Override
                    public void onCenterMenuClick() {


                    }
                }, "");
    }

    /**
     * 日期选择
     */
    protected void showDatePick() {
        Intent intent = new Intent();
        intent.putExtra("date", date);
        intent.setClass(this, DialogActivity.class);
        startActivityForResult(intent, DATE);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void startPhotoZoom(Uri uri, int width, int height) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Int1111ent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", height);
        intent.putExtra("aspectY", width);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", ScreenUtils.getScreenDispaly(this)[0] * height / width);
        intent.putExtra("outputY", ScreenUtils.getScreenDispaly(this)[0]);
        intent.putExtra("scale", true);//黑边
        intent.putExtra("scaleUpIfNeeded", true);//黑边
        intent.putExtra("return-data", false);// 是否返回数据
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        upLoadPath = SDCardUtils.getCacheDir(this) + "/" + System.currentTimeMillis() + ".jpg";
        File file = FileUtils.createFile(upLoadPath);
        Uri cutPathUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cutPathUri);  // 指定目标文件
        startActivityForResult(intent, IMAGE_CUT);
    }
}
