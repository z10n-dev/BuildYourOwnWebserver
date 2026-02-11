# BuildYourOwnWebserver (Glass Box)
Glass Box is an educational HTTP/1.1 server built to demystify web protocols. It exposes the "hidden" work of a web server by streaming internal metrics to a client-side dashboard, allowing users to watch their requests flow through the parsing, routing, and response pipeline live.

## Milestones
The Milestones can be found under the [Milestones Tab](https://github.com/90S31D0N/BuildYourOwnWebserver/milestones)
## Features
The Features are listed under the [Issues Tab](https://github.com/90S31D0N/BuildYourOwnWebserver/issues)

```mermaid
classDiagram
      direction TB

      %% === ENTRY POINT ===
      class Main {
          +main(String[] args)$
      }

      %% === TCP/HTTP CORE ===
      class TCPServer {
          -HTTPHandler handler
          -ServerSocket serverSocket
          -ExecutorService pool
          +run()
          +stopServer()
      }
      Thread <|-- TCPServer

      class HTTPHandler {
          -VirtualHostManager vManager
          +handle(Socket socket, ExecutorService pool)
          -runTask(Socket socket)
      }

      class HTTPRequest {
          -HTTPMethode method
          -String path
          -String httpVersion
          -Map~String, String[]~ headers
          -byte[] bodyBytes
          -String host
          +getMethod() HTTPMethode
          +getPath() String
          +getBody() String
          +getHost() String
      }

      class HTTPResponse {
          -int statusCode
          -String statusMessage
          -HashMap~String, String[]~ headers
          -byte[] body
          -boolean sended
          +setHeader(String key, String value)
          +setBody(byte[] body, String contentType)
          +send(Socket socket)
          +isSended() boolean
      }

      class HTTPMethode {
          <<enumeration>>
          GET
          POST
          PUT
          DELETE
          HEAD
          OPTIONS
      }

      class HTTPRequestParser {
          +parseHTTPRequest(Socket socket)$ HTTPRequest
          +getHTTPHeader(InputStream in)$ String
          -extractMethode(String[] line)$ HTTPMethode
          -extractPath(String[] line)$ String
          -extractHttpVersion(String[] line)$ String
          -extractHttpHeaders(BufferedReader in)$ LinkedHashMap
          +bodyStream(InputStream in, int contentLength)$ InputStream
      }

      class HTTPErrorHandler {
          +sendError(Socket, int, String)$
          +sendBadRequest(Socket)$
          +sendNotFound(Socket)$
          +sendInternalError(Socket)$
          +sendMethodNotAllowed(Socket)$
          +sendNotImplemented(Socket)$
          +handleException(Socket, Exception)$
      }

      class FixedLengthInputStream {
          -InputStream in
          -long remaining
          +read() int
      }
      InputStream <|-- FixedLengthInputStream

      %% === REQUEST HANDLER HIERARCHY ===
      class RequestHandler {
          <<abstract>>
          +handle(HTTPRequest request)* HTTPResponse
          +handle(HTTPRequest request, Socket socket) HTTPResponse
      }

      class MethodeBasedHandler {
          <<abstract>>
          +handle(HTTPRequest request) HTTPResponse
          #handleGetRequest(HTTPRequest) HTTPResponse
          #handlePostRequest(HTTPRequest) HTTPResponse
          #handlePutRequest(HTTPRequest) HTTPResponse
          #handleDeleteRequest(HTTPRequest) HTTPResponse
          #handleHeadRequest(HTTPRequest) HTTPResponse
      }
      RequestHandler <|-- MethodeBasedHandler

      class RouteBasedHandler {
          #HashMap~String, RouteCommand~ routes
          +handle(HTTPRequest request) HTTPResponse
      }
      RequestHandler <|-- RouteBasedHandler

      class StaticFileHandler {
          -String rootPath
          +handleGetRequest(HTTPRequest) HTTPResponse
          +handleHeadRequest(HTTPRequest) HTTPResponse
      }
      MethodeBasedHandler <|-- StaticFileHandler

      class HelloWorldHandler {
          +handleGetRequest(HTTPRequest) HTTPResponse
      }
      MethodeBasedHandler <|-- HelloWorldHandler

      class ToDoHandler {
          -HashMap~UUID, ToDo~ toDoStore
          +handleOptions(HTTPRequest) HTTPResponse
          -sendAllToDos(HTTPRequest) HTTPResponse
          -addTodo(HTTPRequest) HTTPResponse
          -updateTodo(HTTPRequest) HTTPResponse
          -deleteTodo(HTTPRequest) HTTPResponse
      }
      RouteBasedHandler <|-- ToDoHandler

      class SSEHandler {
          -Set~Socket~ sockets
          +handle(HTTPRequest, Socket) HTTPResponse
          +broadcast(SSEEvent event, String message)
          +send(Socket socket, String message)
          +removeSocket(Socket socket)
          +hasClients() boolean
          +clientCount() int
      }
      RequestHandler <|-- SSEHandler

      class RouteCommand {
          <<interface>>
          +run(HTTPRequest request) HTTPResponse
      }

      %% === MODELS ===
      class ToDo {
          <<record>>
          +UUID id
          +String title
          +boolean completed
      }

      %% === CONFIGURATION ===
      class ServerConfig {
          -String environment
          -int port
          -Loglevel loglevel
          -VirtualHostConfig defaultHost
          -Map~String, VirtualHostConfig~ hosts
          +getPort() int
          +getLoglevel() Loglevel
          +getDefaultHost() VirtualHostConfig
          +getHosts() Map
      }

      class VirtualHostConfig {
          -String host
          -String documentRoot
          -RouterConfig router
          +getHost() String
          +getDocumentRoot() String
          +getRouter() RouterConfig
          +getSSEHandler() SSEHandler
      }

      class VirtualHostManager {
          -HashMap~String, VirtualHostConfig~ virtualHosts
          -VirtualHostConfig defaultVirtualHost
          +registerVirtualHost(VirtualHostConfig vhost)
          +getVirtualHost(String host) VirtualHostConfig
          +getSSEHandler() SSEHandler
      }

      class RouterConfig {
          -Map~String, RequestHandler~ routes
          -MethodeBasedHandler defaultHandler
          +register(String pathPattern, RequestHandler handler)
          +findHandler(HTTPRequest request) RequestHandler
          -matchPattern(String path, String pattern) boolean
          +getSSEHandler() SSEHandler
      }

      class ConfigLoader {
          +load(String environment)$ ServerConfig
          -buildHosts(Map hosts)$ Map
      }

      class HandlerFactory {
          -Map~String, Class~ handlerCache$
          +getRequestHandlerByName(String className)$ RequestHandler
      }

      %% === LOGGING ===
      class ServerLogger {
          -ServerLogger instance$
          -SSEHandler sseHandler
          -Loglevel loglevel
          -BlockingQueue~Log~ logQueue
          -boolean running
          +initialize(SSEHandler, Loglevel)$
          +getInstance()$ ServerLogger
          +log(Loglevel, String, LogDestination)
          -processLog(Log)
          +run()
          +shutdown()
      }
      Runnable <|.. ServerLogger

      class Stats {
          -Stats instance$
          -AtomicLong startTime
          -AtomicLong totalRequests
          -AtomicLong activeConnections
          -boolean running
          +init()$
          +getInstance()$ Stats
          +getStatsAsJson() JSONObject
          +incrementRequests()
          +getCPUUsage() double
          +getMemUsage() double
          +run()
      }
      Runnable <|.. Stats

      class Log {
          -Loglevel level
          -String message
          -long timestamp
          -LogDestination destination
          +toJson() JSONObject
      }

      class Loglevel {
          <<enumeration>>
          DEBUG
          INFO
          WARN
          ERROR
          FATAL
          +getLevel() int
          +isHigherOrEqual(Loglevel) boolean
      }

      class LogDestination {
          <<enumeration>>
          SERVER
          CLIENT
          EVERYWHERE
      }

      class SSEEvent {
          <<enumeration>>
          LOG
          STATS
          CONNECTED
          HEARTBEAT
      }

      %% === EXCEPTIONS ===
      class BadRequestException
      class NotFoundException
      class InternalServerErrorException
      class NotImplementedException
      Exception <|-- BadRequestException
      Exception <|-- NotFoundException
      Exception <|-- InternalServerErrorException
      Exception <|-- NotImplementedException

      %% === RELATIONSHIPS ===
      Main --> TCPServer : creates
      Main --> ConfigLoader : uses
      Main --> ServerLogger : initializes
      Main --> VirtualHostManager : creates

      TCPServer --> HTTPHandler : uses
      HTTPHandler --> VirtualHostManager : uses
      HTTPHandler --> HTTPRequestParser : uses
      HTTPHandler --> HTTPErrorHandler : uses
      HTTPHandler --> HTTPRequest : processes
      HTTPHandler --> HTTPResponse : creates

      HTTPRequest --> HTTPMethode : contains
      HTTPRequestParser --> FixedLengthInputStream : creates

      VirtualHostManager --> VirtualHostConfig : manages
      VirtualHostConfig --> RouterConfig : contains
      RouterConfig --> RequestHandler : routes to
      RouterConfig --> StaticFileHandler : default

      RouteBasedHandler --> RouteCommand : uses
      ToDoHandler --> ToDo : stores

      SSEHandler --> SSEEvent : uses
      SSEHandler --> Stats : broadcasts

      ServerLogger --> SSEHandler : pushes logs
      ServerLogger --> Log : queues
      ServerLogger --> Loglevel : filters by
      Log --> Loglevel : has
      Log --> LogDestination : has

      ConfigLoader --> ServerConfig : creates
      ConfigLoader --> HandlerFactory : uses
      ServerConfig --> VirtualHostConfig : contains
      ServerConfig --> Loglevel : contains
```

