'use client'

import React from 'react'

export default function page() {
  const [data, setData] = React.useState<string>("");

  React.useEffect(() => {
    let sse = new EventSource('/api/sse');
    sse.onmessage = function(event) {
      console.log("New SSE message:", event.data);
      setData(event.data);
    };

    return () => {
      sse.close();
    };
  }, []);

  return (
    <div className='flex flex-col min-h-screen'>
        <h1 className='text-3xl font-bold mb-4 flex justify-center mt-10'>Live Dashboard</h1>
        <div className='flex-grow flex justify-center items-center'>
            <div className='text-center'>
                <h2 className='text-2xl font-semibold mb-4'>Real-time Data:</h2>
                <pre className='bg-gray-100 text-black p-4 rounded-lg shadow-md text-left max-w-md mx-auto'>{data}</pre>
            </div>
        </div>
    </div>
  )
}
