# 使用EnOS SDK通过HTTP接入设备

## Maven依赖方式

Maven依赖，在工程`pom.xml``dependencies`下添加如下依赖。

```xml
<dependency>
  <groupId>com.envisioniot</groupId>
  <artifactId>enos-http</artifactId>
  <version>0.1.1-SNAPSHOT</version>
</dependency>
```

## 认证与连接

本章介绍如何进行SDK初始化，建立设备与云端的连接。

### 设备认证

设备的身份认证支持“静态激活”认证方式，需要有ProductKey，DeviceKey和DeviceSecret。

“动态激活”认证方式，目前在HTTP接入上不支。

### SDK初始化

SDK初始化，即设备通过“静态激活”方式鉴权，获取Session ID。如果设备鉴权失败，在执行HTTP请求时，会得到`unable to get authenticated`的错误返回。鉴权成功之后，设备和云端的Session保活更新由SDK负责。

在SDK初始化时，需要提供EnOS HTTP设备接入网关的URL，设备三元组和设备预期的连接存续时长。如下为SDK初始化示例代码。

```java
// construct a static device credential via ProductKey, DeviceKey and DeviceSecret
StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        
// construct a http connection
SessionConfiguration configuration = SessionConfiguration.builder().lifetime(30_000).build();

HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential)
                .sessionConfiguration(configuration)
                .build();
```

### 设备预期的连接存续时长

SDK通过HTTP接入EnOS时，会首先通过三元组鉴权，获取一个Session ID。这个Session ID的有效期为设备指定的**连接存续时长**。在有效期内，SDK会使用这个Session ID作为与EnOS云端交互的身份认证。超过有效期后，SDK会向云端请求更新Session ID。为了避免频繁地更新Session ID，造成过多的网络开销与发送延迟，设备应当选择一个合适的连接存续时长。

连接存续时长一般依照设备向云端上报数据的频次来设计，可以选择为上报时间间隔的2-5倍。例如一个设备一般每分钟上报一次数据，可以选择连接存续时长为*5分钟*。连接存续时长的单位为毫秒，最小有效值为30秒，最大有效值为24小时。

在下列示例代码中，连接存续时长被设置为*5分钟*。

```java
// construct a http connection
SessionConfiguration configuration = SessionConfiguration.builder().lifetime(30_000).build();
```

### EnOS HTTP设备接入网关的URL

设备接入网关的URL，请参考开发者控制台 > 帮助 > 环境信息。

## 设备数据上报

设备可以通过HTTP协议，上报测点数据。

目前在HTTP协议上，还不支持其他数据（如属性、事件等）上报功能。

```java
// build a measurepoint post request
MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("Int_value", 100)
                .build();

// publish the request synchronously
try
{
    MeasurepointPostResponse response = connection.publish(request, null);
} catch (EnvisionException | IOException e)
{
    e.printStackTrace();
}
```

### 异步方式上报数据

设备如果希望采用异步方式上报数据，可以在`publish`请求时，指定一个`IResponseCallback`实现。

```java
// publish the request and handle the response asynchronously
try
{
    connection.publish(request, new IResponseCallback<MeasurepointPostResponse>()
            {
                @Override
                public void onResponse(MeasurepointPostResponse response)
                {
                    // this method will be executed when a response is received
                }

                @Override
                public void onFailure(Exception failure)
                {
                    // this method will be executed if any exception
                }
            }, null);
} catch (EnvisionException | IOException e)
{
    e.printStackTrace();
}
```

## 设备文件类型数据上报

设备可以通过HTTP协议，上报文件类型测点数据。SDK支持在一个HTTP请求中上报多个文件类型的测点。单个HTTP请求中携带的文件总字节数不能超过*200MB*。单个HTTP请求上报文件的时间限制为*60秒*。

目前在HTTP协议上，还不支持其他数据（如属性、事件等）上报功能。

上报文件类型的测点，与上报其他类型的测点一样，支持同步方式与异步方式获取响应。

```java
// build a measurepoint post request with a file measurepoint and a double-type measurepoint
MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("file1x", new File("example.mp4"))
                .addMeasurePoint("voltage", 5.0)
                .build();

// post the request synchronously
try
{
     MeasurepointPostResponse response = connection.publish(request,
            (bytes, length) ->
            {
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length * 100.0));
            });
} catch (IOException | EnvisionException e)
{
    e.printStackTrace();
}

// alternatively, post the request asynchronously
try
{
    connection.publish(request, new IResponseCallback<MeasurepointPostResponse>()
           {
                @Override
                public void onResponse(MeasurepointPostResponse response)
                {
                    // this method will be executed when a response is received
                }

                @Override
                public void onFailure(Exception failure)
                {
                    // this method will be executed if any exception
                }
            }, (bytes, length) ->
            {
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length * 100.0));
            });
} catch (IOException | EnvisionException e)
{
    e.printStackTrace();
}
```

### 监听文件数据上报的进度

设备如果希望监听文件上报的进度，可以在`publish`请求时，指定一个`IProgressListener`实现。

在文件上报的过程中，SDK会调用这个监听器中定义的方法，提供已经发送的文件字节数和文件总字节数信息。