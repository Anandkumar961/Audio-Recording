package com.dipak.audiorecording;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class AudioRecordingActivity extends Activity implements MediaPlayer.OnCompletionListener {
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP};

    private boolean isAudioRecorded = false;
    private String filePath = null;
    private MediaPlayer mediaPlayer;
    private SeekBar songProgressBar;
    private Handler mHandler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setButtonHandlers();
        enableButtons(false);
        setFormatButtonCaption();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        songProgressBar = (SeekBar) findViewById(R.id.seekBar);
        songProgressBar.setClickable(false);
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnPlay)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnFormat)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnFormat, !isRecording);
        enableButton(R.id.btnStop, (isRecording && isAudioRecorded) ? isAudioRecorded : isRecording);
        enableButton(R.id.btnPlay, (isRecording && isAudioRecorded) ? isRecording : isAudioRecorded);
    }

    private void setFormatButtonCaption() {
        ((Button) findViewById(R.id.btnFormat))
                .setText(getString(R.string.audio_format) + " ("
                        + file_exts[currentFormat] + ")");
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private void startRecording() {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        filePath = getFilename();
        ((TextView) findViewById(R.id.tv_fileName)).setText(new File(filePath).getName());
        recorder.setOutputFile(filePath);

        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    private void displayFormatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String formats[] = {"MPEG 4", "3GPP"};

        builder.setTitle(getString(R.string.choose_format_title))
                .setSingleChoiceItems(formats, currentFormat,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                currentFormat = which;
                                setFormatButtonCaption();

                                dialog.dismiss();
                            }
                        }).show();
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Toast.makeText(AudioRecordingActivity.this,
                    "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Toast.makeText(AudioRecordingActivity.this,
                    "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    Toast.makeText(AudioRecordingActivity.this, "Start Recording",
                            Toast.LENGTH_SHORT).show();
                    isAudioRecorded=false;
                    enableButtons(true);
                    startRecording();
                    findViewById(R.id.layout_audioPlayerProgressView).setVisibility(View.GONE);
                    break;
                }
                case R.id.btnStop: {
                    Toast.makeText(AudioRecordingActivity.this, "Stop Recording",
                            Toast.LENGTH_SHORT).show();
                    isAudioRecorded = true;
                    enableButtons(false);
                    if (mediaPlayer.isPlaying()) {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            // Changing button image to play button
                            ((Button) findViewById(R.id.btnPlay)).setText(R.string.play_recording);
                        }
                    } else {
                        stopRecording();
                    }

                    findViewById(R.id.layout_audioPlayerProgressView).setVisibility(View.GONE);
                    break;
                }
                case R.id.btnPlay: {
                    Toast.makeText(AudioRecordingActivity.this, "Playing Recording",
                            Toast.LENGTH_SHORT).show();
                    enableButtons(true);
                    if (mediaPlayer.isPlaying()) {
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();
                            // Changing button image to play button
                            ((Button) findViewById(R.id.btnPlay)).setText(R.string.play_recording);
                        }
                    } else {
                        // Resume song
                        if (mediaPlayer != null) {
                            playRecording();
                            // Changing button image to pause button
                            ((Button) findViewById(R.id.btnPlay)).setText(R.string.pause_recording);
                        }
                    }
                    findViewById(R.id.layout_audioPlayerProgressView).setVisibility(View.VISIBLE);

                    break;
                }
                case R.id.btnFormat: {
                    displayFormatDialog();

                    break;
                }
            }
        }
    };

    private void playRecording() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            mHandler.postDelayed(UpdateSongTime, 100);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            ((TextView) findViewById(R.id.tv_totalTime)).setText(""+milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            ((TextView) findViewById(R.id.tv_currentTime)).setText(""+milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        ((Button) findViewById(R.id.btnPlay)).setText(R.string.play_recording);
        enableButtons(false);
    }
}