package uniresolver.driver.did.vaa;
import com.google.gson.*;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DidVaaDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidVaaDriver.class);

	public static final Pattern DID_VAA_PATTERN = Pattern.compile("^did:vaa:([123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{27,31})$");

	public static final String DEFAULT_VAA_URL = "http://45.120.243.40:8768/";

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

	private String vaaUrl = DEFAULT_VAA_URL;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

	public DidVaaDriver() {
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
		Gson gson=new Gson();
		// match
		Matcher matcher = DID_VAA_PATTERN.matcher(identifier);
		if(!matcher.matches()) {
			return null;
		}

		// fetch data from VAA
		String resolveUrl = this.getVaaUrl() + "/v1/did/resolve?didname=" + identifier;
		HttpGet httpGet = new HttpGet(resolveUrl);

		// find the DDO
		JsonElement jsonDataElement;
		JsonObject jsonResponse;
		try {
			CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet);
			if(httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new ResolutionException("Cannot retrieve DDO for `" + identifier + "` from `" + this.getVaaUrl() + ": " + httpResponse.getStatusLine());
			}

			// extract payload
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);

			EntityUtils.consume(httpEntity);
            // check if exist identifier
            if (entityString == null) {
                throw new ResolutionException("docuement not exist");
            }
            jsonResponse = gson.fromJson(entityString, JsonObject.class);
			log.info("jsonDataElement："+jsonResponse.toString());

		} catch (IOException ex) {
			throw new ResolutionException("Cannot retrieve DDO info for `" + identifier + "` from `" + this.getVaaUrl() + "`: " + ex.getMessage(), ex);
		} catch (JSONException jex) {
			throw new ResolutionException("Cannot parse JSON response from `" + this.getVaaUrl() + "`: " + jex.getMessage(), jex);
		}
		JsonObject jsonDataObject=jsonResponse.getAsJsonObject("didDocument");
        String context = "https://w3id.org/did/v1";
         List<PublicKey> publicKeys = new ArrayList<PublicKey>();
                    JsonArray publicKey = jsonDataObject == null ? null : jsonDataObject.getAsJsonArray("publicKey");
                    if (publicKey != null) {
                        for (JsonElement publicKeyElement : publicKey) {
                            JsonObject jsonObject = publicKeyElement == null ? null : publicKeyElement.getAsJsonObject();
                            String id = jsonObject == null ? null : jsonObject.get("id").getAsString();
                            if (id != null) {
                                String publicKeyType = jsonObject.get("type").getAsString();
                                String publicKeyString = jsonObject.get("publicKeyPem").getAsString();
                                publicKeys.add(PublicKey.build(id, new String[]{publicKeyType}, null, null, null, publicKeyString));
                            }
                        }
                    }

                    List<Service> services = new ArrayList<>();
                     JsonArray service = jsonDataObject.getAsJsonArray("service");
                     if (service != null) {
                                    for (JsonElement serviceElement : service) {
                                        JsonObject jsonObject = serviceElement == null ? null : serviceElement.getAsJsonObject();
                                        services.add(Service.build(gson.fromJson(jsonObject, Map.class)));
                                    }
                         }
		// DDO id
		String id = identifier;

	//	log.info("请求获取到id："+id);
		//log.info("请求获取到publicKeys："+publicKeys.toString());
		//log.info("请求获取到authentications：null");
		//log.info("请求获取到services："+services.toString());
		// create DDO
		DIDDocument didDocument = DIDDocument.build(context, identifier, publicKeys, null, services);
		//log.info("封装得到DIDDocument!!!");
		// create Method METADATA
		Map<String, Object> methodMetadata = new LinkedHashMap<>();
		methodMetadata.put("proof", gson.fromJson(jsonDataObject.getAsJsonObject("proof"), Map.class));
		methodMetadata.put("created", jsonDataObject.get("created").toString());
		methodMetadata.put("updated", jsonDataObject.get("updated").toString());

		// create RESOLVE RESULT
		ResolveResult resolveResult = ResolveResult.build(didDocument, null, methodMetadata);
	//	log.info("解析得到ResolveResult："+resolveResult.toString());
		// done
		return resolveResult;
	}

	@Override
	public Map<String, Object> properties() {

		Map<String, Object> properties = new HashMap<>();
		properties.put("vaaUrl", this.getVaaUrl());

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public String getVaaUrl() {

		return this.vaaUrl;
	}

	public void setVaaUrl(String vaaUrl) {
		this.vaaUrl = vaaUrl;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
