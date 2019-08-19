package co.jp.snjp.x264demo.encode;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import co.jp.snjp.x264demo.encode.exception.EncoderCreateException;
import co.jp.snjp.x264demo.encode.exception.EncoderEncodeException;
import co.jp.snjp.x264demo.encode.utils.FileUtils;
import co.jp.snjp.x264demo.encode.utils.ImageConvertTools;


public class ImageEncoder {

    private byte[] configByte;

    private MediaCodec mMediaCodec;

    private MediaFormat mMediaFormat;

    private boolean isI420;

    private int generateIndex = 0;

    private final int DEFAULT_FRAMERATE = 1;

    private final int DEFAULT_I_FRAME_INTERVAL = 1;

    private final int DEFAULT_WIDTH = 0;

    private final int DEFAULT_HEIGHT = 0;

    private final int DEFAULT_MAX_BIT_RATE = DEFAULT_HEIGHT * DEFAULT_WIDTH * 3 / 2;

//    private final int DEFAULT_KEY_BIT_RATE = 2500 * 100;//1280*720推荐码率

    private int cacheFrameCount;

    private boolean isFirstFrame = true;

    private boolean isNew = false;

    private String mimeType;

    private int compressRatio;

    private int width = DEFAULT_WIDTH;

    private int height = DEFAULT_HEIGHT;

    private int max_bit_rate = DEFAULT_MAX_BIT_RATE;

    //需要编码的图像类型
    public static final int TYPE_YUV = 0;
    public static final int TYPE_JPG = 1;
    public static final int TYPE_BMP = 2;
    private int color_format;

    private List<Camera.Size> supportImageSizes;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ImageEncoder(String mimeType, int compressRatio) throws Exception {
        this.mimeType = mimeType;
//        this.isNew = isNew;
//        this.width = width;
//        this.height = height;
        this.compressRatio = compressRatio;

//        mMediaCodec = MediaCodec.createEncoderByType(mimeType);
//        mMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
//        //码率越低，图片越模糊
//        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, max_bit_rate * compressRatio / 100);
//        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAMERATE);
//        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL);
        color_format = getSupportColorFormat();
//        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);
//
//        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        mMediaCodec.start();
        isFirstFrame = true;
        cacheFrameCount = 0;
        generateIndex = 0;

        try {
            Camera mCamera = Camera.open(0);
            Camera.Parameters params = mCamera.getParameters();

            supportImageSizes = params.getSupportedPreviewSizes();
            for (int i = 0; i < supportImageSizes.size(); i++) {
                Log.d("ENCODER", "SupportedPreviewSizes : " + supportImageSizes.get(i).width + "x" + supportImageSizes.get(i).height);
            }
            mCamera.release();
        } catch (Exception ignored) {
        }

    }

