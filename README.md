## SpringCloudStream基础概念



下图是官方文档中对于Spring Cloud Stream应用模型的结构图。从中我们可以看到，Spring Cloud Stream构建的应用程序与消息中间件之间是通过绑定器`Binder`相关联的，绑定器对于应用程序而言起到了隔离作用，它使得不同消息中间件的实现细节对应用程序来说是透明的。所以对于每一个Spring Cloud Stream的应用程序来说，它不需要知晓消息中间件的通信细节，它只需要知道`Binder`对应用程序提供的概念去实现即可，而这个概念就是在快速入门中我们提到的消息通道：`Channel`。如下图案例，在应用程序和Binder之间定义了两条输入通道和三条输出通道来传递消息，而绑定器则是作为这些通道和消息中间件之间的桥梁进行通信。

![img](/doc/SCSt-with-binder.png)

### 绑定器

`Binder`绑定器是Spring Cloud Stream中一个非常重要的概念。在没有绑定器这个概念的情况下，我们的Spring Boot应用要直接与消息中间件进行信息交互的时候，由于各消息中间件构建的初衷不同，它们的实现细节上会有较大的差异性，这使得我们实现的消息交互逻辑就会非常笨重，因为对具体的中间件实现细节有太重的依赖，当中间件有较大的变动升级、或是更换中间件的时候，我们就需要付出非常大的代价来实施。

通过定义绑定器作为中间层，完美地实现了应用程序与消息中间件细节之间的隔离。通过向应用程序暴露统一的`Channel`通道，使得应用程序不需要再考虑各种不同的消息中间件实现。当我们需要升级消息中间件，或是更换其他消息中间件产品时，我们要做的就是更换它们对应的`Binder`绑定器而不需要修改任何Spring Boot的应用逻辑。

### 发布-订阅模式

在Spring Cloud Stream中的消息通信方式遵循了发布-订阅模式，当一条消息被投递到消息中间件之后，它会通过共享的`Topic`主题进行广播，消息消费者在订阅的主题中收到它并触发自身的业务逻辑处理。这里所提到的`Topic`主题是Spring Cloud Stream中的一个抽象概念，用来代表发布共享消息给消费者的地方。在不同的消息中间件中，`Topic`可能对应着不同的概念，比如：在RabbitMQ中的它对应了Exchange、而在Kakfa中则对应了Kafka中的Topic。

### 消费组

虽然Spring Cloud Stream通过发布-订阅模式将消息生产者与消费者做了很好的解耦，基于相同主题的消费者可以轻松的进行扩展，但是这些扩展都是针对不同的应用实例而言的，在现实的微服务架构中，我们每一个微服务应用为了实现高可用和负载均衡，实际上都会部署多个实例。很多情况下，消息生产者发送消息给某个具体微服务时，只希望被消费一次，按照上面我们启动两个应用的例子，虽然它们同属一个应用，但是这个消息出现了被重复消费两次的情况。为了解决这个问题，在Spring Cloud Stream中提供了消费组的概念。

如果在同一个主题上的应用需要启动多个实例的时候，我们可以通过`spring.cloud.stream.bindings.input.group`属性为应用指定一个组名，这样这个应用的多个实例在接收到消息的时候，只会有一个成员真正的收到消息并进行处理

### 消息分区

通过引入消费组的概念，我们已经能够在多实例的情况下，保障每个消息只被组内一个实例进行消费。对于一些业务场景，就需要对于一些具有相同特征的消息每次都可以被同一个消费实例处理，比如：一些用于监控服务，为了统计某段时间内消息生产者发送的报告内容，监控服务需要在自身内容聚合这些数据，那么消息生产者可以为消息增加一个固有的特征ID来进行分区，使得拥有这些ID的消息每次都能被发送到一个特定的实例上实现累计统计的效果，否则这些数据就会分散到各个不同的节点导致监控结果不一致的情况。而分区概念的引入就是为了解决这样的问题：当生产者将消息数据发送给多个消费者实例时，保证拥有共同特征的消息数据始终是由同一个消费者实例接收和处理。

## RabbitMQ 基本概念

 RabbitMQ 则有更详细的概念需要解释。上面介绍过 RabbitMQ 是 AMQP 协议的一个开源实现，所以其内部实际上也是 AMQP 中的基本概念：

