package com.android.gallery3d.voice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import android.util.Log;

public class VoiceParser {
	private static boolean mIsAudioPhoto;
	private static byte[] mAudioBytes;

	private static String mFilePath;

	public static boolean isAudioPhoto() {
		return mIsAudioPhoto;
	}

	/**
	 * Please call this method after call {@link #parse}, otherwise it will
	 * return null.
	 * 
	 * @return audio bytes
	 */
	public static byte[] getAudioBytes() {
		return mAudioBytes;
	}

	public static void parse(byte[] data) throws UnsupportedEncodingException {
		Log.d("sqm", "VoiceParser[parse] data=" + data);

		if (data == null) {
			return;
		}

		int length = data.length;
		int tagLength = data[length - 1];
		String tag = new String(data, length - tagLength - 1, tagLength,
				"ISO-8859-1");
		if (tag.startsWith("rgkvoice_")) {
			mIsAudioPhoto = true;
			int index = tag.indexOf("_");
			String voiceLengthStr = tag.substring(index + 1);
			// Log.d("sqm", "voiceLengthStr==" + voiceLengthStr);
			int voiceLength = Integer.parseInt(voiceLengthStr);
			Log.d("sqm", "VoiceParser[parse] voiceLength==" + voiceLength);
			byte[] voiceBytes = new byte[voiceLength];
			System.arraycopy(data, length - 1 - tagLength - voiceLength,
					voiceBytes, 0, voiceLength);
			mAudioBytes = voiceBytes;
		} else {
			mIsAudioPhoto = false;
			mAudioBytes = null;
		}

		Log.d("sqm", "VoiceParser[parse] ...end");
	}

	public static void parse(String filePath) throws IOException {
		Log.d("sqm", "VoiceParser[parse] filePath=" + filePath);

		if (filePath == null) {
			return;
		}

		mFilePath = filePath;

		File f = new File(filePath);
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		long length = raf.length();
		Log.d("sqm", "VoiceParser[parse] length==" + length);
		long seek = length - 1;
		raf.seek(seek);
		byte tagLength = raf.readByte();
		Log.d("sqm", "VoiceParser[parse] tagLength==" + tagLength);
		if (tagLength <= 0 || tagLength > 16) {
			return;
		}
		seek -= tagLength;
		raf.seek(seek);
		byte[] buffer = new byte[tagLength];
		raf.read(buffer, 0, tagLength);
		String tag = new String(buffer, 0, tagLength, "ISO-8859-1");
		if (tag.startsWith("rgkvoice_")) {
			mIsAudioPhoto = true;
			int index = tag.indexOf("_");
			String voiceLengthStr = tag.substring(index + 1);
			// Log.d("sqm", "voiceLengthStr==" + voiceLengthStr);
			int voiceLength = Integer.parseInt(voiceLengthStr);
			Log.d("sqm", "VoiceParser[parse] voiceLength==" + voiceLength);
			byte[] voiceBytes = new byte[voiceLength];
			seek -= voiceLength;
			raf.seek(seek);
			raf.read(voiceBytes, 0, voiceLength);
			mAudioBytes = voiceBytes;
		} else {
			mIsAudioPhoto = false;
			mAudioBytes = null;
		}

		raf.close();

		Log.d("sqm", "VoiceParser[parse] ...end");
	}

	public static boolean hasVoice(String filePath) throws IOException {
		Log.d("sqm", "VoiceParser[hasVoice] filePath=" + filePath);

		if (filePath == null) {
			mIsAudioPhoto = false;
			return false;
		}

		File f = new File(filePath);
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		long length = raf.length();
		Log.d("sqm", "VoiceParser[parse] length==" + length);
		long seek = length - 1;
		raf.seek(seek);
		byte tagLength = raf.readByte();
		Log.d("sqm", "VoiceParser[parse] tagLength==" + tagLength);
		if (tagLength <= 0 || tagLength > 16) {
			mIsAudioPhoto = false;
			return false;
		}

		seek -= tagLength;
		raf.seek(seek);
		byte[] buffer = new byte[tagLength];
		raf.read(buffer, 0, tagLength);
		String tag = new String(buffer, 0, tagLength, "ISO-8859-1");
		if (tag.startsWith("rgkvoice_")) {
			mIsAudioPhoto = true;
		} else {
			mIsAudioPhoto = false;
		}

		raf.close();

		Log.d("sqm", "VoiceParser[hasVoice] ...end");
		
		return mIsAudioPhoto;
	}

	/**
	 * Maybe write voice bytes to out if voice exist
	 * 
	 * @param out
	 * @throws IOException
	 */
	public static void maybeWriteAudioToPhoto(java.io.OutputStream out)
			throws IOException {
		Log.d("sqm", "maybeWriteAudioToPhoto{...}");
		if (mIsAudioPhoto) {
			byte[] voiceBytes = mAudioBytes;
			int voiceSize = voiceBytes.length;
			String tagStr = "rgkvoice_" + voiceSize;
			byte[] tagBytes = tagStr.getBytes("ISO-8859-1");
			int tagSize = tagBytes.length;

			Log.d("sqm", "VoiceUtils[getVoicePhotoBytes] voiceSize="
					+ voiceSize + ",tagSize=" + tagSize);

			out.write(voiceBytes);
			out.write(tagBytes);
			out.write((tagSize & 0xFF));
		}

	}

	public static void maybeWriteAudioToPhoto(File toFile, byte[] voiceBytes) {
		Log.d("sqm", "VoiceParser: maybeWriteAudioToPhoto{...}");
		if (voiceBytes != null) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(toFile, true);
				int voiceSize = voiceBytes.length;
				String tagStr = "rgkvoice_" + voiceSize;
				byte[] tagBytes = tagStr.getBytes("ISO-8859-1");
				int tagSize = tagBytes.length;

				Log.d("sqm", "VoiceParser[maybeWriteAudioToPhoto] voiceSize="
					+ voiceSize + ",tagSize=" + tagSize);

				out.write(voiceBytes);
				out.write(tagBytes);
				out.write((tagSize & 0xFF));
				out.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d("sqm", "VoiceParser: maybeWriteAudioToPhoto...file not found.");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("sqm", "VoiceParser: maybeWriteAudioToPhoto...IOException: " + e.getMessage());
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.d("sqm", "VoiceParser: maybeWriteAudioToPhoto..when close stream..IOException: "
							+ e.getMessage());
					}
				}
			}
		}

	}

	/**
	 * Clear the voice bytes
	 */
	public static void reset() {
		mAudioBytes = null;
		mIsAudioPhoto = false;
		mFilePath = null;
	}

	public static String getFilePath() {
		return mFilePath;
	}

}
