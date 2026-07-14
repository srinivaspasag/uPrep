package com.vedantu.billing.payment.managers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.dao.TransactionDAO;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.Transaction;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.user.models.User;

public class EBSPaymentManager extends AbstractPaymentManager {

	private static final ALogger LOGGER = Logger.of(EBSPaymentManager.class);
	public static final String PAYMENT_CHANNEL = "EBS";
	private static String CHARGING_URL = null;
	private static String accountId = null;
	private static String secretKey = null;
	private static String redirect_url = null;
	private static String ebsMode = null; // LIVE or TEST
	private static final int EBS_CHANNEL_STANDARD = 0;
	private static String pageId = null;
	private static String secureHashAlgorithm = null;

	private static final String EBS_RESPONSE_CODE_SUCCESSFULL = "0";

	/*
	 * EBS:StandardRequest <form method="post"
	 * action=https://secure.ebs.in/pg/ma/payment/request name="frmTransaction"
	 * id="frmTransaction" > <input name=channel type=hidden value=0 /> <input
	 * name="account_id" type="hidden" value=" XXXX " /> <input
	 * name="reference_no" type="hidden" value=" XXXX "/> <input name="amount"
	 * type="hidden" value=" XXXX " /> <input name="mode" type="hidden"
	 * value=" XXXX " /> <input name="currency" type="hidden" value="INR" />
	 * <input name="description" type="hidden" value=" XXXX "/> <input
	 * name="return_url" type="hidden" value=" XXXX" /> <input name="name"
	 * type="hidden" value=" XXXX "/> <input name="address" type="hidden"
	 * value=" XXXX "/> <input name="city" type="hidden" value=" XXXX " />
	 * <input name="state" type="hidden" value=" XXXX " /> <input name="country"
	 * type="hidden" value=" XXXX " /> <input name="postal_code" type="hidden"
	 * value=" XXXX "/> <input name="phone" type="hidden" value=" XXXX " />
	 * <input name="email" type="hidden" value=" XXXX " /> <input
	 * name="ship_name" type="hidden" value=" XXXX " /> <input
	 * name="ship_address" type="hidden" value=" XXXX " /> <input
	 * name="ship_country" type="hidden" value=" XXXX " /> <input
	 * name="ship_state" type="hidden" value=" XXXX " /> <input name="ship_city"
	 * type="hidden" value=" XXXX " /> <input name="ship_postal_code"
	 * type="hidden" value=" XXXX "/> <input name="ship_phone" type="hidden"
	 * value=" XXXX " /> <input name="payment_option" type="hidden"
	 * value=" XXXX " /> <input name="bank_code" type="hidden" value=" XXXX " />
	 * <input name="emi type=hidden value= XXXX /> <input name=page_id
	 * type=hidden value= XXXX/> <input name=secure_hash type=hidden value=
	 * XXXX/> <input value=Submit type=submit /> </form>
	 */

	private static EBSPaymentManager INSTANCE = null;

	public static final EBSPaymentManager getInstance() {

		if (INSTANCE == null) {
			INSTANCE = new EBSPaymentManager();
		}
		return INSTANCE;
	}

	private EBSPaymentManager() {
		super();
		CHARGING_URL = Play.application().configuration()
				.getString("ebs.charging.url");
		accountId = Play.application().configuration()
				.getString("ebs.accountId");
		secretKey = Play.application().configuration()
				.getString("ebs.secretKey");
		redirect_url = Play.application().configuration()
				.getString("ebs.redirect_url");
		ebsMode = Play.application().configuration().getString("ebs.mode");
		pageId = Play.application().configuration().getString("ebs.pageId");
		secureHashAlgorithm = Play.application().configuration()
				.getString("ebs.securehash.algorithm");
	}