![img](/doc/rabbitmq.png)

### 消息(Message)

 消息，消息是不具名的，它由消息头和消息体组成。消息体是不透明的，而消息头则由一系列的可选属性组成，这些属性包括routing-key（路由键）、priority（相对于其他消息的优先权）、delivery-mode（指出该消息可能需要持久性存储）等。

### 消息的生产者(Publisher)

 消息的生产者，也是一个向交换器发布消息的客户端应用程序。

### 交换器(Exchange)

 交换器，用来接收生产者发送的消息并将这些消息路由给服务器中的队列。

### 绑定(Binding)

 绑定，用于消息队列和交换器之间的关联。一个绑定就是基于路由键将交换器和消息队列连接起来的路由规则，所以可以将交换器理解成一个由绑定构成的路由表。

### 消息队列(Queue)

 消息队列，用来保存消息直到发送给消费者。它是消息的容器，也是消息的终点。一个消息可投入一个或多个队列。消息一直在队列里面，等待消费者连接到这个队列将其取走。

### 网络连接(Connection)

 网络连接，比如一个TCP连接。

### 信道（Channel）

 信道，多路复用连接中的一条独立的双向数据流通道。信道是建立在真实的TCP连接内地虚拟连接，AMQP 命令都是通过信道发出去的，不管是发布消息、订阅队列还是接收消息，这些动作都是通过信道完成。因为对于操作系统来说建立和销毁 TCP 都是非常昂贵的开销，所以引入了信道的概念，以复用一条 TCP 连接。

### 消息的消费者（Consumer）

 消息的消费者，表示一个从消息队列中取得消息的客户端应用程序。

### 虚拟主机（Virtual Host）

 虚拟主机，表示一批交换器、消息队列和相关对象。虚拟主机是共享相同的身份认证和加密环境的独立服务器域。每个 vhost 本质上就是一个 mini 版的 RabbitMQ 服务器，拥有自己的队列、交换器、绑定和权限机制。vhost 是 AMQP 概念的基础，必须在连接时指定，RabbitMQ 默认的 vhost 是 / 。

### 消息队列服务器实体（Broker）

 表示消息队列服务器实体。

## 依赖

如果使用我们com.dv.cloud框架构建 ，添加我们自己封装的依赖

```xml
<dependency>
    <groupId>com.dv.cloud</groupId>
    <artifactId>dv-cloud-stream-starter</artifactId>
</dependency>
```

如果没有使用我们自己的框架可以直接引用，spring-cloud-stream 的包

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

## 配置 

### rabbitMq 配置

virtual-host：在RabbitMQ中可以虚拟消息服务器VirtualHost，每个VirtualHost相当月一个相对独立的RabbitMQ服务器，每个VirtualHost之间是相互隔离的。exchange、queue、message不能互通。

```yaml
spring:
  rabbitmq:
    host: locahost
    port: 5672
    #在RabbitMQ中可以虚拟消息服务器VirtualHost，每个VirtualHost相当月一个相对独立的RabbitMQ服务器，每个VirtualHost之间是相互隔离的。exchange、queue、message不能互通。
    virtual-host: bsszxc-test  
    username: XXXXXX
    password: XXXXXX
```

### Spring-cloud-Stream 配置

spring.cloud.stream.bindings.<channelName>：绑定通道配置
<channelName>表示正在配置的通道的名称（例如producer,consumer,output,input）。

spring.cloud.stream.bindings.<channelName>.destination：绑定中间件上的通道的目标（RabbitMQ交换机或Kafka主题）

spring.cloud.stream.bindings.group：消费组，可以解决消息重复消费问题。当前主题发送的消息在每个订阅消费组中，只会有一个订阅者接收和消费



