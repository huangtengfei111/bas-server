package app.util;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class ZipMaker {
  private static final Logger log = LoggerFactory.getLogger(ZipMaker.class);

  /**
   * 
   * 
   * @param filesToAdd Add files to be archived into zip file
   * @param dest
   */
  public static void zipIt(ArrayList<File> filesToAdd, File dest) {
    try {
      // This is name and path of zip file to be created
      ZipFile zipFile = new ZipFile(dest.getAbsolutePath());

      // Initiate Zip Parameters which define various properties
      ZipParameters parameters = new ZipParameters();
      parameters.setIncludeRootFolder(false);
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

      // DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of
      // compression
      // DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
      // DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
      // DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
      // DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

      // Now add files to the zip file
      zipFile.addFiles(filesToAdd, parameters);
    } catch (ZipException e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * 
   * @param filesToAdd
   * @param dest
   */
  public static void zipItWithPassword(ArrayList<File> filesToAdd, File dest) {
    try {
      // This is name and path of zip file to be created
      ZipFile zipFile = new ZipFile(dest.getAbsolutePath());

      // Initiate Zip Parameters which define various properties
      ZipParameters parameters = new ZipParameters();
      parameters.setIncludeRootFolder(false);
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

      // DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of
      // compression
      // DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
      // DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
      // DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
      // DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

      // Set the encryption flag to true
      parameters.setEncryptFiles(true);

      // Set the encryption method to AES Zip Encryption
      parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

      // AES_STRENGTH_128 - For both encryption and decryption
      // AES_STRENGTH_192 - For decryption only
      // AES_STRENGTH_256 - For both encryption and decryption
      // Key strength 192 cannot be used for encryption. But if a zip file already has
      // a
      // file encrypted with key strength of 192, then Zip4j can decrypt this file
      parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

      // Set password
      parameters.setPassword("ohlongandlong");

      // Now add files to the zip file
      zipFile.addFiles(filesToAdd, parameters);
    } catch (ZipException e) {
      log.error(e.getMessage(), e);
    }
  }

  public static void unzip() {
    String source = "folder/source.zip";
    String destination = "folder/source/";
    String password = "password";

    try {
      ZipFile zipFile = new ZipFile(source);
      if (zipFile.isEncrypted()) {
        zipFile.setPassword(password);
      }
      zipFile.extractAll(destination);
    } catch (ZipException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {

  }
}
