package cp.kt.selfcontrol.util

import com.jeremyliao.liveeventbus.LiveEventBus
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object BaiduHelper {
    private val auth: String?
        get() {
            // 官网获取的 API Key 更新为你注册的
            val clientId = "igzkzgZo16K2LmkVgnAzsUIv"
            // 官网获取的 Secret Key 更新为你注册的
            val clientSecret = "LnrAcjDeDoauwbbVG5p4NcR832upS9MP"
            return getAuth(clientId, clientSecret)
        }

    fun getAuth(ak: String, sk: String): String? {
        // 获取token地址
        val authHost = "https://aip.baidubce.com/oauth/2.0/token?"
        val getAccessTokenUrl = (authHost // 1. grant_type为固定参数
                + "grant_type=client_credentials" // 2. 官网获取的 API Key
                + "&client_id=" + ak // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk)
        try {
            val realUrl = URL(getAccessTokenUrl)
            // 打开和URL之间的连接
            val connection: HttpURLConnection = realUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // 定义 BufferedReader输入流来读取URL的响应
            val input = BufferedReader(InputStreamReader(connection.inputStream))
            val result = StringBuffer()

            input.use { r ->
                val temp = r.readLine()
                if (temp != null) result.append(temp)
            }

            input.close()

            val jsonObject = JSONObject(result.toString())
            return jsonObject.getString("access_token")
        } catch (e: Exception) {
            LiveEventBus.get(Constant.EventBus.POEM).post("")
        }
        return null
    }

    fun getPoem(text: String) {
        val authHost = "https://aip.baidubce.com/rpc/2.0/creation/v1/poem?"
        val getAccessTokenUrl = (authHost // 1. grant_type为固定参数
                + "access_token=" + auth) // 3. 官网获取的 Secret Key
        try {
            val realUrl = URL(getAccessTokenUrl)
            // 打开和URL之间的连接
            val connection: HttpURLConnection = realUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            //设置header内的参数 connection.setRequestProperty("健, "值");
            connection.setRequestProperty("Content-Type", "application/json")

            //设置body内的参数，put到JSONObject中
            val param = JSONObject()
            param.put("text", text)
//            param.put("index", 0)

            connection.connect()

            // 得到请求的输出流对象
            val output = OutputStreamWriter(connection.outputStream, "UTF-8")
            output.write(param.toString())
            output.flush()

            // 定义 BufferedReader输入流来读取URL的响应
            val input = BufferedReader(InputStreamReader(connection.inputStream))
            val result = StringBuffer()

            input.use { r ->
                val temp = r.readLine()
                if (temp != null) result.append(temp)
            }

            output.close()
            input.close()

            val jsonObject = JSONObject(result.toString())
            val poems = jsonObject.getJSONArray("poem")
            if (poems.length() > 0) {
                val obj = poems.getJSONObject(0)
                val title = obj.getString("title")
                val content = obj.getString("content").replace("\t", "\n")

                LiveEventBus.get(Constant.EventBus.POEM).post("$title#$content")
            }
        } catch (e: Exception) {
            LiveEventBus.get(Constant.EventBus.POEM).post("")
        }
    }
}