```yaml
server:
  port: 8080
spring:
  application:
    name: stream-rabbit-demo
    stream:
      bindings:
        #消费者配置
        #延迟演示
        delay-consumer:
          destination: demo.delay.topic
          group: test-group
        #路由演示
        route-consumer:
          destination: demo.route.topic
          group: test-group
        #异常演示
        exception-consumer:
          destination: demo.exception.topic
          group: test-group
          consumer:
            max-attempts: 3 # 消费尝试次数，默认值为3。设置为1 代表不重试
            requeue-rejected: true # 在该配置作用之下，消息消费失败之后，并不会将该消息抛弃，而是将消息重新放入队列，所以消息的消费逻辑会被重复执行，直到这条消息消费成功为止。
        #死信队列演示
        dlq-consumer:
          content-type: application/json
          destination: demo.dlq.topic
          group: test-group
          consumer:
            ttl: 20000 # 默认不做限制，即无限。消息在队列中最大的存活时间。当消息滞留超过ttl时，会被当成消费失败消息，即会被转发到死信队列或丢弃.
            concurrency: 1 # 初始/最少/空闲时 消费者数量。默认1 stream  会动态增加消费者
            max-attempts: 3 # 消费尝试次数，默认值为3。设置为1 代表不重试
            back-off-initial-interval: 1000 # 消息消费失败后重试消费消息的初始化间隔时间。默认1s，即第一次重试消费会在1s后进行
            back-off-multiplier: 5 # 相邻两次重试之间的间隔时间的倍数。默认2，即第二次是第一次间隔时间的2倍，第三次是第二次的2倍
            back-off-max-interval: 10000 # 下一次尝试重试的最大时间间隔，默认为10000ms，即10s。
        #生产者配置
        delay-producer:
          destination: demo.delay.topic
        route-producer:
          destination: demo.route.topic
        exception-producer:
          destination: demo.exception.topic
        dlq-producer:
          destination: demo.dlq.topic
      rabbit:
        bindings:
          #消费者配置
          delay-consumer:
            consumer:
              # 开启延迟消息队列
              delayed-exchange: true
          route-consumer:
            consumer:
              delayed-exchange: true
          exception-consumer:
            consumer:
              delayed-exchange: true
          dlq-consumer:
            consumer:
              delayed-exchange: true
              # 死信队列DLQ相关
              auto-bind-dlq: true  # 是否自动声明死信队列（DLQ）并将其绑定到死信交换机（DLX）。默认是false。
#              dead-letter-queue-name: 'demo.dlq.test-group.dlq' # 默认prefix + destination + group + .dlq。DLQ的名称。
#              dead-letter-exchange: 'DLX' # 默认prefix + DLX。DLX的名称
#              dead-letter-routingKey: 'demo.dlq.test-group' # 默认destination + group
#              dlq-expires: 30000 # 队列所有 customer 下线, 且在过期时间段内 queue 没有被重新声明, 多久之后队列会被销毁, 注意, 不管队列内有没有消息. 默认不设置.
#              dlq-lazy: false # 是否声明为惰性队列（Lazy Queue）.默认false
#              dlq-max-length: 100000 # 队列中消息数量的最大限制. 默认不限制
#              dlq-max-length-bytes: 100000000 # 队列所有消息总字节的最大限制. 默认不限制
#              dlq-max-priority: 255 # 队列的消息可以设置的最大优先级. 默认不设置
#              dlq-ttl: 1000000 # 队列的消息的过期时间. 默认不限制
#              republish-toDlq: true # 默认false。当为true时，死信队列接收到的消息的headers会更加丰富，多了异常信息和堆栈跟踪。
#              republish-delivery-mode: DeliveryMode.PERSISTENT # 默认DeliveryMode.PERSISTENT（持久化）。当republishToDlq为true时，转发的消息的delivery mode
          #生产者配置
          delay-producer:
            producer:
              # 开启延迟消息队列
              delayed-exchange: true
          route-producer:
            producer:
              # 开启延迟消息队列
              delayed-exchange: true
          exception-producer:
            producer:
              delayed-exchange: true
          dlq-producer:
            producer:
              # 开启延迟消息队列
              delayed-exchange: true

logging:
  file:
    name: ../logs/dvp/${spring.application.name}/${spring.application.name}.log

```

## 代码实现

### 生产者

#### 输出管道定义

`@Output(VILLAGE_PRODUCER)`定义了一个输出通道，并且给通道命名 village-produce

