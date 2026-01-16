# BuildYourOwnWebserver (Glass Box)
Glass Box is an educational HTTP/1.1 server built to demystify web protocols. It exposes the "hidden" work of a web server by streaming internal metrics to a client-side dashboard, allowing users to watch their requests flow through the parsing, routing, and response pipeline live.

## Milestones
The Milestones can be found under the [Milestones Tab](https://github.com/90S31D0N/BuildYourOwnWebserver/milestones)
## Features
The Features are listed under the [Issues Tab](https://github.com/90S31D0N/BuildYourOwnWebserver/issues)

```mermaid
classDiagram
    class HTTP1Handler{
        +runTask(Socket)
    }

    class AbstractHandler{
        +  handle (Socket, Executor Serivice)
        + abstract runTask(Socket)
    }

    class TCPServer{
        - int port
        - AbstractHandler handler
        + run()
        + stopServer()
    }

    class Main{
        + main()
    }

    class HTTPErrorHandler{
        + static sendBadRequest(socket)
        + static sendNotFound(socket)
        + static sendInternalError(socket)
        + static sendMethodeNotAllowed(socket)
        + static sendNotImplemented(socket)
    }

    class HTTPRequestHandler{
        + HTTPRequest parseHTTPRequest(socket)
    }

    class HTTPRequest {
        - HTTPMethode methode
        - String path
        - String httpVersion
        - Map<String, String[]> headers
        - byte[] bodyBytes
        + toString()
    }

    HTTP1Handler --> AbstractHandler : extends
    HTTP1Handler --> HTTPErrorHandler : uses
    HTTP1Handler --> HTTPRequestHandler : uses
    HTTPRequestHandler --> HTTPRequest : creates
    Main --> HTTP1Handler : instantiates
    Main --> TCPServer : instantiates
    TCPServer --> HTTP1Handler : uses
```