    /**
     * 获取支持的颜色格式
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getSupportColorFormat() {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    System.out.println("found");
                    found = true;
                }
            }
            if (!found)
                continue;
            codecInfo = info;
        }

        // Find a color profile that the codec supports
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");

        if (isNew) {
            //优先使用新版本的API，兼容性差，解码效果更好
            for (int i = 0; i < capabilities.colorFormats.length; i++) {

                switch (capabilities.colorFormats[i]) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    default:
                        Log.d("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                        break;
                }
            }
        } else {
            //优先使用旧版本API，兼容性强，解码效果较差(背景是灰色)
            for (int i = capabilities.colorFormats.length - 1; i >= 0; i--) {

                switch (capabilities.colorFormats[i]) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    default:
                        Log.d("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                        break;
                }
            }
        }
        return 0;
    }

    /**
     * 编码一张图片
     *
     * @param file
     * @param isReset 是否重置编码器
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public byte[] encoderFile(File file, boolean isReset) throws EncoderCreateException {
        byte[] yuvData;
        byte[] fileData = FileUtils.readFile4Bytes(file);
        if (fileData == null)
            return null;

        if (file.getName().endsWith(".bmp")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//这个参数设置为true才有效，
            BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
            //获取图片的宽高
            int height = options.outHeight;
            int width = options.outWidth;
            if (this.width != width || this.height != height)
                resetEncoder(width, height);
            else if (isReset)
                resetEncoder(width, height);

            if (isI420)
                yuvData = ImageConvertTools.convertI420(fileData, width, height);
            else
                yuvData = ImageConvertTools.convertNV12(fileData, width, height);
        } else if (file.getName().endsWith(".jpg")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            //       options.inSampleSize = 1;   //width，hight设为原来的十分一
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
            //获取图片的宽高
            int height = options.outHeight;
            int width = options.outWidth;

            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            byte[] bmpData = ImageConvertTools.pixels2bmp(pixels, width, height);

            if (this.width != width || this.height != height)
                resetEncoder(width, height);
            else if (isReset)
                resetEncoder(width, height);
            if (isI420)
                yuvData = ImageConvertTools.convertI420(bmpData, width, height);
            else
                yuvData = ImageConvertTools.convertNV12(bmpData, width, height);
        } else if (file.getName().endsWith(".yuv")) {
            if (isReset)
                resetEncoder(width, height);

            if (!isI420)
                yuvData = ImageConvertTools.I420toNV12(fileData);
            else
                yuvData = fileData;
        } else {
            return null;
        }
        int size = width * height * 3 / 2;
        if (yuvData.length > size) {
            byte[] src = yuvData;
            yuvData = new byte[size];
            System.arraycopy(src, 0, yuvData, 0, size);
        }

        byte[] h264Data = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        try {
            //针对某些设备会缓存几帧再编码，因此需要判断是否是第一帧并记录缓存的帧数。
            if (isFirstFrame) {
                while (true) {
                    if (mMediaCodec != null) {
                        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                            } else {
                                inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                            }
                            if (inputBuffer != null && yuvData != null) {
                                inputBuffer.clear();
                                inputBuffer.put(yuvData);
                                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                                generateIndex++;
                                cacheFrameCount++;
                            }
                        }

                        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            } else {
                                outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                            }
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                byte[] buffer = new byte[bufferInfo.size];
                                outputBuffer.get(buffer, 0, buffer.length);

                                if (bufferInfo.flags == 2) {
                                    configByte = new byte[bufferInfo.size];
                                    configByte = buffer;
                                } else {// if (bufferInfo.flags == 1)
                                    h264Data = new byte[buffer.length + configByte.length];
                                    System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                    System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                    cacheFrameCount--;
                                }
                            }

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                        if (h264Data != null) {
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i < cacheFrameCount + 1; i++) {
                    if (mMediaCodec != null) {
                        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                            } else {
                                inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                            }
                            if (inputBuffer != null && yuvData != null) {
                                inputBuffer.clear();
                                inputBuffer.put(yuvData);
                                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                                generateIndex++;
                                cacheFrameCount++;
                            }
                        }

                        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            } else {
                                outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                            }
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                byte[] buffer = new byte[bufferInfo.size];
                                outputBuffer.get(buffer, 0, buffer.length);

                                if (bufferInfo.flags == 2) {
                                    configByte = new byte[bufferInfo.size];
                                    configByte = buffer;
                                } else {// if (bufferInfo.flags == 1)
                                    h264Data = new byte[buffer.length + configByte.length];
                                    System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                    System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                    cacheFrameCount--;
                                }
                            }

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            try {
                throw new EncoderEncodeException();
            } catch (EncoderEncodeException e1) {
                e1.printStackTrace();
            }
        }
        isFirstFrame = false;
        return h264Data;
    }

    /**
     * 编码一张图片
     *
     * @param yuvData
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private byte[] encoderData(byte[] yuvData) throws EncoderEncodeException {

        byte[] h264Data = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        try {
            //针对某些设备会缓存几帧再编码，因此需要判断是否是第一帧并记录缓存的帧数。
            if (isFirstFrame) {
                while (true) {
                    if (mMediaCodec != null) {
                        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                            } else {
                                inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                            }
                            if (inputBuffer != null && yuvData != null) {
                                inputBuffer.clear();
                                inputBuffer.put(yuvData);
                                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                                generateIndex++;
                                cacheFrameCount++;
                            }
                        }

                        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            } else {
                                outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                            }
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                byte[] buffer = new byte[bufferInfo.size];
                                outputBuffer.get(buffer, 0, buffer.length);

                                if (bufferInfo.flags == 2) {
                                    configByte = new byte[bufferInfo.size];
                                    configByte = buffer;
                                } else {// if (bufferInfo.flags == 1)
                                    h264Data = new byte[buffer.length + configByte.length];
                                    System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                    System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                    cacheFrameCount--;
                                }
                            }

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                        if (h264Data != null) {
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i < cacheFrameCount + 1; i++) {
                    if (mMediaCodec != null) {
                        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                            } else {
                                inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                            }
                            if (inputBuffer != null && yuvData != null) {
                                inputBuffer.clear();
                                inputBuffer.put(yuvData);
                                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                                generateIndex++;
                                cacheFrameCount++;
                            }
                        }

                        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            } else {
                                outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                            }
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                byte[] buffer = new byte[bufferInfo.size];
                                outputBuffer.get(buffer, 0, buffer.length);

                                if (bufferInfo.flags == 2) {
                                    configByte = new byte[bufferInfo.size];
                                    configByte = buffer;
                                } else {// if (bufferInfo.flags == 1)
                                    h264Data = new byte[buffer.length + configByte.length];
                                    System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                    System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                    cacheFrameCount--;
                                }
                            }

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new EncoderEncodeException();
        } finally {
            release();
        }
        isFirstFrame = false;
        return h264Data;
    }

    public byte[] encodeImage(File file, int type) throws EncoderEncodeException, EncoderCreateException {
        byte[] fileData = FileUtils.readFile4Bytes(file);
        return encodeImage(fileData, type);
    }

    public byte[] encodeImage(byte[] fileData, int type) throws EncoderCreateException, EncoderEncodeException {
        byte[] yuvData = null;
        BitmapFactory.Options options;
        int height = 0;
        int width = 0;
        int size;

        try {
            switch (type) {
                case TYPE_YUV:
                    if (!isI420)
                        yuvData = ImageConvertTools.I420toNV12(fileData);
                    else
                        yuvData = fileData;
                    break;
                case TYPE_JPG:
                    options = new BitmapFactory.Options();
                    options.inScaled = false;
                    //       options.inSampleSize = 1;   //width，hight设为原来的十分一
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
                    //获取图片的宽高
                    height = options.outHeight;
                    width = options.outWidth;

                    if (width < 0 || height < 0)
                        throw new EncoderEncodeException();

                    if (this.width != width || this.height != height)
                        try {
                            resetEncoder(width, height);
                        } catch (Exception e) {
                            //分辨率不支持需要缩放图片
                            int scaledWidth = 0;
                            int scaledHeight = 0;
                            //优先选择相机支持的分辨率，如果没有找到就使用默认的1280分辨率
                            if (supportImageSizes != null)
                                for (int i = 0; i < supportImageSizes.size(); i++) {
                                    float result = width > height ? width * 1.0f * supportImageSizes.get(i).height / height / supportImageSizes.get(i).width : width * 1.0f * supportImageSizes.get(i).width / height / supportImageSizes.get(i).height;
                                    if (result == 1.0f) {
                                        scaledWidth = width > height ? supportImageSizes.get(i).width : supportImageSizes.get(i).height;
                                        scaledHeight = scaledWidth == supportImageSizes.get(i).width ? supportImageSizes.get(i).height : supportImageSizes.get(i).width;
                                        break;
                                    }
                                }
                            else {
                                if (width > height) {
                                    scaledWidth = 1280;
                                    scaledHeight = (int) (height * 1.0 / width * 1280);
                                } else {
                                    scaledHeight = 1280;
                                    scaledWidth = (int) (width * 1.0 / height * 1280);
                                }
                            }
                            try {
                                bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
                            } catch (Exception e1) {
                                throw new EncoderCreateException("width:" + scaledWidth + ",height:" + scaledHeight);
                            }
                            width = scaledWidth;
                            height = scaledHeight;
                            if (this.width != width || this.height != height)
                                resetEncoder(width, height);
                        }

                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    bitmap.recycle();
                    byte[] bmpData = ImageConvertTools.pixels2bmp(pixels, width, height);

                    if (isI420)
                        yuvData = ImageConvertTools.convertI420(bmpData, width, height);
                    else
                        yuvData = ImageConvertTools.convertNV12(bmpData, width, height);

                    size = width * height * 3 / 2;
                    if (yuvData.length > size) {
                        byte[] src = yuvData;
                        yuvData = new byte[size];
                        System.arraycopy(src, 0, yuvData, 0, size);
                    }
                    break;
                case TYPE_BMP:
                    options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;//这个参数设置为true才有效，
                    BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
                    //获取图片的宽高
                    height = options.outHeight;
                    width = options.outWidth;

                    if (width < 0 || height < 0)
                        throw new EncoderEncodeException();

                    if (this.width != width || this.height != height)
                        resetEncoder(width, height);

                    if (isI420)
                        yuvData = ImageConvertTools.convertI420(fileData, width, height);
                    else
                        yuvData = ImageConvertTools.convertNV12(fileData, width, height);

                    size = width * height * 3 / 2;
                    if (yuvData.length > size) {
                        byte[] src = yuvData;
                        yuvData = new byte[size];
                        System.arraycopy(src, 0, yuvData, 0, size);
                    }
                    break;
            }
        } catch (EncoderCreateException e) {
            throw new EncoderCreateException("width:" + width + ",height:" + height);
        } catch (Exception e) {
            throw new EncoderEncodeException();
        }
        return encoderData(yuvData);
    }

    /**
     * 图像宽高变化，需要重新设置编码器
     *
     * @param width
     * @param height
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void resetEncoder(int width, int height) throws EncoderCreateException {
        try {
            release();
            this.width = width;
            this.height = height;
            this.max_bit_rate = width * height * 3 / 2;
            mMediaCodec = MediaCodec.createEncoderByType(mimeType);
            if (mMediaFormat != null) {
                mMediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
                mMediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
                mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, max_bit_rate / compressRatio);

                mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mMediaCodec.start();
                isFirstFrame = true;
                cacheFrameCount = 0;
                generateIndex = 0;
            } else {
                mMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
                //码率越低，图片越模糊
                mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, max_bit_rate / compressRatio);
                mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAMERATE);
                mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL);
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);

                mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mMediaCodec.start();
                isFirstFrame = true;
                cacheFrameCount = 0;
                generateIndex = 0;
            }
        } catch (Exception e) {
            throw new EncoderCreateException();
        }
    }

    /**
     * 停止硬件编码
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void stopEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
        }
    }

    /**
     * release all resource that used in Encoder
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / DEFAULT_FRAMERATE;
    }


}