	@Override
	protected String getChargingRequestUrl(String transactionId, long orderId,
			User user, int amount, String currencyCode, DeviceType deviceType,
			String billingEmail,String billingPhone) throws VedantuException {

		Map<String, Object> httpParams = new HashMap<String, Object>();
		// required params
		httpParams.put("channel", String.valueOf(EBS_CHANNEL_STANDARD));
		httpParams.put("account_id", accountId);
		httpParams.put("reference_no", String.valueOf(orderId));
		// as the amount is in paisa
		float amountValue = amount / 100;
		httpParams.put("amount", amountValue);
		httpParams.put("mode", ebsMode);
		// httpParams.put("currency", currencyCode);
		httpParams.put("description", orderId + ","+transactionId+","
				+ deviceType.name());
		String name = user._getFullName();
		httpParams.put("name", name);
		//To avoid internal risk from EBS we are appending orderId to address,city and state(Unique entry)
		httpParams.put(
				"address",
				Play.application().configuration()
						.getString("ebs.billing.address.default")
						.replaceAll(" ", "")+" "+ orderId);
		httpParams.put(
				"city",
				Play.application().configuration()
						.getString("ebs.billing.city.default")+" "+ orderId);
		httpParams.put(
				"state",
				Play.application().configuration()
						.getString("ebs.billing.state.default")+" "+ orderId);
		httpParams.put(
				"country",
				Play.application().configuration()
						.getString("ebs.billing.country.default"));
		httpParams.put("postal_code", Play.application().configuration()
				.getString("ebs.billing.postal_code.default"));
		httpParams.put("phone",billingPhone);
		httpParams.put("email", billingEmail);
		httpParams.put("ship_name", name);
		if (!StringUtils.isEmpty(pageId))
			httpParams.put("page_id", pageId);
		httpParams.put("return_url", redirect_url);
		httpParams.put("algo", secureHashAlgorithm);
		// Securehash will be added in the following method.
		return getPaymentRedirectUrl(httpParams);
	}

	@Override
	public OnPaymentReceivedRes onPaymentReceived(Map<String, Object> resParams)
			throws VedantuException {

		StringBuffer encryptedDigitalReceipt = new StringBuffer()
				.append(resParams.get("DR"));
		String digitalReceipt = decryptResponse(encryptedDigitalReceipt);
		LOGGER.debug("Digital Receipt:"+digitalReceipt);
		Map<String, Object> httpResParams = toParamsMap(digitalReceipt, "&",
				"=");
//		String secureHash = (String) httpResParams.get("SecureHash");
//		if (StringUtils.isEmpty(secureHash)
//				|| StringUtils.equalsIgnoreCase(
//						computeSecureHash(httpResParams), secureHash)) {
//			// TODO: Same exception for both cases????
//			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
//					"invalid response");
//		}
//
		LOGGER.debug("response params : " + httpResParams);
		String error = (String) httpResParams.get("Error");
		String responseCode = (String) httpResParams.get("ResponseCode");
		if (StringUtils.isNotEmpty(error)) {
			Logger.debug("EBS Received Error Response:" + responseCode + ","
					+ error);
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
					"error response");
		}

		String orderId = (String) httpResParams.get("MerchantRefNo");

		String paymentChannelTransactionId = (String) httpResParams
				.get("TransactionID");

		String order_status = (String) httpResParams.get("ResponseCode");


		TransactionStatus transactionStatus = null;
		OrderState orderState = null;

		if (order_status.equalsIgnoreCase(EBS_RESPONSE_CODE_SUCCESSFULL)) {
			orderState = OrderState.CONFIRMED;
			transactionStatus = TransactionStatus.SUCCESS;
		} else if (order_status.equalsIgnoreCase("Aborted")) {
			orderState = OrderState.CANCELLED;
			transactionStatus = TransactionStatus.CANCELLED;
		} else {
			orderState = OrderState.CANCELLED;
			transactionStatus = TransactionStatus.FAILED;
		}

		String paymentMethod = (String) httpResParams.get("PaymentMethod");

		String paymentInstrument = (String) httpResParams.get("CardName"); //not present

		String amount = (String) httpResParams.get("Amount");

		int amountPaid = (int) (Float.parseFloat(amount) * 100);

		if (transactionStatus != TransactionStatus.SUCCESS) {
			amountPaid = 0;
		}

		Order order = OrderDAO.INSTANCE.getOrderById(Long.parseLong(orderId
				.trim()));
		order.orderState = orderState;
		String transactionId = null;


