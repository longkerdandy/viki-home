package com.github.longkerdandy.viki.home.hap;

import static com.github.longkerdandy.viki.home.util.Configurations.getPropertiesConfiguration;

import com.github.longkerdandy.viki.home.ext.ControllerExt;
import com.github.longkerdandy.viki.home.hap.http.HAPChannelInboundHandler;
import com.github.longkerdandy.viki.home.hap.mdns.HAPmDNSAdvertiser;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.hap.storage.Registry;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import com.github.longkerdandy.viki.home.util.Networks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HomeKit Accessory Protocol Extension
 */
public class HomeKitProtocolExt implements ControllerExt {

  // extension name
  public static final String EXT_NAME = "HomeKit Accessory Protocol";

  private static final Logger logger = LoggerFactory.getLogger(HomeKitProtocolExt.class);

  // configuration
  private final PropertiesConfiguration config;
  // local address
  private final InetAddress address;
  // host name
  private final String hostname;
  // mac address
  private final String macAddress;
  // http server port
  private final int port;
  // pin code
  private final String pinCode;
  // storage
  private final HAPStorage hapStorage;
  // session registry
  private final Registry registry;
  // mDNS
  private HAPmDNSAdvertiser advertiser;
  // netty
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  /**
   * Constructor
   */
  public HomeKitProtocolExt(Locale locale, SQLiteStorage storage) {
    try {
      this.config = getPropertiesConfiguration("config/viki-home-hap.properties");
      this.address = Networks.getLocalInetAddress();
      this.hostname = Networks.getHostname(this.address);
      this.macAddress = Networks.getMacAddress(this.address);
      this.port = config.getInt("hap.port");
      this.pinCode = config.getString("hap.pin");
      this.hapStorage = new HAPStorage(config);
      this.registry = new Registry();
    } catch (ConfigurationException | IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getExtName() {
    return EXT_NAME;
  }

  @Override
  public void init() throws IOException, InterruptedException {
    logger.info("Initializing HomeKit Accessory Protocol Extension ...");

    logger.info("Migrating HAP storage to new version if necessary ...");
    Flyway.configure()
        .dataSource(this.hapStorage.getDataSource())
        .locations(this.config.getString("storage.migration.scripts"))
        .encoding(this.config.getString("storage.migration.encoding", "UTF-8"))
        .load().migrate();

    logger.info("Initializing HAP storage ...");
    this.hapStorage.init();

    logger.info("Initializing mDNS discovery service ...");
    initDiscovery();

    logger.info("Initializing HTTP server ...");
    initHTTPServer();

    logger.info("HomeKit Accessory Protocol Extension is initialized ...");
  }

  /**
   * Initialize the advertiser
   */
  protected void initDiscovery() throws IOException {
    this.advertiser = new HAPmDNSAdvertiser(
        this.address, this.hostname, this.macAddress, this.port, this.hapStorage);

    // Register the bridge service
    this.advertiser.registerBridgeService();
  }

  /**
   * Initialize HTTP service
   */
  protected void initHTTPServer() throws InterruptedException {
    // Configure EventLoopGroup
    this.bossGroup = new NioEventLoopGroup(1);
    this.workerGroup = new NioEventLoopGroup();

    // Bootstrap the http server
    ServerBootstrap b = new ServerBootstrap();
    b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<>() {
          @Override
          protected void initChannel(Channel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("log", new LoggingHandler(LogLevel.DEBUG));
            pipeline.addLast("codec", new HttpServerCodec());
            pipeline.addLast("aggregator", new HttpObjectAggregator(1073741824));
            pipeline.addLast("hap", new HAPChannelInboundHandler(
                hapStorage, macAddress, pinCode, registry, advertiser));
          }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true);

    // Bind network address and port, start the http server
    b.bind(this.address, this.port).sync().channel();
  }

  @Override
  public void destroy() throws IOException {
    logger.info("Destroying HomeKit Accessory Protocol Extension ...");

    logger.info("Destroying mDNS discovery service ...");
    this.advertiser.shutdown();

    logger.info("Destroying HTTP server ...");
    // Gracefully shutdown the EventLoopGroup
    this.bossGroup.shutdownGracefully();
    this.workerGroup.shutdownGracefully();

    logger.info("HomeKit Accessory Protocol Extension is destroyed ...");
  }
}
