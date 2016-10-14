import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * A minimal servlet that returns the license key for
 * the given PIN.
 *
 * @author Carol Hamer
 */
public class GetLicense extends HttpServlet {

  /**
   * Get the license corresponding to the PIN.
   */
  String getLicense(int pin) {
    String retString = "";
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt(pin);
      baos.close();
      dos.close();
      baos.close();
      byte[] pinData = baos.toByteArray();
      
      SecretKeyFactory scf = SecretKeyFactory.getInstance("DES");
      // The DES key used to generate the license is hard-coded:
      byte[] keyData = {
          (byte)-29, (byte)14, (byte)-22, (byte)35, 
          (byte)-53, (byte)4, (byte)-3, (byte)-48
      };
      DESKeySpec keySpec = new DESKeySpec(keyData);
      SecretKey desKey = scf.generateSecret(keySpec);
      Cipher desCipher;

      // Create the cipher 
      desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      // Initialize the cipher for encryption
      desCipher.init(Cipher.ENCRYPT_MODE, desKey);

      // Encrypt the pin
      byte[] ciphertext = desCipher.doFinal(pinData);

      // Turn the encrypted byte array into a human-readable string
      DataInputStream stream 
          = new DataInputStream(new ByteArrayInputStream(ciphertext));
      int val1 = stream.readInt();
      int val2 = stream.readInt();
      stream.close();
      retString = val1 + "z" + val2;
     
    } catch (Exception e) {
      e.printStackTrace();
    }
    return retString;
  }

  /**
   * Use an input stream to convert an array of bytes to an int.
   */
  public static int parseInt(byte[] data) throws IOException {
    DataInputStream stream 
      = new DataInputStream(new ByteArrayInputStream(data));
    int retVal = stream.readInt();
    stream.close();
    return(retVal);
  }

  /**
   * Handle the POST request (others are ignored).
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    int length = request.getContentLength();
    byte[] content = new byte[length];
    // get the input stream to read the user data from BlackBerry App World:
    InputStream is = request.getInputStream();
    int read = is.read(content);
    // Many errors are ignored in this minimal example.
    if(read != length) {
      response.sendError(400);
    } else {
      // get the PIN from the data:
      String contentString = new String(content);
      int startIndex = contentString.indexOf("PIN=");
      int endIndex = contentString.indexOf("&", startIndex);
      String pinString = contentString.substring(startIndex+4, endIndex);
      // it is sent as a hexidecimal string, hence the "16"
      // argument when parsing it:
      int pin = Integer.parseInt(pinString, 16);

      // Prepare the response data:
      String responseString = "key=" + getLicense(pin);
      byte[] responseBytes = responseString.getBytes();
      response.setContentType("application/www-url-encoded");
      response.setContentLength(responseBytes.length);
      OutputStream os = response.getOutputStream();
      // send the message
      os.write(responseBytes);
      os.close();
      response.flushBuffer();
    }
  }
}
