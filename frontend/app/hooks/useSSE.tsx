"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { json } from "stream/consumers";

const SSEContext = createContext({
    stats: [] as stat[],
    logs: [] as logEntry[],
});

export interface logEntry {
    logLevel: string;
    message: string;
    timestamp: Date;
}

export interface stat {
    cpuUsage: number;
    memoryUsage: number;
    activeConnections: number;
    totalRequests: number;
    uptime: number;
}

export const SSEProvider = ({ children }: { children: React.ReactNode }) => {
    const [stats, setStats] = useState<stat[]>([]);
    const [logs, setLogs] = useState<logEntry[]>([]);

        useEffect(() => {
            const sse = new EventSource('/api/sse');

            sse.onerror = (err) => {
                console.error("SSE error:", err);
                sse.close();
            }
        
            sse.onmessage = (event) => {
                console.log("New SSE message:", event.data);
            };

            sse.addEventListener('log', (event) => {
                // console.log("New SSE message:", event.data);
                const data = JSON.parse(event.data);
                const logEntry: logEntry = {
                    logLevel: data.level,
                    message: data.message,
                    timestamp: new Date(data.timestamp),
                };
                setLogs((prev) => [...prev, logEntry]);
            });

            sse.addEventListener('stats', (event) => {
                console.log("Stats update:", event.data);
                const data = JSON.parse(event.data);
                const statEntry: stat = {
                    cpuUsage: data.cpuUsage,
                    memoryUsage: data.memoryUsage,
                    activeConnections: data.activeConnections,
                    totalRequests: data.totalRequests,
                    uptime: data.uptime,
                };
                setStats((prev) => [...prev, statEntry]);
            });
    
            return () => {
                sse.close();
            };
        }, []);
    
        return (
            <SSEContext.Provider value={{ stats, logs }}>
                {children}
            </SSEContext.Provider>
        );
    };

    export const useSSE = () => {
        return useContext(SSEContext);
    }