"use client";

import { createContext, useContext, useEffect, useState } from "react";

const SSEContext = createContext({
    activeCount: 0,
    logs: [] as string[],
});

export const SSEProvider = ({ children }: { children: React.ReactNode }) => {
    const [activeCount, setActiveCount] = useState(0);
    const [logs, setLogs] = useState<string[]>([]);

        useEffect(() => {
            const sse = new EventSource('/api/sse');

            sse.onerror = (err) => {
                console.error("SSE error:", err);
                sse.close();
            }
        
            sse.onmessage = (event) => {
                console.log("New SSE message:", event.data);
                setLogs((prev) => [...prev, event.data]);
            };

            sse.addEventListener('log', (event) => {
                console.log("New SSE message:", event.data);
                setLogs((prev) => [...prev, event.data]);
            });

            sse.addEventListener('connected', (event) => {
                console.log("Connected clients update:", event.data);
                setActiveCount(event.data);
            });
    
            return () => {
                sse.close();
            };
        }, []);
    
        return (
            <SSEContext.Provider value={{ activeCount, logs }}>
                {children}
            </SSEContext.Provider>
        );
    };

    export const useSSE = () => {
        return useContext(SSEContext);
    }