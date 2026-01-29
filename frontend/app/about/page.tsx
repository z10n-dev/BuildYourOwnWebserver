"use client";
import { MermaidDiagram } from "@lightenna/react-mermaid-diagram"

export default function About() {
  const diagramText = `classDiagram
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
    TCPServer --> HTTP1Handler : uses`;
  return (
    <main className="min-h-screen bg-zinc-50 dark:bg-black">
      <div className="max-w-4xl mx-auto px-4 py-16">
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-8 text-center">About Glass Box</h1>

        <MermaidDiagram>
          {diagramText}
        </MermaidDiagram>
        
        <div className="bg-white dark:bg-zinc-900 rounded-lg shadow-md p-8 mb-8">
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 mb-4">Our Mission</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed mb-4">
            At Glass Box, we believe in transparency and building solutions that empower our users. 
            Our mission is to create innovative web services that are accessible, reliable, and user-friendly.
          </p>
        </div>

        <div className="bg-white dark:bg-zinc-900 rounded-lg shadow-md p-8 mb-8">
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 mb-4">Our Vision</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed mb-4">
            We envision a future where web applications are built with clarity and purpose, 
            where every line of code serves a meaningful function, and where users experience 
            the true potential of modern web technology.
          </p>
        </div>

        <div className="bg-white dark:bg-zinc-900 rounded-lg shadow-md p-8">
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 mb-4">Why Choose Us?</h2>
          <ul className="text-gray-700 dark:text-gray-300 space-y-2">
            <li className="flex items-start">
              <span className="text-blue-500 mr-3">✓</span>
              <span>Transparent and open development practices</span>
            </li>
            <li className="flex items-start">
              <span className="text-blue-500 mr-3">✓</span>
              <span>High performance and reliability</span>
            </li>
            <li className="flex items-start">
              <span className="text-blue-500 mr-3">✓</span>
              <span>Dedicated customer support</span>
            </li>
            <li className="flex items-start">
              <span className="text-blue-500 mr-3">✓</span>
              <span>Continuous innovation and improvement</span>
            </li>
          </ul>
        </div>
      </div>
    </main>
  );
}
