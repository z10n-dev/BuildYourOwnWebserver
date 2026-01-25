"use client";

import React, { useEffect, useState } from 'react'

export default function Connected() {
    const [data, setData] = useState<string[]>([]);
      
        useEffect(() => {
          let sse = new EventSource('/api/clients');
          sse.onmessage = function(event) {
            console.log("New SSE message:", event.data);
            setData(prev => [...prev, event.data]);
          };
      
          return () => {
            sse.close();
          };
        }, []);
  return (
    <div>Connected</div>
  )
}
