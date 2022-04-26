import org.slf4j.LoggerFactory
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

// add log msgs to Jenkins console
manager.listener.logger.println "Post Build pipeline started\n"
new CodeCleanUp(manager).runCleanUp()

class CodeCleanUp {
    static def manager

    CodeCleanUp(def manager) {
    this.manager=manager
    }

    def runCleanUp() {
       println "Inside runCleanUp() method..."
       
       // add log msgs to Jenkins console
       manager.listener.logger.println "Manager inside runCleanUp() method"
       manager.listener.logger.println CodeCleanUp.httpGetMethod("http://example.com:81")  //https://reqres.in/api/products/3
       
    }

    private static String httpGetMethod(String url) {
        CloseableHttpClient httpclient = HttpClients.custom()
              .setRetryHandler(retryHandler())
              .setDefaultRequestConfig(timeoutRequestConfig())
              .build();

        HttpGet httpget = new HttpGet(url)
        CloseableHttpResponse response = null
        StringBuilder result = null
        try {
            response = httpclient.execute(httpget)
            InputStream ins = response.getEntity().getContent()
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins))
            result = new StringBuilder()
            String line
            while ((line = reader.readLine()) != null) {
                result.append(line)
            }
        } catch (ClientProtocolException e) {
            manager.listener.logger.println e
        } catch (IOException e) {
            manager.listener.logger.println e
        } finally {
            try {
                if (response != null) {
                    response.close()
                }
            } catch (IOException e) {
                e.printStackTrace()
            }
        }
        return result.toString()
    }

    // retry handler
    public static HttpRequestRetryHandler retryHandler() {
        HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
             @Override
             public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                manager.listener.logger.println "Retrying ${executionCount} 'th time"
                return executionCount < 2;
             }
        }; 
       return requestRetryHandler;
    }

    // timeout config
    public static RequestConfig timeoutRequestConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(5000).build(); 
       return requestConfig;
    }
    
}