```java
/**
 *
 */
public interface ProducerQueue {

    /**
     *  延迟演示队列
     */
    String DELAY_PRODUCER= "delay-producer";

    /**
     *  路由演示队列
     */
    String ROUTE_PRODUCER = "route-producer";

    /**
     *  异常演示队列
     */
    String EXCEPTION_PRODUCER = "exception-producer";

    /**
     *  死信演示队列
     */
    String DLQ_CONSUMER = "dlq-consumer";
    /**
     *  自定义消费 死信队列
     */
    String DLQ_QUEUE = "demo.dlq.topic.test-group";
    String DLQ = DLQ_QUEUE + ".dlq";

    @Output(DELAY_PRODUCER)
    MessageChannel delayProducer();

    @Output(ROUTE_PRODUCER)
    MessageChannel routeProducer();

    @Output(EXCEPTION_PRODUCER)
    MessageChannel excepProducer();

    @Output(DLQ_PRODUCER)
    MessageChannel dlqProducer();

}
```

#### 消息发送示例

`@EnableBinding`，该注解用来指定一个或多个定义了`@Input`或`@Output`注解的接口，以此实现对消息通道（Channel）的绑定

```java
@Slf4j
@RestController
@EnableBinding(ProducerQueue.class)
public class TestController {

    @Autowired
    private ProducerQueue producerQueue;

    /**
     * 发送消息
     * @param message
     * @return
     */
    @GetMapping("/sendMessage")
    public String messageWithMQ(@RequestParam String message) {
        producerQueue.delayProducer().send(MessageBuilder.withPayload(message).build());
        return "ok";
    }

    /**
     * 发送延迟消息
     * @param message
     * @param delay
     * @return
     */
    @GetMapping("/delay/sendMessage")
    public String sendDelay(@RequestParam String message,@RequestParam Long delay) {
        Message<String> messageVo = MessageBuilder
                .withPayload(message).setHeader("x-delay", delay).build();
        log.info("delay-message"+ JSONObject.toJSONString(messageVo));
        producerQueue.delayProducer().send(messageVo);
        Date date = new Date();
        System.out.print("订单创建成功"+date);
        return "ok";
    }

    /**
     * 发送路由
     * @param message
     * @param version
     * @return
     */
    @GetMapping("/route/sendMessage")
    public String sendRoute(@RequestParam String message,@RequestParam String version) {
        producerQueue.routeProducer().send(MessageBuilder.withPayload(message).setHeader("version", version).build());
        return "ok";
    }

    /**
     * 发送错误信息路由
     * @param message
     * @param version
     * @return
     */
    @GetMapping("/exception/sendMessage")
    public String sendExce(@RequestParam String message,@RequestParam String version) {
        producerQueue.excepProducer().send(MessageBuilder.withPayload(message).setHeader("version", version).build());
        return "ok";
    }

    /**
     * 发送死信
     * @param message
     * @return
     */
    @GetMapping("/dlq/sendMessage")
    public String sendDeath(@RequestParam String message) {
        producerQueue.dlqProducer().send(MessageBuilder.withPayload(message).build());
        return "ok";
    }
}
```

。

### 消费者

#### 输入管道定义

`@Input(VILLAGE_PRODUCER)`定义了一个输入通道，并且给通道命名 village-produce

```java
/**
 * 消费者队列定义
 */
public interface ConsumerQueue {

    /**
     *  延迟演示队列
     */
    String DELAY_CONSUMER = "delay-consumer";

    /**
     *  路由演示队列
     */
    String ROUTE_CONSUMER = "route-consumer";

    /**
     *  异常演示队列
     */
    String EXCEPTION_CONSUMER = "exception-consumer";

    /**
     *  死信演示队列
     */
    String DLQ_CONSUMER = "dlq-consumer";

    @Input(DELAY_CONSUMER)
    SubscribableChannel delayConsumer();

    @Input(ROUTE_CONSUMER)
    SubscribableChannel routeConsumer();

    @Input(EXCEPTION_CONSUMER)
    SubscribableChannel exceConsumer();

    @Input(DLQ_CONSUMER)
    SubscribableChannel dlqConsumer();


}
```

#### 消息接收示例

`@StreamListener`：该注解主要定义在方法上，作用是将被修饰的方法注册为消息中间件上数据流的事件监听器，注解中的属性值对应了监听的消息通道名。

`@ServiceActivator` 订阅任何一个错误通道来处理错误；如果不订阅，则只会记录错误并确认消息成功。如果错误通道服务激活器引发异常，则消息将被拒绝（默认情况下），并且不会被重新传递。

