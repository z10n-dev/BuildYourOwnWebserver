'use client'

import React from 'react'
import Terminal from '../components/Terminal';
import ActiveClientsGraph from '../components/ActiveClientsStats';
import { useSSE } from '../hooks/useSSE';
import { Stats } from '../components/Stats';

export default function page() {
  const { stats, logs } = useSSE();

  return (
    <div className='flex flex-col min-h-screen items-center'>
      <h1 className='text-3xl font-bold mb-4 flex justify-center mt-10'>Live Dashboard</h1>

      <div className='grid grid-cols-1 md:grid-cols-2 gap-4 w-screen my-10 mx-10 px-10'>
        <Terminal logs={logs}></Terminal>
        <ActiveClientsGraph></ActiveClientsGraph>
      </div>

      <div className='mx-10'>
        <Stats></Stats>
      </div>
    </div>
  )
}