		Transaction transactionByOrder = TransactionDAO.INSTANCE.getTransactionByOrderId(order.orderId);
		transactionId = transactionByOrder.id.toString();

//		String description = (String)  httpResParams.get("Description");
//		if(!StringUtils.isEmpty(description)){
//			String[] tokens = description.split(",");
//			if(tokens.length>1){
//				transactionId=tokens[1];
//			}
//		}

		/*
		 * Check if there is anyway we pass txn id and they return on response.
		 * However the method updateTransaction handling the null by getting
		 * transaction by orderid.
		 */

		Transaction transaction = updateTransactionStatus(order.orderId,
				transactionId, paymentChannelTransactionId, paymentInstrument,
				paymentMethod, transactionStatus,
				String.valueOf(System.currentTimeMillis()), httpResParams,
				amountPaid);

		return updateOrderAndGetPaymentReceivedRes(order, transaction);
	}

	private String computeSecureHash(Map<String, Object> paramsMap) {
		StringBuilder secureHashBuilder = new StringBuilder();
		String[] keys = paramsMap.keySet().toArray(new String[] {});
		secureHashBuilder.append(secretKey);

		for (String key : keys) {
			Object value = paramsMap.get(key);
			if (value != null) {
				secureHashBuilder.append("|");
				secureHashBuilder.append(value);
			}
		}
		return secureHashBuilder.toString();
	}

	@Override
	public String getCallbackUrl() {
		return redirect_url;
	}

	private String getPaymentRedirectUrl(Map<String, Object> httpParams) {

		StringBuilder qs = null;
		StringBuilder secureHashBuilder = new StringBuilder();
		TreeMap<String, Object> sortedMap = new TreeMap<String, Object>(
				httpParams);
		// String[] keys = httpParams.keySet().toArray(new String[] {});
		secureHashBuilder.append(secretKey);

		for (String key : sortedMap.keySet()) {
			Object value = httpParams.get(key);
			if (value != null) {

				if (qs == null) {
					qs = new StringBuilder();
				} else {
					qs.append("&");
				}
				qs.append(key);
				qs.append("=");
				qs.append(value);
			}
		}


		//MD5 hash to be generated for the following parameters only and in the same order.
		// Country parameter value should not exceed 3 characters
		if (sortedMap.get("account_id") != null) {
			secureHashBuilder.append("|");
			secureHashBuilder.append(sortedMap.get("account_id"));
		}
		if (sortedMap.get("amount") != null) {
			secureHashBuilder.append("|");
			secureHashBuilder.append(sortedMap.get("amount"));
		}
		if (sortedMap.get("reference_no") != null) {
			secureHashBuilder.append("|");
			secureHashBuilder.append(sortedMap.get("reference_no"));
		}

		if (sortedMap.get("return_url") != null) {
			secureHashBuilder.append("|");
			secureHashBuilder.append(sortedMap.get("return_url"));
		}
		if (sortedMap.get("mode") != null) {
			secureHashBuilder.append("|");
			secureHashBuilder.append(sortedMap.get("mode"));
		}

		qs.insert(0, "?");
		qs.insert(0, CHARGING_URL);
		qs.append("&");
		qs.append("secure_hash");
		qs.append("=");
		qs.append(getMessageDigest(secureHashBuilder.toString(),
				secureHashAlgorithm));

		String url = qs.toString();
		LOGGER.debug("payment channel [" + PAYMENT_CHANNEL
				+ "] payment url :  " + url);
		return url;
	}

	private Map<String, Object> toParamsMap(String encResponse,
			String seprator1, String separator2) {

		Map<String, Object> responseParams = new HashMap<String, Object>();
		for (String keyValue : StringUtils.split(encResponse, seprator1)) {
			if (StringUtils.isEmpty(keyValue)) {
				continue;
			}
			String[] keyValueArray = keyValue.split(separator2);
			if (keyValueArray.length != 2) {
				continue;
			}
			responseParams.put(keyValueArray[0], keyValueArray[1]);
		}

		return responseParams;
	}

	private String decryptResponse(StringBuffer encryptedResponse) {
		for (int i = 0; i < encryptedResponse.length(); i++) {
			if (encryptedResponse.charAt(i) == ' ')
				encryptedResponse.setCharAt(i, '+');
		}
		Base64 base64 = new Base64();
		byte[] data = base64.decode(encryptedResponse.toString());
		RC4 rc4 = new RC4(secretKey);
		byte[] result = rc4.rc4(data);
		ByteArrayInputStream byteIn = new ByteArrayInputStream(result, 0,
				result.length);
		BufferedReader dataIn = new BufferedReader(new InputStreamReader(byteIn));
		String recvString1 = "";
		String recvString = "";
		try {
			recvString1 = dataIn.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return recvString;
		}
		int i = 0;
		while (recvString1 != null) {
			i++;
			if (i > 705)
				break;
			recvString += recvString1 + "\n";
			try {
				recvString1 = dataIn.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return recvString;
			}
		}
		recvString = recvString.replace("=&", "=--&");
		return recvString;
	}

