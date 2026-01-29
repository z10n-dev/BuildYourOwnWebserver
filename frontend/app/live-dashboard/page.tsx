'use client'

import React from 'react'
import Terminal from '../components/Terminal';
import ActiveClientsGraph from '../components/ActiveClientsStats';
import { useSSE } from '../hooks/useSSE';

export default function page() {
  const { activeCount, logs} = useSSE();

  return (
    <div className='flex flex-col min-h-screen items-center'>
        <h1 className='text-3xl font-bold mb-4 flex justify-center mt-10'>Live Dashboard V2</h1>
      
        {/* <div className='w-full h-1/2 overflow-y-auto border border-gray-300 rounded p-4'>
          {data.map((item, index) => (
            <div key={index} className='mb-2 p-2 bg-gray-800 rounded'>
              {item}
            </div>
          ))}
        </div> */}

        <div className='grid grid-cols-1 md:grid-cols-2 gap-4 w-screen my-10 mx-10 px-10'>
          <Terminal logs={logs}></Terminal>
          {/* <ActiveClientsGraph></ActiveClientsGraph> */}
        </div>

    </div>
  )
}
