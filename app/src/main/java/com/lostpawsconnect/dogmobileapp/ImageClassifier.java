package com.lostpawsconnect.dogmobileapp;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifier {

    private static final int IMAGE_SIZE = 128;
    private static final int NUM_CHANNELS = 3;
    private static final int NUM_CLASSES = 120;

    private Interpreter tflite;
    public List<String> labels = new ArrayList<>(); // List to hold class labels

    public ImageClassifier(Context context, String modelPath, String labelsPath) {
        try {
            tflite = new Interpreter(loadModelFile(context, modelPath));
            labels = LabelReader.readLabels(context, labelsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(Context context, String labelsPath) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = null;

        try {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(labelsPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return labels;
    }

    // ... (rest of the class remains the same)

    private void preprocessImage(Bitmap bitmap, ByteBuffer inputBuffer) {
        inputBuffer.rewind();
        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int pixelValue : pixels) {
            inputBuffer.putFloat((float) ((pixelValue >> 16 & 0xFF) / 255.0));
            inputBuffer.putFloat((float) ((pixelValue >> 8 & 0xFF) / 255.0));
            inputBuffer.putFloat((float) ((pixelValue & 0xFF) / 255.0));
        }
    }

    public String classifyImage(Uri picUri, ContentResolver resolver) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, picUri);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // Normalize and prepare the input buffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CHANNELS * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        preprocessImage(resizedBitmap, inputBuffer);

        // Run inference
        float[][] outputArray = new float[1][NUM_CLASSES];
        tflite.run(inputBuffer, outputArray);

        // Post-process the output (you need to implement this based on your model)
        String result = postprocessOutput(outputArray);

        return result;
    }

    private String postprocessOutput(float[][] outputArray) {
        // Implement post-processing based on your model's output format
        // For example, find the index with the highest probability as the predicted class
        int predictedClassIndex = argmax(outputArray[0]);
        return labels.get(predictedClassIndex);
    }

    private int argmax(float[] array) {
        int maxIndex = 0;
        float maxVal = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }
}

class LabelReader {

    public static List<String> readLabels(Context context, String fileName) {
        List<String> labelsList = new ArrayList<>();

        try {
            AssetManager assetManager = context.getAssets();
            // Open the file from the assets folder
            InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open(fileName));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Read each line from the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                labelsList.add(line);
            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return labelsList;
    }
}