//	public static void main(String[] args) throws VedantuException,
//			MalformedURLException {
//		EBSPaymentManager manager = new EBSPaymentManager();
//		String url = manager.getChargingRequestUrl("5301fd4744ae90d0284a9c33",
//				41, new User(), 100 / 100, "INR", DeviceType.WEB, null);
//		System.out.println("url : " + url);
//	}

	private String getMessageDigest(String msg, String algorithm) {
		Logger.debug("SecureHashingString:" + msg);
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] data = msg.getBytes();
			digest.update(data, 0, data.length);
			BigInteger i = new BigInteger(1, digest.digest());
			return String.format("%1$032X", i).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	class ResponseData {
		private HashMap info = new HashMap(); // Stores the data for

		// the phone directory.
		void addEntry(String name, String number) {
			// Record the phone number for a specified name.
			info.put(name, number);
		}

		String getData(String name) {
			// Retrieve the phone number for a specified name.
			// Returns null if there is no number for the name.
			return (String) info.get(name);
		}

	} // end class PhoneDirectory

	/**
	 * This class contains two static methods for Base64 encoding and decoding.
	 *
	 * @author <a href="http://izhuk.com">Igor Zhukovsky</a>
	 */
	class Base64 {

		/**
		 * Decodes BASE64 encoded string.
		 *
		 * @param encoded
		 *            string
		 * @return decoded data as byte array
		 */
		byte[] decode(String encoded) {
			int i;
			byte output[] = new byte[3];
			int state;

			ByteArrayOutputStream data = new ByteArrayOutputStream(
					encoded.length());

			state = 1;
			for (i = 0; i < encoded.length(); i++) {
				byte c;
				{
					char alpha = encoded.charAt(i);
					if (Character.isWhitespace(alpha))
						continue;

					if ((alpha >= 'A') && (alpha <= 'Z'))
						c = (byte) (alpha - 'A');
					else if ((alpha >= 'a') && (alpha <= 'z'))
						c = (byte) (26 + (alpha - 'a'));
					else if ((alpha >= '0') && (alpha <= '9'))
						c = (byte) (52 + (alpha - '0'));
					else if (alpha == '+')
						c = 62;
					else if (alpha == '/')
						c = 63;
					else if (alpha == '=')
						break; // end
					else
						return null; // error
				}

				switch (state) {
				case 1:
					output[0] = (byte) (c << 2);
					break;
				case 2:
					output[0] |= (byte) (c >>> 4);
					output[1] = (byte) ((c & 0x0F) << 4);
					break;
				case 3:
					output[1] |= (byte) (c >>> 2);
					output[2] = (byte) ((c & 0x03) << 6);
					break;
				case 4:
					output[2] |= c;
					data.write(output, 0, output.length);
					break;
				}
				state = (state < 4 ? state + 1 : 1);
			} // for

			if (i < encoded.length()) /* then '=' found, but the end of string */
				switch (state) {
				case 3:
					data.write(output, 0, 1);
					return (encoded.charAt(i) == '=')
							&& (encoded.charAt(i + 1) == '=') ? data
							.toByteArray() : null;
				case 4:
					data.write(output, 0, 2);
					return (encoded.charAt(i) == '=') ? data.toByteArray()
							: null;
				default:
					return null;
				}
			else
				return (state == 1 ? data.toByteArray() : null); /* end of string */

		} // decode

		final static String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

		/**
		 * Encodes binary data by BASE64 method.
		 *
		 * @param data
		 *            binary data as byte array
		 * @return encoded data as String
		 */
		String encode(byte[] data) {

			char output[] = new char[4];
			int state = 1;
			int restbits = 0;
			int chunks = 0;

			StringBuffer encoded = new StringBuffer();

			for (int i = 0; i < data.length; i++) {
				int ic = (data[i] >= 0 ? data[i] : (data[i] & 0x7F) + 128);
				switch (state) {
				case 1:
					output[0] = base64.charAt(ic >>> 2);
					restbits = ic & 0x03;
					break;
				case 2:
					output[1] = base64.charAt((restbits << 4) | (ic >>> 4));
					restbits = ic & 0x0F;
					break;
				case 3:
					output[2] = base64.charAt((restbits << 2) | (ic >>> 6));
					output[3] = base64.charAt(ic & 0x3F);
					encoded.append(output);

					// keep no more the 76 character per line
					chunks++;
					if ((chunks % 19) == 0)
						encoded.append("\r\n");
					break;
				}
				state = (state < 3 ? state + 1 : 1);
			} // for

			/* final */
			switch (state) {
			case 2:
				output[1] = base64.charAt((restbits << 4));
				output[2] = output[3] = '=';
				encoded.append(output);
				break;
			case 3:
				output[2] = base64.charAt((restbits << 2));
				output[3] = '=';
				encoded.append(output);
				break;
			}

			return encoded.toString();
		} // encode()

	} // Base64

	/**
	 * Author : R. Prince - Java coded based on the original VB source by Mike
	 * Shaffer Date : 01/2004 Name : RC4CipherEntity Stereotype : Entity
	 * Description : When called from a JSP/Servlet the RC4CipherEntity
	 * deciphers or enciphers a string of data.
	 **/

	class RC4 {

		private byte state[] = new byte[256];
		private int x;
		private int y;

		/**
		 * Initializes the class with a string key. The length of a normal key
		 * should be between 1 and 2048 bits. But this method doens't check the
		 * length at all.
		 *
		 * @param key
		 *            the encryption/decryption key String decryptedResponse =
		 */
		RC4(String key) throws NullPointerException {
			this(key.getBytes());
		}

		/**
		 * Initializes the class with a byte array key. The length of a normal
		 * key should be between 1 and 2048 bits. But this method doens't check
		 * the length at all.
		 *
		 * @param key
		 *            the encryption/decryption key
		 */
		RC4(byte[] key) throws NullPointerException {

			for (int i = 0; i < 256; i++) {
				state[i] = (byte) i;
			}

			x = 0;
			y = 0;

			int index1 = 0;
			int index2 = 0;

			byte tmp;

			if (key == null || key.length == 0) {
				throw new NullPointerException();
			}

			for (int i = 0; i < 256; i++) {

				index2 = ((key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;

				tmp = state[i];
				state[i] = state[index2];
				state[index2] = tmp;

				index1 = (index1 + 1) % key.length;
			}

		}

		/**
		 * RC4 encryption/decryption.
		 *
		 * @param data
		 *            the data to be encrypted/decrypted
		 * @return the result of the encryption/decryption
		 */
		byte[] rc4(String data) {

			if (data == null) {
				return null;
			}

			byte[] tmp = data.getBytes();

			this.rc4(tmp);

			return tmp;
		}

		/**
		 * RC4 encryption/decryption.
		 *
		 * @param buf
		 *            the data to be encrypted/decrypted
		 * @return the result of the encryption/decryption
		 */
		byte[] rc4(byte[] buf) {

			// int lx = this.x;
			// int ly = this.y;

			int xorIndex;
			byte tmp;

			if (buf == null) {
				return null;
			}

			byte[] result = new byte[buf.length];

			for (int i = 0; i < buf.length; i++) {

				x = (x + 1) & 0xff;
				y = ((state[x] & 0xff) + y) & 0xff;

				tmp = state[x];
				state[x] = state[y];
				state[y] = tmp;

				xorIndex = ((state[x] & 0xff) + (state[y] & 0xff)) & 0xff;
				result[i] = (byte) (buf[i] ^ state[xorIndex]);
			}

			// this.x = lx;
			// this.y = ly;

			return result;
		}

	}
}
