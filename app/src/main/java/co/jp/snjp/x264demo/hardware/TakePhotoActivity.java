package co.jp.snjp.x264demo.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.jp.snjp.x264demo.R;
import co.jp.snjp.x264demo.utils.FileUtils;
import co.jp.snjp.x264demo.utils.PermissionUtils;
import co.jp.snjp.x264demo.view.ShutterButton;

public class TakePhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView surfaceView;
    SurfaceHolder holder;

    private Camera mCamera;
    private boolean mPreviewRunning;

    private int cameraId;

    private boolean isShutter;

    private ShutterButton btn;

    private ImageButton confirm, cancel;

    //原始图片目录
    private final String BMP_PATH = Environment.getExternalStorageDirectory().toString() + "/bmp/";

    private final String HYWAY_HYWAY_YUV_PATH = Environment.getExternalStorageDirectory().toString() + "/hyway_hyway_yuv/";

    private byte[] currentData;

    Camera.Parameters parameters;

    private int pic_height = 720;
    private int pic_width = 1280;
    private int format = ImageFormat.JPEG;

    List<Integer> supportedPictureFormats;
    List<Integer> supportedPreviewFormats;
    List<Camera.Size> previewList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.checkPermission(this, Manifest.permission.CAMERA, 111);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_photo);
        initView();
    }

    private void initView() {
        surfaceView = findViewById(R.id.camera_surfaceView);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        btn = findViewById(R.id.btn_takePhoto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isShutter = true;
                btn.setEnabled(false);

                mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                         bmp = rotateBitmapByDegree(bmp, 90);
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                        data = bos.toByteArray();
//                        try {
//                            bos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        String fileName = System.currentTimeMillis() + ".jpg";
                        FileUtils.writeFile(new File(BMP_PATH + fileName), data, false);
                        finish();
                    }
                });
            }
        });
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.cancelAutoFocus();
                doAutoFocus();
            }
        });
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


    private void openCamera() {
        if (findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_BACK) != -1) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_FRONT) != -1) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
    }

    private void closeCamera() {
        if (mCamera != null) {
            mPreviewRunning = false;
            mCamera.release();
        }
    }

    /**
     * 查找摄像头
     *
     * @param camera_facing 按要求查找，镜头是前还是后
     * @return -1表示找不到
     */
    private int findBackOrFrontCamera(int camera_facing) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == camera_facing) {
                return camIdx;
            }
        }
        return -1;
    }

    private void doAutoFocus() {
        parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    if (!Build.MODEL.equals("KORIDY H30")) {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        camera.setParameters(parameters);
                    } else {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(parameters);
                    }
                }
            }
        });
    }

    /**
     * 初始化摄像头
     *
     * @param holder
     */
    private void initCamera(SurfaceHolder holder) {
        Log.e("TAG", "initCamera");
        if (mPreviewRunning)
            mCamera.stopPreview();

        Camera.Parameters parameters;
        try {
            //获取预览的各种分辨率
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        supportedPictureFormats = parameters.getSupportedPictureFormats();
        supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        previewList = parameters.getSupportedPreviewSizes();
        //设置预览尺寸
        parameters.setPreviewSize(pic_width, pic_height);
        //设置拍照尺寸
        parameters.setPictureSize(pic_width, pic_height);
        // 设置照片格式
        parameters.setPictureFormat(format);
        //设置预览格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        //配置camera参数
        mCamera.setParameters(parameters);
        setCameraDisplayOrientation(this, cameraId, mCamera);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        mPreviewRunning = true;
    }

    /**
     * 设置旋转角度
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public static void startActivityForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, TakePhotoActivity.class), requestCode);
    }

    public void confirm(View view) {
        String fileName = System.currentTimeMillis() + ".yuv";
        FileUtils.writeFile(new File(HYWAY_HYWAY_YUV_PATH + fileName), currentData, false);
        btn.setEnabled(true);
        mCamera.startPreview();
        confirm.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
    }

    public void cancel(View view) {
        btn.setEnabled(true);
        mCamera.startPreview();
        currentData = null;
        confirm.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
    }

    public void config(View view) {
        ArrayList<String> sizes = new ArrayList<>();
        for (int i = 0; i < previewList.size(); i++) {
            sizes.add(previewList.get(i).width + "x" + previewList.get(i).height);
        }
        ArrayList<String> formats = new ArrayList<>();
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            int format = supportedPictureFormats.get(i);
            switch (format) {
                case ImageFormat.YUV_420_888:
                    formats.add("YUV_420_888");
                    break;
                case ImageFormat.JPEG:
                    formats.add("JPEG");
                    break;
                case ImageFormat.YV12:
                    formats.add("YV12");
                    break;
                case ImageFormat.NV21:
                    formats.add("NV21");
                    break;
                default:
                    formats.add("NONE");
                    break;

            }
        }
        Intent intent = new Intent(this, SettingsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("image_size", sizes);
        bundle.putStringArrayList("image_format", formats);
        intent.putExtras(bundle);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            String[] split = data.getStringExtra("size").split("x");
            pic_height = Integer.parseInt(split[0]);
            pic_width = Integer.parseInt(split[1]);
            switch (data.getStringExtra("format")) {
                case "JPEG":
                    format = ImageFormat.JPEG;
                    break;
                case "YUV_420_888":
                    format = ImageFormat.YUV_420_888;
                    break;
                case "YV12":
                    format = ImageFormat.YV12;
                    break;
                case "NV21":
                    format = ImageFormat.NV21;
                    break;
                default:
                    break;
            }
            holder.setFixedSize(pic_height, pic_width);
            holder.setFormat(format);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isShutter) {
            mCamera.stopPreview();
            isShutter = false;
            currentData = data;
            confirm.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            openCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