对于每个输入绑定，Spring Cloud Stream 使用以下语义创建一个专用的错误通道 `<destinationName>.errors`。

```java
@Slf4j
@Component
@EnableBinding(ConsumerQueue.class)
public class TestListener {

    /**
     * 消费演示，延迟雄安发演示
     * @param entity
     */
    @StreamListener(ConsumerQueue.DELAY_CONSUMER)
    public void receiveDelay(Message<String> entity) {
        log.info("监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) +"]");
        Date date = new Date();
        System.out.print("订单超时取消 ："+date);
    }

    /**
     * 路由演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='1.0'")
    public void receiveRoute1(Message<String> entity, @Header("version") String version) {
        log.info("1.0监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
    }
    /**
     * 路由演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='2.0'")
    public void receiveRoute2(Message<String> entity, @Header("version") String version) {
        log.info("2.0监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
    }

    /**
     * 异常演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='exceptionTest'")
    public void receiveException(Message<String> entity, @Header("version") String version) {
        log.info("测试模拟异常 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
        throw new RuntimeException("Message consumer failed!");
    }

    /**
     * 异常，自定义处理演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.EXCEPTION_CONSUMER,condition = "headers['version']=='exceptionDealTest'")
    public void receiveExceptionDeal(Message<String> entity, @Header("version") String version) {
        log.info("自定义异常处理 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
        throw new RuntimeException("Message consumer failed!");
    }

    /**
     * 消息消费失败的降级处理逻辑
     *
     * @param message
     */
    @ServiceActivator(inputChannel = "demo.exception.topic.test-group.errors")
    public void error(Message<?> message) {
        log.info("消息消费失败的降级处理逻辑---");
    }

    /**
     * 异常死信队列演示
     * @param entity
     */
    @StreamListener(ConsumerQueue.DLQ_CONSUMER)
    public void receiveDlq(Message<String> entity) {
        int count = 1;

        log.info("死信 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + "]");
        // 进入DLQ的逻辑
        if (count == 3) {
            count = 1;
            throw new AmqpRejectAndDontRequeueException("tried 3 times failed, send to dlq! 进入死信队列");
        } else {
            count ++;
            throw new RuntimeException("Message consumer failed!");
        }
    }
    
    /**
     * 再次消费死信队列的处理逻辑
     * @param failedMessage
     * concurrency # 消费端的监听个数(即@RabbitListener开启几个线程去处理数据。)
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(ConsumerQueue.DLQ)
                    , exchange = @Exchange(ConsumerQueue.DLX)
                    , key = ConsumerQueue.DLQ_QUEUE
            ),
            concurrency = "1-5"
    )
    public void handleDlq(Message failedMessage) throws InterruptedException {
        Thread.sleep(10);
        log.info("重新消费 死信队列. 完整消息: {};", failedMessage);

        log.info("重新消费 死信队列. body: {}", new String( (byte[])failedMessage.getPayload() ));
    }

}
```

## RabbitMq 管理平台简单介绍

### 用户创建

![addUser](/doc/addUser.jpg)

### 虚拟主机创建

![image-20210719143441594](/doc/addvirtualhost.png)

### 虚拟主机切换

![virtual-host](/doc/virtual-host.jpg)

### 查看交换器(Exchange)

 ![exchange](/doc/exchange.jpg)



### 查看消息队列(Queue)

 ![queue](/doc/queue.jpg)

### 查看消息（message）

点击要查看的queue

![messagedetail](/doc/messagedetail.jpg)

点击Get Message

![getmessagedetail](/doc/getmessagedetail.jpg)
### 查看网络连接(Connection)

 ![connection](/doc/connection.jpg)

### 查看信道（Channel）

![channel](/doc/channel.jpg)


## 参考资料

其它配置可以参考：

https://www.springcloud.cc/spring-cloud-brixton.html#multiple-binders

https://ziyueknow.com/page/spring-cloud/part05-06.html#spring-cloud-stream-overview-producing-consuming-messages

https://www.jianshu.com/p/79ca08116d57

demo 搭建参考：https://github.com/dyc87112/SpringCloud-Learning/tree/master/4-Finchley

