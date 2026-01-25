'use client'

import React from 'react'
import Terminal from '../components/Terminal';
import ActiveClientsGraph from '../components/ActiveClientsStats';

export default function page() {
  const [data, setData] = React.useState<string[]>([]);

  React.useEffect(() => {
    let sse = new EventSource('/api/sse');
    sse.onmessage = function(event) {
      console.log("New SSE message:", event.data);
      setData(prev => [...prev, event.data]);
    };

    return () => {
      sse.close();
    };
  }, []);

  return (
    <div className='flex flex-col max-h-screen items-center'>
        <h1 className='text-3xl font-bold mb-4 flex justify-center mt-10'>Live Dashboard</h1>
      
        {/* <div className='w-full h-1/2 overflow-y-auto border border-gray-300 rounded p-4'>
          {data.map((item, index) => (
            <div key={index} className='mb-2 p-2 bg-gray-800 rounded'>
              {item}
            </div>
          ))}
        </div> */}

        <div className='grid grid-cols-2 gap-4 w-screen my-10 mx-10 px-10'>
          <Terminal logs={data}></Terminal>
          <ActiveClientsGraph></ActiveClientsGraph>
        </div>

    </div>
  )
}
