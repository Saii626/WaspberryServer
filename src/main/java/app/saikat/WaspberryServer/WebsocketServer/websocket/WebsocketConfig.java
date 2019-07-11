package app.saikat.WaspberryServer.WebsocketServer.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

// @Configuration
// @EnableWebSocket
// public class WebsocketConfig implements WebSocketConfigurer {

// //     // private Logger logger;

// //     // public WebsocketConfig(Logger logger) {
// //     //     this.logger = logger;
// //     // }

//     @Override
//     public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//         // if (server == null) {
//         //     logger.error("Websocket server null");
//         //     return;
//         // }
//         registry.addHandler(websocketServer(), "/socket").setAllowedOrigins("*").withSockJS();
//     }

//     @Bean
//     public WebsocketServer websocketServer() {
//         return new WebsocketServer();
//     }

// }

@Configuration  
public class WebsocketConfig {  
    @Bean  
    public ServerEndpointExporter serverEndpointExporter(){  
        return new ServerEndpointExporter();  
    }  

    @Bean
    public NewWebsocket newWebsocket() {
        return new NewWebsocket();
    }
} 