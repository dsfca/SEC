package crypto;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAProvider {

	private static final String ASSYMMETRIC_CYPHER_ALGO = "RSA";
	final static String DIGEST_ALGO = "SHA-256";
	
	/**************************************************************************************
	 * 											-RSAKeyGenerator()
	 * - generate rsa random pair(private and public) key and write them
	 *   in path passed as argument(rpivKeyPath and pubKeyPath) 
	 * 
	 * - input: 
	 * 		- privKeyPath: path to store generated private key.
	 * 		- pubKeyPath: path to story generayted public key.
	 * 
	 * - return:
	 * 
	 * ************************************************************************************/
	public static void RSAKeyGenerator(String privKeyPath, String pubKeyPath) throws Exception {
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASSYMMETRIC_CYPHER_ALGO);
        keyGen.initialize(1024);
        KeyPair keys = keyGen.generateKeyPair();
        
        PrivateKey privKey = keys.getPrivate();
        byte[] privKeyEncoded = privKey.getEncoded();
        
        PublicKey pubKey = keys.getPublic();
        byte[] pubKeyEncoded = pubKey.getEncoded();
        
        write(privKeyEncoded, privKeyPath);
        write(pubKeyEncoded, pubKeyPath);
        
	}
	
	/**write content of the array 
	 * content and save it on a given path
	 */
	public static void write(byte[] content, String path) throws Exception {
		FileOutputStream file  = new FileOutputStream(path);
        file.write(content);
        file.close();
	}
	

	/**************************************************************************************
	 * 											-readRSAKey()
	 * - return rsa key pair(private and public) readed from files with paths passed 
	 * 	 in argument. The keys must be previously generated and related 
	 * 
	 * - input: 
	 * 		- pubKeyPath: public key path 
	 * 		- privKeyPath: private key path
	 * 
	 * - return rsa key pair 
	 * 
	 * ************************************************************************************/
	public static KeyPair readRSAKey(String pubkeypath, String privKeyPath) throws Exception {
	    PublicKey pub = readPubKey(pubkeypath);	    
        PrivateKey priv = readPrivKey(privKeyPath);
        KeyPair keys = new KeyPair(pub, priv);
        return keys;
	}
	
	public static PublicKey readPubKey(String pubkeypath) throws Exception {
		byte[] pubEncoded = readfile(pubkeypath);
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
	    KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
	    PublicKey pub = keyFacPub.generatePublic(pubSpec);
	    return pub;
	}
	
	public static PrivateKey readPrivKey(String privKeyPath) throws Exception {
		byte[] privEncoded = readfile(privKeyPath);
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);
	    return priv;
	}
	
	public static byte[] readfile(String path) throws Exception {
		FileInputStream file = new FileInputStream(path);
        byte[] fileEncoded = new byte[file.available()];
        file.read(fileEncoded);
        file.close();
        return fileEncoded;
	}
	
	
	/**************************************************************************************
	 * 											-getTexthashEnWithPriKey()
	 * 
	 * - return plain text hash cyphered with private key, important when processing 
	 * 	 the digital signature of a text. the text returned is encoded on base 64.
	 * 
	 * - input: 
	 * 		- plaintext: plain text whose hash will be generated and encrypted with private key
	 * 		- key: private key to cypher hash of plain text.
	 * 
	 * - return: hash of plain text encrypted with private key(encoded on base 64)
	 * 
	 * ************************************************************************************/
	public static String getTexthashEnWithPriKey(String plaintext, PrivateKey key) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
        messageDigest.update(plaintext.getBytes());
        byte[] digestBytes = messageDigest.digest();
        String digestB64dString = Base64.getEncoder().encodeToString(digestBytes);
        String digestCyphered = priKeyCiphDeciph(digestB64dString, key, Cipher.ENCRYPT_MODE);
        return digestCyphered;
	}
	
	/**************************************************************************************
	 * 											-istextAuthentic()
	 * 
	 * - verify the digital signature of the plaintext and return true 
	 * 	 if signature is valid and false otherwise.
	 * 	 computes hash of the plaintext, decipher the plaintext hash ciphered with private key(digesCiphered)
	 * 	 and verify if both are equals.
	 *  
	 * 
	 * - input: 
	 * 		- plaintext: plain text to valid the digital signature
	 * 		- digesCiphered: supposed hash of plaintext ciphered with private key.
	 * 		- key: public key of private key used to cipher digesCiphered.
	 * 
	 * - return: boolean( true if signature is valid, false otherwise)
	 * ************************************************************************************/
	public static boolean istextAuthentic(String plaintext ,String digesCiphered, PublicKey key)throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
        messageDigest.update(plaintext.getBytes());
        byte[] digestBytes = messageDigest.digest();
        String digestB64dString = Base64.getEncoder().encodeToString(digestBytes);
		String digesCipheredtB64dString = pubKeyCiphDeciph(digesCiphered, key, Cipher.DECRYPT_MODE );
		return digesCipheredtB64dString.equals(digestB64dString);
	}
	
	
	/**************************************************************************************
	 * 											-priKeyCiphDeciph()
	 * 
	 * - cipher or decipher text (message_B64) depending on cipher mode passed in
	 * 	 argument(mode) with private key passed(key) and return the result encoded in base64.
	 * 
	 * - input: 
	 * 		- message_B64: text encoded on base64
	 * 		- key: private key to cipher or decipher message_B64.
	 * 		- mode: cipher mode(cipher or decipher)
	 * 
	 * - return: string encoded in base64
	 * 
	 * 
	 * ************************************************************************************/
	public static String priKeyCiphDeciph(String message_B64, PrivateKey key, int mode ) throws Exception {
		byte[] messageBytes = Base64.getDecoder().decode(message_B64);
    	Cipher cipher = Cipher.getInstance(ASSYMMETRIC_CYPHER_ALGO);
    	cipher.init(mode, key);
    	byte[] messageCyphered = cipher.doFinal(messageBytes);
    	String cipherB64String = Base64.getEncoder().encodeToString(messageCyphered);
    	return cipherB64String;
	}
	
	/**************************************************************************************
	 * 											-pubKeyCiphDeciph()
	 * 
	 * - cipher or decipher text (message_B64) depending on cipher mode passed in
	 * 	 argument(mode) with public key passed(key) and return the result encoded in base64.
	 * 
	 * - input: 
	 * 		- message_B64: text encoded on base64
	 * 		- key: public key to cipher or decipher message_B64.
	 * 		- mode: cipher mode(cipher or decipher)
	 * 
	 * - return: string encoded in base64
	 * 
	 * 
	 * ************************************************************************************/
	public static String pubKeyCiphDeciph(String message_B64, PublicKey key, int mode ) throws Exception {
		byte[] messageBytes = Base64.getDecoder().decode(message_B64);
    	Cipher cipher = Cipher.getInstance(ASSYMMETRIC_CYPHER_ALGO);
    	cipher.init(mode, key);
    	byte[] messageCyphered = cipher.doFinal(messageBytes);
    	String cipherB64String = Base64.getEncoder().encodeToString(messageCyphered);
    	return cipherB64String;
	}
	
	/*public static void main(String[] args) {
		
		try {
		//	for(int i = 0; i < 7; i++) {
				String privKeyPath = "resources/public_keys/user" + 1 +"_public.key";
				String pubKeyPath = "resources/private_keys/user" + 1 + "_private.key";
				RSAKeyGenerator(privKeyPath, pubKeyPath);
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
}
